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

package controllers

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models.Mode._
import models._
import models.address.Address
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersBenefitsAndInsuranceControllerSpec extends ControllerSpecBase with OptionValues with BeforeAndAfterEach {

  import CheckYourAnswersBenefitsAndInsuranceControllerSpec._

  "CheckYourAnswersBenefitsAndInsurance Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(NormalMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onPageLoad() is called with UpdateMode with less data" must {
      "return OK and the correct view" in {
        val result = controller(updateData).onPageLoad(UpdateMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsStringWithLessData(UpdateMode)
      }
    }

    "onPageLoad() is called with UpdateMode" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(UpdateMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, hideSaveAndContinueButton = true)
      }

      "NOT display submit button with return to tasklist when in update mode" in {
        val result = controller(data).onPageLoad(UpdateMode, None)(fakeRequest)
        status(result) mustBe OK
        assertNotRenderedById(asDocument(contentAsString(result)), "submit")
      }

      "display submit button with return to tasklist when in normal mode" in {
        val result = controller(data).onPageLoad(NormalMode, srn)(fakeRequest)
        status(result) mustBe OK
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }
    }
  }
}

object CheckYourAnswersBenefitsAndInsuranceControllerSpec extends ControllerSpecBase with MockitoSugar {
  private val schemeName = "Test Scheme Name"
  private val insuranceCompanyName = "Test company Name"
  private val policyNumber = "Test policy number"

  private def postUrl(mode: Mode) = routes.PsaSchemeTaskListController.onPageLoad(mode, None)

  private val insurerAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")
  private val data = UserAnswers().schemeName(schemeName).investmentRegulated(true).occupationalPensionScheme(true).
    typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(true).insuranceCompanyName(insuranceCompanyName).
    insurancePolicyNumber(policyNumber).insurerConfirmAddress(insurerAddress).dataRetrievalAction

  private val updateData = UserAnswers().schemeName(schemeName).investmentRegulated(true).occupationalPensionScheme(true).
    typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(true)
    .insuranceCompanyName(insuranceCompanyName).dataRetrievalAction

  private val view = injector.instanceOf[checkYourAnswers]

  private def controller(dataRetrievalAction: DataRetrievalAction): CheckYourAnswersBenefitsAndInsuranceController =
    new CheckYourAnswersBenefitsAndInsuranceController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      getEmptyDataPsp,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      controllerComponents,
      view
    )

  private def benefitsAndInsuranceSection(mode: Mode) = AnswerSection(
    None,
    commonRows(mode) ++ Seq(
      AnswerRow(
        messages("messages__insurance_policy_number_cya_label", insuranceCompanyName),
        Seq(policyNumber),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.InsurancePolicyNumberController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__insurance_policy_number", insuranceCompanyName))))
      ),
      AnswerRow(
        messages("messages__addressFor", insuranceCompanyName),
        Seq(
          insurerAddress.addressLine1,
          insurerAddress.addressLine2,
          insurerAddress.addressLine3.get,
          insurerAddress.addressLine4.get,
          insurerAddress.postcode.get,
          "Country of GB"),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.InsurerConfirmAddressController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__insurer_confirm_address", insuranceCompanyName)))))
    )
  )

  private def updateBenefitsAndInsuranceSection(mode: Mode) = AnswerSection(
    None,
    commonRows(mode) ++ Seq(
      AnswerRow(
        messages("messages__insurance_policy_number_cya_label", insuranceCompanyName),
        Seq("site.not_entered"),
        answerIsMessageKey = true,
        Some(Link("site.add", routes.InsurancePolicyNumberController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__insurance_policy_number", insuranceCompanyName))))
      ),
      AnswerRow(
        messages("messages__addressFor", insuranceCompanyName),
        Seq("site.not_entered"),
        answerIsMessageKey = true,
        Some(Link("site.add", routes.InsurerConfirmAddressController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__insurer_confirm_address", insuranceCompanyName)))))
    )
  )

  private def commonRows(mode: Mode): Seq[AnswerRow] = {
    Seq(
      AnswerRow(
        messages("messages__investment_regulated_scheme__h1", schemeName),
        Seq("site.yes"),
        answerIsMessageKey = true,
        if (mode == UpdateMode) {
          None
        } else {
          Some(Link("site.change", routes.InvestmentRegulatedSchemeController.onPageLoad(checkMode(mode)).url,
            Some(messages("messages__visuallyhidden__investmentRegulated", schemeName))))
        }
      ),
      AnswerRow(
        messages("messages__occupational_pension_scheme__h1", schemeName),
        Seq("site.yes"),
        answerIsMessageKey = true,
        if (mode == UpdateMode) {
          None
        } else {
          Some(Link("site.change", routes.OccupationalPensionSchemeController.onPageLoad(checkMode(mode)).url,
            Some(messages("messages__visuallyhidden__occupationalPensionScheme", schemeName))))
        }
      ),
      AnswerRow(
        messages("messages__type_of_benefits_cya_label", schemeName),
        Seq(s"messages__type_of_benefits__${TypeOfBenefits.Defined}"),
        answerIsMessageKey = true,
        Some(Link("site.change", controllers.routes.TypeOfBenefitsController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__type_of_benefits_change", schemeName))))
      ),
      AnswerRow(
        messages("securedBenefits.checkYourAnswersLabel", schemeName),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.BenefitsSecuredByInsuranceController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__securedBenefits", schemeName))))
      ),
      AnswerRow(
        messages("insuranceCompanyName.checkYourAnswersLabel"),
        Seq(insuranceCompanyName),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.InsuranceCompanyNameController.onPageLoad(checkMode(mode), None).url,
          Some(messages("messages__visuallyhidden__insuranceCompanyName"))))
      )
    )
  }

  def heading(name: String, mode: Mode): String = if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
    Message("messages__benefitsAndInsuranceDetailsFor", name)

  def vm(mode: Mode, hideSaveAndContinueButton: Boolean, data: AnswerSection): CYAViewModel = CYAViewModel(
    answerSections = Seq(data),
    href = postUrl(mode),
    schemeName = Some(schemeName),
    returnOverview = false,
    hideEditLinks = false,
    srn = None,
    hideSaveAndContinueButton = hideSaveAndContinueButton,
    title = heading(Message("messages__theScheme").resolve, mode),
    h1 = heading(schemeName, mode)
  )

  private def viewAsString(mode: Mode = NormalMode, hideSaveAndContinueButton: Boolean = false): String =
    view(vm(mode, hideSaveAndContinueButton, benefitsAndInsuranceSection(mode)))(fakeRequest, messages).toString

  private def viewAsStringWithLessData(mode: Mode): String =
    view(vm(mode, hideSaveAndContinueButton = true, updateBenefitsAndInsuranceSection(mode)))(fakeRequest, messages).toString

}




