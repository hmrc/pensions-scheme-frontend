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

package controllers.register.establishers.individual

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.individual.EstablisherNinoFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherNinoId}
import models._
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import views.html.register.establishers.individual.establisherNino

class EstablisherNinoControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new EstablisherNinoFormProvider()
  val form = formProvider()
  val firstIndex = Index(0)
  val establisherName = "test first name test last name"

  val validData = Map(SchemeDetailsId.toString -> Json.toJson(SchemeDetails("Test Scheme Name", SchemeType.SingleTrust)),
    EstablisherDetailsId.toString -> Json.toJson(EstablishersIndividualMap[EstablisherDetails](Map(
      0 -> EstablisherDetails("test first name", "test last name", LocalDate.now),
      1 -> EstablisherDetails("test", "test", LocalDate.now)))),
    EstablisherNinoId.toString -> Json.toJson(EstablishersIndividualMap[EstablisherNino](Map(0 -> EstablisherNino.Yes("CS700100A")))))

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCacheMap): EstablisherNinoController =
    new EstablisherNinoController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form): String = establisherNino(frontendAppConfig, form, NormalMode,
    firstIndex, establisherName)(fakeRequest, messages).toString

  "EstablisherNino Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {

      val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(CacheMap(cacheMapId, validData)))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(EstablisherNino.Yes("CS700100A")))
    }

    " redirect to Session Expired page when establisher name is not present" in {
      val result = controller (getEmptyCacheMap).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("establisherNino.hasNino", "yes"), ("establisherNino.nino", "CS700100A"))

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", EstablisherNino.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
