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

package controllers

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.IsAboutBenefitsAndInsuranceCompleteId
import models.address.Address
import models.{CheckMode, TypeOfBenefits}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeSectionComplete, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersBenefitsAndInsuranceControllerSpec extends ControllerSpecBase with OptionValues {

  import CheckYourAnswersBenefitsAndInsuranceControllerSpec._

  "CheckYourAnswersBenefitsAndInsurance Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SchemeTaskListController.onPageLoad().url
        FakeSectionComplete.verify(IsAboutBenefitsAndInsuranceCompleteId, true)
      }
    }
  }
}

object CheckYourAnswersBenefitsAndInsuranceControllerSpec extends ControllerSpecBase {

  private val insuranceCompanyName = "Test company Name"
  private val policyNumber = "Test policy number"
  private val postUrl = routes.CheckYourAnswersBenefitsAndInsuranceController.onSubmit()
  private val insurerAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")
  private val data = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
    typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(true).insuranceCompanyName(insuranceCompanyName).
    insurancePolicyNumber(policyNumber).insurerConfirmAddress(insurerAddress).dataRetrievalAction

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersBenefitsAndInsuranceController =
    new CheckYourAnswersBenefitsAndInsuranceController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      new FakeCountryOptions
    )

  private val benefitsAndInsuranceSection = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("investmentRegulated.checkYourAnswersLabel"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(routes.InvestmentRegulatedSchemeController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__investmentRegulated")
      ),
      AnswerRow(
        messages("occupationalPensionScheme.checkYourAnswersLabel"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(routes.OccupationalPensionSchemeController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__occupationalPensionScheme")
      ),
      AnswerRow(
        messages("messages__type_of_benefits_cya_label"),
        Seq(s"messages__type_of_benefits__${TypeOfBenefits.Defined}"),
        answerIsMessageKey = true,
        Some(controllers.routes.TypeOfBenefitsController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__type_of_benefits_change")
      ),
      AnswerRow(
        messages("securedBenefits.checkYourAnswersLabel"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(routes.BenefitsSecuredByInsuranceController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__securedBenefits")
      ),
      AnswerRow(
        messages("insuranceCompanyName.checkYourAnswersLabel"),
        Seq(insuranceCompanyName),
        answerIsMessageKey = false,
        Some(routes.InsuranceCompanyNameController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__insuranceCompanyName")
      ),
      AnswerRow(
        messages("messages__insurance_policy_number_cya_label", insuranceCompanyName),
        Seq(policyNumber),
        answerIsMessageKey = false,
        Some(routes.InsurancePolicyNumberController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__insurance_policy_number", insuranceCompanyName)
      ),
      AnswerRow(
        messages("messages__insurer_confirm_address_cya_label"),
        Seq(
          insurerAddress.addressLine1,
          insurerAddress.addressLine2,
          insurerAddress.addressLine3.get,
          insurerAddress.addressLine4.get,
          insurerAddress.postcode.get,
          "Country of GB"),
        answerIsMessageKey = false,
        Some(routes.InsurerConfirmAddressController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__insurer_confirm_address"))
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(
      benefitsAndInsuranceSection
    ),
    postUrl,
    None
  )(fakeRequest, messages).toString

}




