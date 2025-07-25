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
import forms.UTRFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, ReferenceValue}
import navigators.Navigator
import org.apache.pekko.stream.Materializer
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.*
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import utils.{FakeNavigator, UserAnswers}
import viewmodels.UTRViewModel
import views.html.utr

import scala.concurrent.{ExecutionContext, Future}

class UTRControllerSpec extends SpecBase with Matchers with OptionValues with ScalaFutures with BeforeAndAfterEach {

  import UTRControllerSpec.*

  private val view = injector.instanceOf[utr]
  val viewmodel: UTRViewModel = UTRViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = "legend"
  )

  override def beforeEach(): Unit = {
    FakeUserAnswersService.reset()
    super.beforeEach()
  }

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[UTRFormProvider]
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

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[UTRFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(ReferenceValue("1234567890")).get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider().fill(ReferenceValue("1234567890")),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject.*

      running(_.overrides(
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val request = FakeRequest().withFormUrlEncodedBody(("utr", "1234567890"))
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
          FakeUserAnswersService.getData.mustBe(
            Json.obj(
              "userAnswer" -> Json.obj(
                FakeHasUTRIdentifier.toString -> true,
                FakeIdentifier.toString -> Json.obj(
                  "value" -> "1234567890",
                  "isEditable" -> true
                )
              )
            )
          )
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val formProvider = app.injector.instanceOf[UTRFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val request = FakeRequest().withFormUrlEncodedBody(("utr", "123456789012345"))

          val messages = app.injector.instanceOf[MessagesApi].preferred(request)

          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider().bind(Map("utr" -> "123456789012345")),
            viewmodel,
            None
          )(request, messages).toString
      }
    }
  }
}



object UTRControllerSpec {

  object FakeIdentifier extends TypedIdentifier[ReferenceValue]
  object FakeHasUTRIdentifier extends TypedIdentifier[Boolean]

  val companyName = "test company"
  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val userAnswersService: UserAnswersService,
                                  override val navigator: Navigator,
                                  val formProvider: UTRFormProvider,
                                  val controllerComponents: MessagesControllerComponents,
                                  val view: utr
                                )(implicit val ec: ExecutionContext) extends UTRController {

    def onPageLoad(viewmodel: UTRViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, viewmodel, formProvider())(DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000"))))
    }

    def onSubmit(viewmodel: UTRViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, viewmodel, formProvider(), FakeHasUTRIdentifier)(DataRequest(fakeRequest, "cacheId", answers, Some(PsaId("A0000000"))))
    }
  }

}


