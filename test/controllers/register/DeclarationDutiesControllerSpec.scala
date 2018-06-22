/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register

import connectors.{FakeDataCacheConnector, PensionsSchemeConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationDutiesFormProvider
import identifiers.register.{DeclarationDutiesId, SchemeDetailsId}
import models.register.{SchemeDetails, SchemeSubmissionResponse, SchemeType}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, UserAnswers}
import views.html.register.declarationDuties

import scala.concurrent.{ExecutionContext, Future}

class DeclarationDutiesControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DeclarationDutiesFormProvider()
  val form = formProvider()


  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("test-scheme-id")

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
      Future.successful(validSchemeSubmissionResponse)
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): DeclarationDutiesController =
    new DeclarationDutiesController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider, fakePensionsSchemeConnector)

  def viewAsString(form: Form[_] = form): String = declarationDuties(frontendAppConfig, form, "Test Scheme Name")(fakeRequest, messages).toString

  "DeclarationDuties Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        SchemeDetailsId.toString -> SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
        DeclarationDutiesId.toString -> true
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = controller(dontGetAnyData).onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
