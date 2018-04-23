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

import play.api.data.Form
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import forms.register.UKBankDetailsFormProvider
import identifiers.register.{SchemeDetailsId, UKBankDetailsId}
import models.NormalMode
import views.html.register.uKBankDetails
import controllers.ControllerSpecBase
import models.register.SchemeType.SingleTrust
import models.register.{SchemeDetails, SortCode, UKBankDetails}
import org.apache.commons.lang3.RandomUtils
import org.joda.time.LocalDate
import play.api.mvc.Call

class UKBankDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new UKBankDetailsFormProvider()
  val form = formProvider()

  val schemeName = "Test Scheme Name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): UKBankDetailsController =
    new UKBankDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form): String = uKBankDetails(frontendAppConfig, form, NormalMode, schemeName)(fakeRequest, messages).toString

  val accountNo = RandomUtils.nextInt(10000000, 99999999).toString
  val sortCode = RandomUtils.nextInt(100000, 999999).toString

  "UKBankDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val bankDetails = UKBankDetails("test bank name", "test account name",
        SortCode("34", "45", "67"), "test account number", new LocalDate(LocalDate.now().getYear,
          LocalDate.now().getMonthOfYear, LocalDate.now().getDayOfMonth))

      val validData = Json.obj(
        UKBankDetailsId.toString -> Json.toJson(bankDetails),
        SchemeDetailsId.toString -> SchemeDetails(schemeName, SingleTrust)
      )

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(bankDetails))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("bankName", "test bank"),
        ("accountName", "test account"), ("sortCode", sortCode),
        ("accountNumber", accountNo),
        ("date.day", LocalDate.now().getDayOfMonth.toString), ("date.month", LocalDate.now().getMonthOfYear.toString),
        ("date.year", LocalDate.now().getYear.toString))

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

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("bankName", "test bank"),
            ("accountName", "test account"), ("sortCode", sortCode),
            ("accountNumber", accountNo),
            ("date.day", LocalDate.now().getDayOfMonth.toString), ("date.month", LocalDate.now().getMonthOfYear.toString),
            ("date.year", LocalDate.now().getYear.toString))

          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
      "scheme details are not retrieved" when {
        "GET" in {
          val result = controller(getEmptyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

          val result = controller(getEmptyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
