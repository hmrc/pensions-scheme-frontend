/*
 * Copyright 2022 HM Revenue & Customs
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
import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.AddressLookupConnector
import forms.address.PostCodeLookupFormProvider
import identifiers.TypedIdentifier
import models.NormalMode
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import navigators.Navigator
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
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
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

object PostcodeLookupControllerSpec {

  object FakeIdentifier extends TypedIdentifier[Seq[TolerantAddress]]
  object FakeAddressIdentifier extends TypedIdentifier[Address]
  object FakeSelectedAddressIdentifier extends TypedIdentifier[TolerantAddress]

  val postCall: Call = Call("POST", "www.example.com")
  val manualCall: Call = Call("GET", "www.example.com")

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val addressLookupConnector: AddressLookupConnector,
                                  override val navigator: Navigator,
                                  formProvider: PostCodeLookupFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: postcodeLookup
                                )(implicit val ec: ExecutionContext) extends PostcodeLookupController {

    def onPageLoad(viewmodel: PostcodeLookupViewModel, answers: UserAnswers): Future[Result] =
      get(viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000"))))

    def onSubmit(viewmodel: PostcodeLookupViewModel, answers: UserAnswers, request: Request[AnyContent] = FakeRequest()): Future[Result] =
      post(FakeIdentifier, viewmodel, NormalMode, invalidError)(DataRequest(request, "cacheId", answers, Some(PsaId("A0000000"))))

    private val invalidError: Message = "foo"

    override protected def form: Form[String] = formProvider()
  }

}

class PostcodeLookupControllerSpec extends SpecBase with Matchers with MockitoSugar with ScalaFutures with OptionValues {

  val viewmodel: PostcodeLookupViewModel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com")
  )

  private val view = injector.instanceOf[postcodeLookup]

  import PostcodeLookupControllerSpec._

  "get" must {
    "return a successful result" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val mat: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(), viewmodel, None)(request, messages).toString
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

          val request = FakeRequest()
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("postcode" -> "ZZ11ZZ"))

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

            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val request = FakeRequest()
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("postcode" -> "ZZ11ZZ"))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(
              formProvider().withError("postcode", "foo"), viewmodel, None)(request, messages).toString
        }
      }
      "the postcode is invalid" in {

        val invalidPostcode = "*" * 10

        val userAnswersService: UserAnswersService = mock[UserAnswersService]
        val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]

        verifyNoInteractions(addressConnector)

        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator),
          bind[UserAnswersService].toInstance(userAnswersService),
          bind[AddressLookupConnector].toInstance(addressConnector)
        )) {
          app =>

            implicit val mat: Materializer = app.materializer

            val request = FakeRequest().withFormUrlEncodedBody("postcode" -> invalidPostcode)

            val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
            val messages = app.injector.instanceOf[MessagesApi].preferred(request)
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewmodel, UserAnswers(), request)
            val form = formProvider().bind(Map("postcode" -> invalidPostcode))

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(form, viewmodel, None)(request, messages).toString
        }
      }
    }

    "return ok" when {
      "the postcode returns no results" which {
        "presents with form errors" in {

          val userAnswersService: UserAnswersService = mock[UserAnswersService]
          val addressConnector: AddressLookupConnector = mock[AddressLookupConnector]
          val postCode = "ZZ1 1ZZ"

          when(addressConnector.addressLookupByPostCode(eqTo(postCode))(any(), any())) thenReturn Future.successful {
            Seq.empty
          }

          running(_.overrides(
            bind[Navigator].toInstance(FakeNavigator),
            bind[UserAnswersService].toInstance(userAnswersService),
            bind[AddressLookupConnector].toInstance(addressConnector)
          )) {
            app =>

              implicit val mat: Materializer = app.materializer

              val formProvider = app.injector.instanceOf[PostCodeLookupFormProvider]
              val request = FakeRequest()
              val messages = app.injector.instanceOf[MessagesApi].preferred(request)
              val expectedErrorMessage = messages("messages__error__postcode_no_results", postCode)

              val controller = app.injector.instanceOf[TestController]
              val result = controller.onSubmit(viewmodel, UserAnswers(), request.withFormUrlEncodedBody("postcode" -> "ZZ11ZZ"))

              status(result) mustEqual OK

              val expectedResult = view(formProvider().withError("postcode", expectedErrorMessage), viewmodel, None)(request, messages).toString
              contentAsString(result) mustEqual expectedResult
          }
        }
      }
    }
  }
}
