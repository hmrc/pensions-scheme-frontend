/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.address

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, DataRetrievalAction, FakeDataRetrievalAction}
import forms.address.PostCodeLookupFormProvider
import identifiers.TypedIdentifier
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import models.{CheckUpdateMode, Mode, NormalMode}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpException
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

object PostcodeLookupControllerSpec {

  object FakeIdentifier extends TypedIdentifier[Seq[TolerantAddress]]
  object FakeAddressIdentifier extends TypedIdentifier[Address]
  object FakeSelectedAddressIdentifier extends TypedIdentifier[TolerantAddress]

  val postCall: Call = Call("POST", "www.example.com")
  val manualCall: Call = Call("GET", "www.example.com")
  val validPostcode = "ZZ1 1ZZ"
  val tolerantAddress = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))
  val address = Address("address line 1", "address line 2", None, None, Some(validPostcode), "GB")

  val answers =  Json.obj(FakeAddressIdentifier.toString -> address,
    FakeSelectedAddressIdentifier.toString -> tolerantAddress)
  val preSavedAddress = new FakeDataRetrievalAction(Some(answers))

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val addressLookupConnector: AddressLookupConnector,
                                  override val navigator: Navigator,
                                  formProvider: PostCodeLookupFormProvider
                                ) extends PostcodeLookupController {

    def onPageLoad(viewmodel: PostcodeLookupViewModel, answers: UserAnswers): Future[Result] =
      get(viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))

    def onSubmit(viewmodel: PostcodeLookupViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(FakeIdentifier, viewmodel, NormalMode, invalidError, noResultError)(DataRequest(request, "cacheId", answers, PsaId("A0000000")))

    def onClick(mode: Mode, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
        clear(FakeAddressIdentifier, FakeSelectedAddressIdentifier, mode, srn, manualCall)(DataRequest(request, "cacheId", answers, PsaId("A0000000")))

    private val srn = Some("123")
    private val invalidError: Message = "foo"

    private val noResultError: Message = "bar"

    override protected def form: Form[String] = formProvider()
  }

}

class PostcodeLookupControllerSpec extends WordSpec with MustMatchers with MockitoSugar with ScalaFutures with OptionValues {

  val viewmodel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com")
  )

  import PostcodeLookupControllerSpec._

  "get" must {
    "return a successful result" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val mat: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual postcodeLookup(appConfig, formProvider(), viewmodel, None)(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect on successful submission" in {

      val userAnswersService: UserAnswersService = mock[UserAnswersService]
      val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

      val address = TolerantAddress(Some(""), Some(""), None, None, None, Some("GB"))

      when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn Future.successful {
        Seq(address)
      }

      when(userAnswersService.save(eqTo(NormalMode), eqTo(None), eqTo(FakeIdentifier), eqTo(Seq(address)))(any(), any(), any(), any()))
      .thenReturn (Future.successful(Json.obj()))

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[AddressLookupConnector].toInstance(addressConnector)
      )) {
        app =>

          implicit val mat: Materializer = app.materializer

          val request = FakeRequest()
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request" when {
      "the postcode look fails to return result" in {

        val userAnswersService: UserAnswersService = mock[UserAnswersService]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn
          Future.failed(new HttpException("Failed", INTERNAL_SERVER_ERROR))

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(userAnswersService),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val request = FakeRequest()
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual postcodeLookup(appConfig,
              formProvider().withError("value", "foo"), viewmodel, None)(request, messages).toString
        }
      }
      "the postcode is invalid" in {

        val invalidPostcode = "*" * 10

        val userAnswersService: UserAnswersService = mock[UserAnswersService]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        verifyZeroInteractions(addressConnector)

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(userAnswersService),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val request = FakeRequest().withFormUrlEncodedBody("value" -> invalidPostcode)

            val appConfig = app.injector.instanceOf[FrontendAppConfig]
            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request)
            val form = formProvider().bind(Map("value" -> invalidPostcode))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual postcodeLookup(appConfig, form, viewmodel, None)(request, messages).toString
        }
      }
    }

    "return ok" when {
      "the postcode returns no results" which {
        "presents with form errors" in {

          val userAnswersService: UserAnswersService = mock[UserAnswersService]
          val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

          when(addressConnector.addressLookupByPostCode(eqTo("ZZ1 1ZZ"))(any(), any())) thenReturn Future.successful {
            Seq.empty
          }

          running(_.overrides(
            bind[Navigator].toInstance(FakeNavigator),
            bind[UserAnswersService].toInstance(userAnswersService),
            bind[AddressLookupConnector].toInstance(addressConnector)
          )) {
            app =>

              implicit val mat: Materializer = app.materializer

              val appConfig = app.injector.instanceOf[FrontendAppConfig]
              val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
              val request = FakeRequest()
              val messages = app.injector.instanceOf[MessagesApi].preferred(request)
              val controller = app.injector.instanceOf[TestController]
              val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("value" -> "ZZ11ZZ"))

              status(result) mustEqual OK
              contentAsString(result) mustEqual postcodeLookup(appConfig, formProvider().withError("value", "bar"), viewmodel, None)(request, messages).toString
          }
        }
      }
    }

    "clear saved address and selected address in list" when {
      "user clicks on manual entry link" in {
        val userAnswersService: UserAnswersService = mock[UserAnswersService]

        when(userAnswersService.remove(any(), any(), any())(any(), any(), any()))
            .thenReturn(Future.successful(Json.obj()))

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[DataRetrievalAction].toInstance(preSavedAddress),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[UserAnswersService].toInstance(userAnswersService)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val request = FakeRequest()
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onClick(CheckUpdateMode, UserAnswers(answers), request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result) mustBe Some(manualCall.url)
        }
      }
    }
  }
}
