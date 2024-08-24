/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasBeenTradingFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company._
import models.address.{Address, TolerantAddress}
import models.{CompanyDetails, Index, NormalMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasBeenTradingCompanyControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute = controllers.routes.IndexController.onPageLoad
  val formProvider = new HasBeenTradingFormProvider()
  val form: Form[Boolean] = formProvider("messages__tradingAtLeastOneYear__error","test company name")
  val index: Index = Index(0)
  val srn: SchemeReferenceNumber
  val postCall: Call = controllers.register.trustees.company.routes.HasBeenTradingCompanyController.onSubmit(NormalMode, index, srn)

  val viewModel: CommonFormWithHintViewModel = CommonFormWithHintViewModel(
    controllers.register.trustees.company.routes.HasBeenTradingCompanyController.onSubmit(NormalMode, index, srn),
    title = Message("messages__trustee_company_trading-time__title"),
    heading = Message("messages__hasBeenTrading__h1", "test company name"),
    hint = None
  )
  val tolerantAddress: TolerantAddress = TolerantAddress(None, None, None, None, None, None)
  val address: Address = Address("line 1", "line 2", None, None, None, "GB")

  private def getTrusteeCompanyDataWithPreviousAddress(hasBeenTrading: Boolean): FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails("test company name"),
          HasBeenTradingCompanyId.toString -> hasBeenTrading,
          CompanyPreviousAddressPostcodeLookupId.toString -> Seq(tolerantAddress),
          CompanyPreviousAddressId.toString -> address,
          CompanyPreviousAddressListId.toString -> tolerantAddress
        )
      )
    ))
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompany): HasBeenTradingCompanyController =
    new HasBeenTradingCompanyController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view)(
      global
    )

  private def viewAsString(form: Form[_] = form) = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "HasBeenTradingCompanyController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(HasBeenTradingCompanyId(index), true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "clean up previous address, if user changes answer from yes to no" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller(getTrusteeCompanyDataWithPreviousAddress(hasBeenTrading = true)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER

      FakeUserAnswersService.userAnswer.get(HasBeenTradingCompanyId(index)).value mustEqual false
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressPostcodeLookupId(index)) mustBe None
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressId(index)) mustBe None
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressListId(index)) mustBe None
    }

    "not clean up for previous address, if user changes answer from no to yes" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getTrusteeCompanyDataWithPreviousAddress(hasBeenTrading = false)).onSubmit(NormalMode, index, None)(postRequest)

      status(result) mustBe SEE_OTHER

      FakeUserAnswersService.userAnswer.get(HasBeenTradingCompanyId(index)).value mustEqual true
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressPostcodeLookupId(index)) mustBe Some(Seq(tolerantAddress))
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressId(index)) mustBe Some(address)
      FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressListId(index)) mustBe Some(tolerantAddress)
    }
  }
}

