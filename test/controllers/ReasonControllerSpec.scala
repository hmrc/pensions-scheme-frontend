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
import forms.ReasonFormProvider
import identifiers.TypedIdentifier
import models.NormalMode
import models.requests.DataRequest
import navigators.Navigator
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ OptionValues}
import org.scalatest.matchers.must.Matchers
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.ReasonViewModel
import views.html.reason

import scala.concurrent.{ExecutionContext, Future}

class ReasonControllerSpec extends ControllerSpecBase with Matchers with OptionValues with ScalaFutures {

  import ReasonControllerSpec._

  val viewmodel: ReasonViewModel = ReasonViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    srn = srn)

  private val view = injector.instanceOf[reason]

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider(errorKey, companyName)(messages), viewmodel, None)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)("123456789").get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider(errorKey, companyName)(messages).fill("123456789"),
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
            ("reason", "123456789")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.verify(FakeIdentifier, "123456789")
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val request = FakeRequest().withFormUrlEncodedBody(("reason", "1234567^89{0}12345"))

          val messages = app.injector.instanceOf[MessagesApi].preferred(request)

          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider(errorKey, companyName)(messages).bind(Map("reason" -> "1234567^89{0}12345")),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }
}


object ReasonControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]

  val companyName = "test company"
  val errorKey = "messages__reason__error_utrRequired"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  val formProvider: ReasonFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: reason
                                )(implicit val ec: ExecutionContext) extends ReasonController {

    def onPageLoad(viewmodel: ReasonViewModel, answers: UserAnswers): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000")))
      get(FakeIdentifier, viewmodel, formProvider(errorKey, companyName))
    }

    def onSubmit(viewmodel: ReasonViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      implicit val request: DataRequest[AnyContent] = DataRequest(fakeRequest, "cacheId", answers, Some(PsaId("A0000000")))
      post(FakeIdentifier, NormalMode, viewmodel, formProvider(errorKey, companyName))
    }
  }

}



