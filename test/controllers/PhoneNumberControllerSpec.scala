/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.PhoneFormProvider
import identifiers.TypedIdentifier
import models.CheckUpdateMode
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ OptionValues}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.phoneNumber

import scala.concurrent.{ExecutionContext, Future}

class PhoneNumberControllerSpec extends SpecBase with Matchers with OptionValues with ScalaFutures with MockitoSugar {

  import PhoneNumberControllerSpec._

  val viewmodel: CommonFormWithHintViewModel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("legend")
  )

  private val view = injector.instanceOf[phoneNumber]

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(), viewmodel, None)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)("098777777777").get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider().fill("098777777777"),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      running(_.overrides(
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val request = FakeRequest().withFormUrlEncodedBody(
            ("phone", "098777777777")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.verify(FakeIdentifier, "098777777777")
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[PhoneFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider().bind(Map.empty[String, String]),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }
}

object PhoneNumberControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]
  val companyName = "test company name"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  formProvider: PhoneFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: phoneNumber
                                )(implicit val ec: ExecutionContext) extends PhoneNumberController {

    def onPageLoad(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(), viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000"))))
    }

    def onSubmit(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, CheckUpdateMode, formProvider(), viewmodel)(DataRequest(fakeRequest, "cacheId", answers, Some(PsaId("A0000000"))))
    }
  }
}
