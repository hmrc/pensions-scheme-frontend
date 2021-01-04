/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions.DataRetrievalAction
import forms.NINOFormProvider
import identifiers.TypedIdentifier
import javax.inject.Inject
import models.requests.DataRequest
import models.{Mode, NormalMode, ReferenceValue}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{Message, NinoViewModel}
import views.html.nino

import scala.concurrent.{ExecutionContext, Future}


class NinoControllerSpec extends ControllerSpecBase {

  override def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "/")

  val viewmodel = NinoViewModel(
    postCall = Call("POST", "/"),
    title = Message("messages__enterNINO", Message("messages__thePerson").resolve),
    heading = Message("messages__enterNINO"),
    hint = Message("messages__common__nino_hint"),
    srn = None
  )

  object FakeIdentifier extends TypedIdentifier[ReferenceValue]

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new NINOFormProvider()
  val form = formProvider("Mark")

  private val view = injector.instanceOf[nino]

  class TestNinoController @Inject()(
                                      override val appConfig: FrontendAppConfig,
                                      override val messagesApi: MessagesApi,
                                      override val userAnswersService: UserAnswersService,
                                      override val navigator: Navigator,
                                      val controllerComponents: MessagesControllerComponents,
                                      val view: nino
                                    )(implicit val ec: ExecutionContext) extends NinoController {

    def onPageLoad(answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, form, viewmodel)(DataRequest(FakeRequest(), "cacheId", answers, Some(PsaId("A0000000"))))
    }

    def onSubmit(mode: Mode, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, form, viewmodel)(DataRequest(fakeRequest, "cacheId", answers, Some(PsaId("A0000000"))))
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): TestNinoController =
    new TestNinoController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewmodel, None)(fakeRequest, messages).toString


  "NinoController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(UserAnswers())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val result = controller().onPageLoad(UserAnswers().set(FakeIdentifier)(ReferenceValue("nino")).asOpt.get)

      contentAsString(result) mustBe viewAsString(form.fill(ReferenceValue("nino")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "CS700100A"))

      val result = controller().onSubmit(NormalMode, UserAnswers(), postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(FakeIdentifier, ReferenceValue("CS700100A", isEditable = true))
    }

    "return a Bad Request and errors invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "invalid value"))
      val boundForm = form.bind(Map("nino" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, UserAnswers(), postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

  }
}
