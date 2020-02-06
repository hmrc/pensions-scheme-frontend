/*
 * Copyright 2020 HM Revenue & Customs
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

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import forms.address.ConfirmAddressFormProvider
import identifiers.{SchemeNameId, TypedIdentifier}
import models._
import models.address.Address
import models.requests.DataRequest
import navigators.Navigator
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserAnswersService
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import scala.concurrent.{ExecutionContext, Future}


class ConfirmPreviousAddressControllerSpec extends SpecBase with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  "get" must {

    "return a successful result when user has not answered the question previously" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ConfirmAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(), userAnswers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(name), viewmodel(), countryOptions, Some(schemeName))(request, messages).toString
      }
    }

    "return a successful result when user has answered the question previously" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ConfirmAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel(), userAnswersWithId(true))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(name).fill(true),
            viewmodel(),
            countryOptions,
            Some(schemeName)
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid and the data is changed" in {

      import play.api.inject._

      val userAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[Navigator].toInstance(FakeNavigator),
        bind[ConfirmAddressFormProvider].toInstance(new ConfirmAddressFormProvider())
      )) {
        app =>
          when(userAnswersService.upsert(
            any(), any(), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          when(userAnswersService.save[Address, PreviousAddressId.type](
            any(), any(), eqTo(PreviousAddressId), any())(any(), any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), userAnswersWithId(false), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a redirect and save the data when the there is no existing data and user answers yes" in {

      import play.api.inject._

      val userAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(Some(userAnswers.json)))
      )) {
        app =>

          when(userAnswersService.upsert(
            any(), any(), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "true"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), userAnswers, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          verify(userAnswersService, times(1)).upsert(any(), any(), any())(any(), any(), any())
      }
    }

    "return a redirect and save the data when the there is no existing data and user answers no" in {

      import play.api.inject._

      val userAnswersService = mock[UserAnswersService]

      running(_.overrides(
        bind[UserAnswersService].toInstance(userAnswersService),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(Some(userAnswers.json)))
      )) {
        app =>

          when(userAnswersService.save(
            any(), any(), any(), any())(any(), any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> "false"
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), userAnswers, request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          verify(userAnswersService, times(1)).save(any(), any(), any(), any())(any(), any(), any(), any())
      }
    }


    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ConfirmAddressFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val countryOptions = app.injector.instanceOf[CountryOptions]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel(), userAnswers, request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider(errorMessage(messages)).bind(Map.empty[String, String]),
            viewmodel(),
            countryOptions,
            Some(schemeName)
          )(request, messages).toString
      }
    }
  }
  
  val schemeName = "Test Scheme Name"
  val name = "Test name"
  private val psaId = PsaId("A0000000")
  private val userAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  private val view = injector.instanceOf[confirmPreviousAddress]

  private def userAnswersWithId(id: Boolean) = UserAnswers()
    .set(SchemeNameId)(schemeName).flatMap(
    _.set(FakeIdentifier)(id)).asOpt.value

  private def testAddress(line2: String) = Address(
    "address line 1",
    line2,
    Some("test town"),
    Some("test county"),
    Some("test post code"), "GB"
  )

  private def viewmodel(line2: String = "address line 2") = ConfirmAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("hint"),
    address = testAddress(line2),
    name = name,
    srn = Some("S12345")
  )

  private def errorMessage(implicit messages: Messages) = Message("messages__confirmPreviousAddress__error", "Test name").resolve


  object FakeIdentifier extends TypedIdentifier[Boolean]

  object PreviousAddressId extends TypedIdentifier[Address]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  override val countryOptions: CountryOptions,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: confirmPreviousAddress
                                )(implicit val ec: ExecutionContext) extends ConfirmPreviousAddressController {

    def onPageLoad(viewmodel: ConfirmAddressViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, psaId))
    }

    def onSubmit(viewmodel: ConfirmAddressViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, PreviousAddressId, viewmodel, NormalMode)(DataRequest(fakeRequest, "cacheId", answers, psaId))
    }


  }

}
