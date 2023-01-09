/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{FakeUserAnswersCacheConnector, PensionAdministratorConnector}
import controllers.actions._
import forms.register.SchemeTypeFormProvider
import models.NormalMode
import models.register.SchemeType
import play.api.data.Form
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import utils.{FakeNavigator, NameMatching, NameMatchingFactory, UserAnswers}
import views.html.schemeType

import scala.concurrent.{ExecutionContext, Future}

class SchemeTypeControllerSpec extends ControllerSpecBase {
  private def onwardRoute = controllers.routes.IndexController.onPageLoad
  private val view = injector.instanceOf[schemeType]

  val formProvider = new SchemeTypeFormProvider()
  val form = formProvider()
  val schemeName = "Test Scheme Name"

  val config: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]
  val pensionAdministratorConnector: PensionAdministratorConnector = injector.instanceOf[PensionAdministratorConnector]

  val minData = UserAnswers().schemeName(schemeName).dataRetrievalAction

  object FakeNameMatchingFactory extends NameMatchingFactory(pensionAdministratorConnector) {
    override def nameMatching(schemeName: String)
                             (implicit ec: ExecutionContext,
                              hc: HeaderCarrier): Future[NameMatching] =
      Future.successful(NameMatching("value 1", "My PSA"))
  }

  def controller(dataRetrievalAction: DataRetrievalAction = minData): SchemeTypeController =
    new SchemeTypeController(
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) = view(form, NormalMode, schemeName)(fakeRequest, messages).toString

  "SchemeType Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = UserAnswers().schemeName(schemeName).schemeType(SchemeType.SingleTrust).json
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(SchemeType.SingleTrust))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("schemeType.type", "single"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors" when {
      "invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "scheme name matches psa name" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "My PSA"))
        val boundForm = form.bind(Map("value" -> "My PSA"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }

  }
}
