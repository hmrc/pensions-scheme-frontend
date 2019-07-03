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

package controllers.register.establishers.company

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.ReasonController
import forms.register.establishers.company.NoCompanyNumberFormProvider
import identifiers.TypedIdentifier
import models.NormalMode
import models.requests.DataRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.ReasonViewModel
import views.html.reason

import scala.concurrent.{ExecutionContext, Future}

class NoCompanyNumberControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with GuiceOneAppPerSuite {

  import NoCompanyNumberControllerSpec._

  override lazy val app = new GuiceApplicationBuilder()
    .overrides(
    bind[Navigator].toInstance(FakeNavigator),
    bind[UserAnswersService].toInstance(FakeUserAnswersService)
  ).build()

  implicit val materializer: Materializer = app.materializer

  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val formProvider = app.injector.instanceOf[NoCompanyNumberFormProvider]
  val request = FakeRequest()
  val messages = app.injector.instanceOf[MessagesApi].preferred(request)
  val controller = app.injector.instanceOf[TestController]

  "NoCompanyNumberController" when {

    "calling get method" must {

      "return a successful result when there is no existing answer" in {
          val result = controller.onPageLoad(viewModel, UserAnswers())
          status(result) mustEqual OK
          contentAsString(result) mustEqual reason(
            appConfig,
            formProvider("test company")(messages),
            viewModel,
            None)(request, messages).toString
      }

      "return a successful result when there is an existing answer" in {
          val answers = UserAnswers().set(FakeIdentifier)("123456789").get
          val result = controller.onPageLoad(viewModel, answers)
          status(result) mustEqual OK
          contentAsString(result) mustEqual reason(
            appConfig,
            formProvider("test company")(messages).fill("123456789"),
            viewModel,
            None
          )(request, messages).toString
      }

    }

    "calling post method" must {

      "return a redirect when the submitted data is valid" in {
          val request = FakeRequest().withFormUrlEncodedBody(("reason", "123456789"))
          val result = controller.onSubmit(viewModel, UserAnswers(), request)
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.verify(FakeIdentifier, "123456789")
      }

      "return a bad request when the submitted data is invalid" in {
          val request = FakeRequest().withFormUrlEncodedBody(("reason", "123456789{0}12345"))
          val result = controller.onSubmit(viewModel, UserAnswers(), request)
          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual reason(
            appConfig,
            formProvider("test company")(messages).bind(Map("reason" -> "123456789{0}12345")),
            viewModel,
            None
          )(request, messages).toString
      }
    }
  }
}


object NoCompanyNumberControllerSpec extends NoCompanyNumberControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]

  val viewModel = ReasonViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading"
  )

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  val formProvider: NoCompanyNumberFormProvider
                                )(implicit val ec: ExecutionContext) extends ReasonController {

    def onPageLoad(viewModel: ReasonViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, viewModel, formProvider("test company"))(DataRequest(FakeRequest(), "cacheId", answers, PsaId("A0000000")))
    }

    def onSubmit(viewModel: ReasonViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, viewModel, formProvider("test company"))(DataRequest(fakeRequest, "cacheId", answers, PsaId("A0000000")))
    }
  }
}

