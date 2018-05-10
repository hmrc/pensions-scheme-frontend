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

package controllers.register.trustees

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CheckMode, CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import views.html.register.trustees.addTrustee

class AddTrusteeControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def editTrusteeCompanyRoute(id: Int): String =
    controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id).url
  def editTrusteeIndividualRoute(id: Int): String =
    controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, id).url

  val formProvider = new AddTrusteeFormProvider()
  val schemeName = "Test Scheme Name"
  private val maxTrustees = frontendAppConfig.maxTrustees
  val trusteeCompanyA: (String, String) = ("Trustee Company A" -> editTrusteeCompanyRoute(0))
  val trusteeCompanyB: (String, String) = ("Trustee Company B" -> editTrusteeCompanyRoute(1))
  val trusteeIndividual: (String, String) = ("Trustee Individual" -> editTrusteeIndividualRoute(2))
  val allTrustees = Seq(trusteeCompanyA, trusteeCompanyB, trusteeIndividual)

  private def validData = {
    Json.obj(SchemeDetailsId.toString ->
      SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
        TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company A", None, None)
        ),
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company B", None, None)
        ),
        Json.obj(
          TrusteeDetailsId.toString -> PersonDetails("Trustee", None, "Individual", LocalDate.now())
        )
      )
    )
  }

  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeName): AddTrusteeController=
    new AddTrusteeController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form, trustees: Seq[(String, String)] = Seq.empty): String =
    addTrustee(frontendAppConfig, form, NormalMode, schemeName, trustees)(fakeRequest, messages).toString

  val testAnswer = "answer"

  "AddTrustee Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, allTrustees)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
