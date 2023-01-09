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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.trustees.ConfirmDeleteTrusteeFormProvider
import identifiers.TypedIdentifier
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models.person.PersonName
import models.register.trustees.TrusteeKind
import models.register.trustees.TrusteeKind.{Company, Individual, Partnership}
import models.{CompanyDetails, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.api.libs.json.Writes
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{FakeNavigator, UserAnswers}
import views.html.register.trustees.confirmDeleteTrustee

class ConfirmDeleteTrusteeControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteTrusteeControllerSpec._

  "ConfirmDeleteTrustee Controller" must {

    "return OK and the correct view for a GET to confirm deletion of a company trustee" in {
      val result = controller(testData(companyId)(companyTrustee)).onPageLoad(NormalMode, 0, Company, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyTrustee.companyName, Company)
    }

    "return OK and the correct view for a GET to confirm deletion of an individual trustee" in {
      val result = controller(testData(individualId)(individualTrustee)).onPageLoad(NormalMode, 0, Individual, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individualTrustee.fullName, Individual)
    }

    "return OK and the correct view for a GET to confirm deletion of a partnership trustee" in {
      val result = controller(testData(partnershipId)(partnershipTrustee)).onPageLoad(NormalMode, 0, Partnership, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(partnershipTrustee.name, Partnership)
    }

    "redirect to Session Expired for a GET if a deletable trustee cannot be found in UserAnswers" in {
      val result = controller(getEmptyData).onPageLoad(NormalMode, 0, Company, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a GET if no cached data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, 0, Company, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "remove the trustee in a POST request for a company trustee" in {
      val result = controller(testData(companyId)(companyTrustee)).onSubmit(NormalMode, 0, Company, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(companyId, companyTrustee.copy(isDeleted = true))
    }

    "remove the trustee in a POST request for an individual trustee" in {
        val result = controller(testData(individualId)(individualTrustee)).onSubmit(NormalMode, 0, Individual, None)(postRequest)

        status(result) mustBe SEE_OTHER
        FakeUserAnswersService.verify(individualId, individualTrustee.copy(isDeleted = true))

    }

    "remove the trustee in a POST request for a partnership trustee" in {
      val result = controller(testData(partnershipId)(partnershipTrustee)).onSubmit(NormalMode, 0, Partnership, None)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(partnershipId, partnershipTrustee.copy(isDeleted = true))
    }

    "redirect to the next page following a POST request when selected no" in {
      FakeUserAnswersService.reset()
      val result = controller(testData(partnershipId)(partnershipTrustee)).onSubmit(NormalMode, 0, Partnership, None)(postRequestForCancel)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(partnershipId)
    }

    "redirect to the next page following a POST request" in {
      val result = controller(testData(companyId)(companyTrustee)).onSubmit(NormalMode, 0, Company, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(testData(companyId)(companyTrustee)).onSubmit(NormalMode, 0, Company, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(companyTrustee.companyName, Company, boundForm)
    }

    "redirect to Session Expired following a POST if no cached data exists" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, 0, Company, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

}

object ConfirmDeleteTrusteeControllerSpec extends ControllerSpecBase {

  private val formProvider = new ConfirmDeleteTrusteeFormProvider()
  private val form         = formProvider.apply("test-company-name")

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private val postRequestForCancel: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private val individualId  = TrusteeNameId(0)
  private val companyId     = CompanyDetailsId(0)
  private val partnershipId = PartnershipDetailsId(0)

  private val individualTrustee = PersonName(
    "test-first-name",
    "test-last-name"
  )

  private val companyTrustee = CompanyDetails(
    "test-company-name"
  )

  private val partnershipTrustee = PartnershipDetails(
    "test-partnership-name"
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad


  private val view = injector.instanceOf[confirmDeleteTrustee]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new ConfirmDeleteTrusteeController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute),
      FakeUserAnswersService,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(trusteeName: String, trusteeKind: TrusteeKind, form: Form[_] = form) =
    view(
      form,
      trusteeName,
      routes.ConfirmDeleteTrusteeController.onSubmit(NormalMode, 0, trusteeKind, None),
      None,
      NormalMode,
      None
    )(fakeRequest, messages).toString

  private def testData[I <: TypedIdentifier.PathDependent](id: I)(value: id.Data)(implicit writes: Writes[id.Data]): DataRetrievalAction = {
    val userAnswers = UserAnswers()
      .set(id)(value)
      .asOpt
      .value

    new FakeDataRetrievalAction(Some(userAnswers.json))
  }

}
