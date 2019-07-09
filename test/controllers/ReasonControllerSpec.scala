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

package controllers

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import forms.ReasonFormProvider
import identifiers.TypedIdentifier
import models.NormalMode
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.ReasonViewModel
import views.html.reason

import scala.concurrent.{ExecutionContext, Future}

class ReasonControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures {

  import ReasonControllerSpec._

  val viewmodel = ReasonViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading"
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual reason(appConfig, formProvider(errorKey, companyName)(messages), viewmodel, None)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)("123456789").get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual reason(
            appConfig,
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

          implicit val materializer: Materializer = app.materializer

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

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[ReasonFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val request = FakeRequest().withFormUrlEncodedBody(("reason", "1234567^89{0}12345"))

          val messages = app.injector.instanceOf[MessagesApi].preferred(request)

          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual reason(
            appConfig,
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
                                  val formProvider: ReasonFormProvider
                                )(implicit val ec: ExecutionContext) extends ReasonController {

    def onPageLoad(viewmodel: ReasonViewModel, answers: UserAnswers): Future[Result] =
      get(FakeIdentifier, viewmodel, formProvider(errorKey, companyName))(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))

    def onSubmit(viewmodel: ReasonViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] =
      post(FakeIdentifier, NormalMode, viewmodel, formProvider(errorKey, companyName))(DataRequest(fakeRequest, "cacheId", answers, PsaId("A0000000")))
  }

}



