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

package controllers.register.establishers

import play.api.data.Form
import play.api.libs.json.{JsBoolean, Json}
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import play.api.test.Helpers._
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.AddEstablisherId
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.{EstablisherDetails, NormalMode, SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import views.html.register.establishers.addEstablisher

class AddEstablisherControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddEstablisherFormProvider()
  val form = formProvider()
  val schemeName = "Test Scheme Name"
  val day = LocalDate.now().getDayOfMonth
  val month = LocalDate.now().getMonthOfYear
  val year = LocalDate.now().getYear - 20

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameCacheMap): AddEstablisherController =
    new AddEstablisherController(frontendAppConfig, messagesApi, FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form, allEstablishers: Option[Map[String, String]] = None): String = addEstablisher(frontendAppConfig,
    form, NormalMode, allEstablishers, schemeName)(fakeRequest, messages).toString

  "AddEstablisher Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired page on a GET when scheme name is not present" in {
      val result = controller(getEmptyCacheMap).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
