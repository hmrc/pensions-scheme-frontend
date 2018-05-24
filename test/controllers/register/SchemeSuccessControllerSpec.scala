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

import connectors.DataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import views.html.register.schemeSuccess
import controllers.ControllerSpecBase
import identifiers.register.{SchemeDetailsId, SubmissionReferenceNumberId}
import models.register.{SchemeDetails, SchemeSubmissionResponse, SchemeType}
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import utils.FakeNavigator
import org.mockito.Mockito._
import play.api.mvc.Results._

import scala.concurrent.Future

class SchemeSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = controllers.routes.LogoutController.onPageLoad()

  val submissionReferenceNumber="XX123456789132"

  val fakeDataCacheConnector = mock[DataCacheConnector]

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString -> Json.toJson(SchemeDetails("test scheme name", SchemeType.SingleTrust)),
    SubmissionReferenceNumberId.toString->SchemeSubmissionResponse(submissionReferenceNumber)
  )

  private def controller(dataRetrievalAction: DataRetrievalAction =
                 new FakeDataRetrievalAction(Some(validData))):SchemeSuccessController =
    new SchemeSuccessController(
      frontendAppConfig,
      messagesApi,
      fakeDataCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute)
    )

  def viewAsString(): String =
    schemeSuccess(
      frontendAppConfig,
      Some("test scheme name"),
      LocalDate.now(),
      submissionReferenceNumber
    )(fakeRequest, messages).toString

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET" in {
      when(fakeDataCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))

      val result = controller().onPageLoad(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
     verify(fakeDataCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page for a POST" in {
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

    }
  }

}
