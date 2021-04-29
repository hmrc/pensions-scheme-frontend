/*
 * Copyright 2021 HM Revenue & Customs
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

package utils.datacompletion

import base.JsonFileReader
import helpers.DataCompletionHelper
import identifiers.TcmpToggleId
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorEnterNINOId, DirectorHasNINOId, DirectorNoNINOReasonId}
import identifiers.register.trustees.individual.{TrusteeEnterNINOId, TrusteeHasNINOId, TrusteeNoNINOReasonId}
import identifiers.register.trustees.{company => tc}
import models.address.Address
import models.register.{SchemeType, SortCode}
import models._
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsValue
import utils.{Enumerable, UserAnswers}

class DataCompletionSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import DataCompletionSpec._

  "All generic methods" when {
    "isComplete" must {
      "return Some(true) only when all values in list are true" in {
        UserAnswers().isComplete(Seq(Some(true), Some(true), Some(true))) mustBe Some(true)
        UserAnswers().isComplete(Seq(Some(true), Some(false), Some(true))) mustBe Some(false)
      }

      "return None when only all values in list are true" in {
        UserAnswers().isComplete(Seq(None, None, None, None)) mustBe None
        UserAnswers().isComplete(Seq(None, Some(false), Some(true))) mustBe Some(false)
      }

      "return false in every other case" in {
        UserAnswers().isComplete(Seq(Some(true), None, Some(false), None)) mustBe Some(false)
        UserAnswers().isComplete(Seq(None, Some(true), Some(true))) mustBe Some(false)
      }
    }

    "isListComplete" must {
      "return true only when all values in list are true" in {
        UserAnswers().isListComplete(Seq(true, true, true)) mustBe true
      }

      "return false in every other case" in {
        UserAnswers().isListComplete(Seq(true, false, true)) mustBe false
      }
    }

    "isAddressComplete" must {
      "return None when current Address is missing" in {
        UserAnswers(userAnswersUninitiated).isAddressComplete(CompanyAddressId(0), CompanyPreviousAddressId(0),
          CompanyAddressYearsId(0), Some(HasBeenTradingCompanyId(0))) mustBe None
      }

      "return Some(true) when entire address journey is completed" in {
        UserAnswers(userAnswersCompleted).isAddressComplete(CompanyAddressId(0), CompanyPreviousAddressId(0),
          CompanyAddressYearsId(0), Some(HasBeenTradingCompanyId(0))) mustBe Some(true)
      }

      "return Some(false) when previous Address is missing" in {
        UserAnswers(userAnswersInProgress).isAddressComplete(CompanyAddressId(0), CompanyPreviousAddressId(0),
          CompanyAddressYearsId(0), Some(HasBeenTradingCompanyId(0))) mustBe Some(false)
      }
    }

    "isContactDetailsComplete" must {
      "return None when both contact details are missing" in {
        UserAnswers(userAnswersUninitiated).isContactDetailsComplete(CompanyEmailId(0), CompanyPhoneId(0)) mustBe None
      }

      "return Some(true) when contact details are complete" in {
        UserAnswers(userAnswersCompleted).isContactDetailsComplete(CompanyEmailId(0), CompanyPhoneId(0)) mustBe Some(true)
      }

      "return Some(false) when one of the contact details is missing" in {
        UserAnswers(userAnswersInProgress).isContactDetailsComplete(CompanyEmailId(0), CompanyPhoneId(0)) mustBe Some(false)
      }
    }

    "isAnswerComplete" must {
      "return None when answer is missing" in {
        UserAnswers(userAnswersUninitiated).isAnswerComplete(IsCompanyDormantId(0)) mustBe None
      }

      "return Some(true) when answer is present" in {
        UserAnswers(userAnswersCompleted).isAnswerComplete(IsCompanyDormantId(0)) mustBe Some(true)
      }
    }

    "isAnswerComplete for yes no answers" must {
      "return None when answer is missing" in {
        UserAnswers(userAnswersUninitiated).isAnswerComplete(DirectorHasNINOId(0, 0),
          DirectorEnterNINOId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe None
      }

      "return Some(true) when answer for yes value is present" in {
        UserAnswers(userAnswersCompleted).isAnswerComplete(DirectorHasNINOId(0, 0),
          DirectorEnterNINOId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe Some(true)
      }

      "return Some(true) when answer for no - reason is present" in {
        UserAnswers(userAnswersCompleted).isAnswerComplete(TrusteeHasNINOId(1),
          TrusteeEnterNINOId(1), Some(TrusteeNoNINOReasonId(1))) mustBe Some(true)
      }

      "return Some(true) when has value is false and reason is not needed" in {
        UserAnswers(userAnswersCompleted).isAnswerComplete(tc.HasCompanyVATId(0),
          tc.CompanyEnterVATId(0), None) mustBe Some(true)
      }

      "return Some(false) when answer is missing" in {
        UserAnswers(userAnswersInProgress).isAnswerComplete(DirectorHasNINOId(0, 0),
          DirectorEnterNINOId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe Some(false)
      }
    }
  }

  "Establisher Company completion status should be returned correctly" when {
    "isEstablisherCompanyDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyDetailsComplete(0, mode) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherCompanyDetailsComplete(0, mode) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyDetailsComplete(0, mode) mustBe Some(false)
      }
    }

    "isEstablisherCompanyAddressComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyAddressComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherCompanyAddressComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyAddressComplete(0) mustBe Some(false)
      }
    }

    "isEstablisherCompanyContactDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyContactDetailsComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherCompanyContactDetailsComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyContactDetailsComplete(0) mustBe Some(false)
      }
    }

    "isEstablisherCompanyComplete" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyComplete(0, mode) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherCompanyComplete(0, mode) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyComplete(0, mode) mustBe false
      }
    }


    "isDirectorComplete" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isDirectorComplete(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isDirectorComplete(0, 0) mustBe false
      }
    }
  }

  "Trustee Company completion status should be returned correctly" when {
    "isTrusteeCompanyDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyDetailsComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeCompanyDetailsComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyDetailsComplete(0) mustBe Some(false)
      }
    }

    "isTrusteeCompanyAddressComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyAddressComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeCompanyAddressComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyAddressComplete(0) mustBe Some(false)
      }
    }

    "isTrusteeCompanyContactDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyContactDetailsComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeCompanyContactDetailsComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyContactDetailsComplete(0) mustBe Some(false)
      }
    }

    "isTrusteeCompanyComplete" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyComplete(0) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeCompanyComplete(0) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyComplete(0) mustBe false
      }
    }

  }

  "Trustee Individual completion status should be returned correctly" when {

    "isTrusteeIndividualComplete" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeIndividualComplete(1) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeIndividualComplete(1) mustBe false
      }
    }

    "isTrusteeIndividualDetailsComplete" must {
      "return None when no answers are present" in {
        emptyAnswers.isTrusteeIndividualDetailsComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        userAnswersIndividualDetailsCompleted.isTrusteeIndividualDetailsComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        userAnswersIndividualDetailsInProgress.isTrusteeIndividualDetailsComplete(0) mustBe Some(false)
      }
    }

    "isTrusteeIndividualAddressComplete" must {
      "return None when no answers are present" in {
        emptyAnswers.isTrusteeIndividualAddressComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        userAnswersAddressDetailsCompleted.isTrusteeIndividualAddressComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        userAnswersAddressDetailsInProgress.isTrusteeIndividualAddressComplete(0) mustBe Some(false)
      }
    }

    "isTrusteeIndividualContactDetailsComplete" must {
      "return None when no answers are present" in {
        emptyAnswers.isTrusteeIndividualContactDetailsComplete(0) mustBe None
      }

      "return Some(true) when all answers are present" in {
        userAnswersContactDetailsCompleted.isTrusteeIndividualContactDetailsComplete(0) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        userAnswersContactDetailsInProgress.isTrusteeIndividualContactDetailsComplete(0) mustBe Some(false)
      }
    }
  }

  "Trustee Partnership completion status should be returned correctly" when {
    "isTrusteePartnershipDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipDetailsComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteePartnershipDetailsComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipDetailsComplete(2) mustBe Some(false)
      }
    }

    "isTrusteePartnershipAddressComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipAddressComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteePartnershipAddressComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipAddressComplete(2) mustBe Some(false)
      }
    }

    "isTrusteePartnershipContactDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipContactDetailsComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteePartnershipContactDetailsComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipContactDetailsComplete(2) mustBe Some(false)
      }
    }

    "isTrusteePartnershipComplete " must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipComplete(2) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteePartnershipComplete(2) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipComplete(2) mustBe false
      }
    }
  }

  "isBeforeYouStartCompleted" must {
    "return true when all the answers are completed for Single Trust" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.SingleTrust).establishedCountry("GB").
        declarationDuties(true)
      answers.isBeforeYouStartCompleted(NormalMode) mustBe true
    }

    "return true when all the answers are completed for UpdateMode" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.GroupLifeDeath).establishedCountry("GB")
      answers.isBeforeYouStartCompleted(UpdateMode) mustBe true
    }

    "return true when all the answers are completed for Group LIfe" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.GroupLifeDeath).establishedCountry("GB").
        declarationDuties(true).haveAnyTrustees(true)
      answers.isBeforeYouStartCompleted(NormalMode) mustBe true
    }

    "return false when have any trustees not answered for Group LIfe" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.GroupLifeDeath).establishedCountry("GB").
        declarationDuties(true)
      answers.isBeforeYouStartCompleted(NormalMode) mustBe false
    }

    "return false when not all the answers are completed for Normal Mode" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.SingleTrust).establishedCountry("GB")
      answers.isBeforeYouStartCompleted(NormalMode) mustBe false
    }

    "return false when all the answers are not completed for UpdateMode" in {
      val answers = UserAnswers().schemeName("name").schemeType(SchemeType.SingleTrust)
      answers.isBeforeYouStartCompleted(UpdateMode) mustBe false
    }
  }

  "isMembersCompleted" must {
    "return true when all the answers are completed" in {
      val answers = UserAnswers().currentMembers(Members.One).futureMembers(Members.One)
      answers.isMembersCompleted.value mustBe true
    }

    "return false when all answers not completed" in {
      val answers = UserAnswers().currentMembers(Members.One)
      answers.isMembersCompleted.value mustBe false
    }

    "return None when there is no data" in {
      UserAnswers().isMembersCompleted mustBe None
    }
  }

  "isBankDetailsCompleted" must {
    "return true when all the answers are completed with no uk bank account" in {
      val answers = UserAnswers().ukBankAccount(false)
      answers.isBankDetailsCompleted.value mustBe true
    }

    "return true when all the answers are completed with uk bank account" in {
      val answers = UserAnswers().ukBankAccount(true).
        bankAccountDetails(BankAccountDetails(SortCode("12", "34", "56"), "no"))
      answers.isBankDetailsCompleted.value mustBe true
    }

    "return false when bank details are not provided" in {
      val answers = UserAnswers().ukBankAccount(true)
      answers.isBankDetailsCompleted.value mustBe false
    }

    "return None when there is no data" in {
      UserAnswers().isBankDetailsCompleted mustBe None
    }
  }

  "isBenefitsAndInsuranceCompleted" must {
    "return true when all the answers are completed with benefits secured by insurance" in {
      val answers = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
        typeOfBenefits(TypeOfBenefits.MoneyPurchase).moneyPurchaseBenefits(Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Collective))
        .benefitsSecuredByInsurance(true).insuranceCompanyName("test name").
        insurancePolicyNumber("112").insurerConfirmAddress(Address("a", "b", None, None, None, "GB"))
      answers.isBenefitsAndInsuranceCompleted.value mustBe true
    }

    "return true when all the answers are completed with no benefits secured by insurance" in {
      val answers = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
        typeOfBenefits(TypeOfBenefits.Defined).benefitsSecuredByInsurance(false)
      answers.isBenefitsAndInsuranceCompleted.value mustBe true
    }

    "return false when benefits are mixed but money purchase benefits are not defined if tcmp toggle is true" in {
      val answers = UserAnswers().set(TcmpToggleId)(true).get.investmentRegulated(true).occupationalPensionScheme(true).
        typeOfBenefits(TypeOfBenefits.MoneyPurchaseDefinedMix).benefitsSecuredByInsurance(false)
      answers.isBenefitsAndInsuranceCompleted.value mustBe false
    }

    "return false when insurance details are not provided" in {
      val answers = UserAnswers().investmentRegulated(true).occupationalPensionScheme(true).
        typeOfBenefits(TypeOfBenefits.MoneyPurchase).benefitsSecuredByInsurance(true)
      answers.isBenefitsAndInsuranceCompleted.value mustBe false
    }

    "return false when not all answers are completed" in {
      val answers = UserAnswers().investmentRegulated(true)
      answers.isBenefitsAndInsuranceCompleted.value mustBe false
    }

    "return None when there is no data" in {
      UserAnswers().isBenefitsAndInsuranceCompleted mustBe None
    }
  }

  "isWorkingKnowledgeCompleted" must {
    "return true when user has working knowledge" in {
      val answers = UserAnswers().declarationDuties(haveWorkingKnowledge = true)
      answers.isWorkingKnowledgeCompleted.value mustBe true
    }

    "return true when user don't have working knowledge but have an adviser" in {
      val answers = UserAnswers().declarationDuties(haveWorkingKnowledge = false).adviserName("Test Adviser").
        adviserEmailAddress("s@s.com").adviserPhone("123").advisersAddress(Address("a", "b", None, None, None, "GB"))
      answers.isWorkingKnowledgeCompleted.value mustBe true
    }

    "return false when user don't have working knowledge and don't have adviser details completed" in {
      val answers = UserAnswers().declarationDuties(haveWorkingKnowledge = false).adviserName("Test Adviser")
      answers.isWorkingKnowledgeCompleted.value mustBe false
    }

    "return None when there is no data" in {
      UserAnswers().isWorkingKnowledgeCompleted mustBe None
    }
  }
}

object DataCompletionSpec extends JsonFileReader with DataCompletionHelper {
  private val mode = NormalMode
  private val userAnswersCompleted: JsValue = readJsonFromFile("/payload.json")
  private val userAnswersInProgress: JsValue = readJsonFromFile("/payloadInProgress.json")

  private val userAnswersUninitiated: JsValue = readJsonFromFile("/payloadUninitiated.json")

  private val userAnswersIndividualDetailsCompleted: UserAnswers = setTrusteeCompletionStatusIndividualDetails(isComplete = true)
  private val userAnswersIndividualDetailsInProgress: UserAnswers = setTrusteeCompletionStatusIndividualDetails(isComplete = false)

  private val userAnswersAddressDetailsCompleted: UserAnswers = setTrusteeCompletionStatusAddressDetails(isComplete = true)
  private val userAnswersAddressDetailsInProgress: UserAnswers = setTrusteeCompletionStatusAddressDetails(isComplete = false)

  private val userAnswersContactDetailsCompleted: UserAnswers = setTrusteeCompletionStatusContactDetails(isComplete = true)
  private val userAnswersContactDetailsInProgress: UserAnswers = setTrusteeCompletionStatusContactDetails(isComplete = false)

  private val emptyAnswers = UserAnswers()
}
