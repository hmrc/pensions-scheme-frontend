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

package controllers.register.trustees

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.AddTrusteeFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.register._
import models.register.trustees.TrusteeKind
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import views.html.register.trustees.addTrustee

class AddTrusteeControllerSpec extends ControllerSpecBase {
  appRunning()
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def editTrusteeCompanyRoute(id: Int): String =
    controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id, None).url

  def editTrusteeIndividualRoute(id: Int): String =
    controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, id, None).url

  def deleteTrusteeRoute(id: Int, kind: TrusteeKind): String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, id, kind, None).url

  val formProvider = new AddTrusteeFormProvider()
  val schemeName = "Test Scheme Name"

  lazy val trusteeCompanyA: TrusteeCompanyEntity = TrusteeCompanyEntity(CompanyDetailsId(0), "Trustee Company A", isDeleted = false, isCompleted = false)
  lazy val trusteeCompanyB: TrusteeCompanyEntity = TrusteeCompanyEntity(CompanyDetailsId(1), "Trustee Company B", isDeleted = false, isCompleted = false)
  lazy val trusteeIndividual: TrusteeIndividualEntity = TrusteeIndividualEntity(TrusteeDetailsId(2), "Trustee Individual",
    isDeleted = false, isCompleted = false)

  lazy val allTrustees = Seq(trusteeCompanyA, trusteeCompanyB, trusteeIndividual)

  private def validData = {
    Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company A")
        ),
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("Trustee Company B")
        ),
        Json.obj(
          TrusteeDetailsId.toString -> PersonDetails("Trustee", None, "Individual", LocalDate.now())
        )
      )
    )
  }

  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): AddTrusteeController =
    new AddTrusteeController(frontendAppConfig, messagesApi, FakeUserAnswersCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  val submitUrl = controllers.register.trustees.routes.AddTrusteeController.onSubmit(NormalMode, None)
  def viewAsString(form: Form[_] = form, trustees: Seq[Trustee[_]] = Seq.empty): String =
    addTrustee(frontendAppConfig, form, NormalMode, trustees, None, submitUrl)(fakeRequest, messages).toString

  val testAnswer = "answer"

  "AddTrustee Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, allTrustees)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
