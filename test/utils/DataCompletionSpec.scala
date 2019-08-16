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

package utils

import base.JsonFileReader
import helpers.DataCompletionHelper
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorHasNINOId, DirectorNewNinoId, DirectorNoNINOReasonId}
import models.NormalMode
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsValue

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
        DirectorNewNinoId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe None
    }

    "return Some(true) when answer is present" in {
      UserAnswers(userAnswersCompleted).isAnswerComplete(DirectorHasNINOId(0, 0),
        DirectorNewNinoId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe Some(true)
    }

    "return Some(false) when answer is missing" in {
      UserAnswers(userAnswersInProgress).isAnswerComplete(DirectorHasNINOId(0, 0),
        DirectorNewNinoId(0, 0), Some(DirectorNoNINOReasonId(0, 0))) mustBe Some(false)
    }
  }

  "isUtrComplete" must {
    "return None when answer is missing" in {
      UserAnswers(userAnswersUninitiated).isUtrComplete(HasCompanyUTRId(0),
        CompanyUTRId(0), NoCompanyUTRId(0)) mustBe None
    }

    "return Some(true) when answer is present" in {
      UserAnswers(userAnswersCompleted).isUtrComplete(HasCompanyUTRId(0),
        CompanyUTRId(0), NoCompanyUTRId(0)) mustBe Some(true)
    }

    "return Some(false) when answer is missing" in {
      UserAnswers(userAnswersInProgress).isUtrComplete(HasCompanyUTRId(0),
        CompanyUTRId(0), NoCompanyUTRId(0)) mustBe Some(false)
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

    "isEstablisherCompanyComplete with hns toggle on" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyComplete(0, mode, true) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherCompanyComplete(0, mode, true) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyComplete(0, mode, true) mustBe false
      }
    }

    "isEstablisherCompanyComplete with hns toggle off" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherCompanyComplete(0, mode, false) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompletedNonHnS).isEstablisherCompanyComplete(0, mode, false) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherCompanyComplete(0, mode, false) mustBe false
      }
    }

    "isDirectorCompleteNonHnS" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompletedNonHnS).isDirectorCompleteNonHnS(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isDirectorCompleteNonHnS(0, 0) mustBe false
      }
    }

    "isDirectorCompleteHnS" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isDirectorCompleteHnS(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isDirectorCompleteHnS(0, 0) mustBe false
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

    "isTrusteeCompanyComplete with hns toggle on" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyComplete(0, true) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeCompanyComplete(0, true) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyComplete(0, true) mustBe false
      }
    }

    "isTrusteeCompanyComplete with hns toggle off" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteeCompanyComplete(0, false) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompletedNonHnS).isTrusteeCompanyComplete(0, false) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeCompanyComplete(0, false) mustBe false
      }
    }
  }

  "Trustee Individual completion status should be returned correctly" when {
    "isTrusteeIndividualComplete H&S disabled" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompletedNonHnS).isTrusteeIndividualComplete(isHnSEnabled = false, 1) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeIndividualComplete(isHnSEnabled = false, 1) mustBe false
      }
    }

    "isTrusteeIndividualComplete H&S enabled" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteeIndividualComplete(isHnSEnabled = true, 1) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteeIndividualComplete(isHnSEnabled = true, 1) mustBe false
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

    "isTrusteePartnershipComplete with hns toggle on" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipComplete(2, true) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isTrusteePartnershipComplete(2, true) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipComplete(2, true) mustBe false
      }
    }

    "isTrusteePartnershipComplete with hns toggle off" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isTrusteePartnershipComplete(2, false) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompletedNonHnS).isTrusteePartnershipComplete(2, false) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isTrusteePartnershipComplete(2, false) mustBe false
      }
    }
  }
}

object DataCompletionSpec extends JsonFileReader with DataCompletionHelper  {
  private val mode = NormalMode
  private val userAnswersCompleted: JsValue = readJsonFromFile("/payloadHnS.json")
  private val userAnswersInProgress: JsValue = readJsonFromFile("/payloadHnSInProgress.json")

  private val userAnswersCompletedNonHnS: JsValue = readJsonFromFile("/payload.json")
  private val userAnswersUninitiated: JsValue = readJsonFromFile("/payloadHnSUninitiated.json")

  private val userAnswersIndividualDetailsCompleted: UserAnswers = setTrusteeCompletionStatusIndividualDetails(isComplete = true, toggled = true)
  private val userAnswersIndividualDetailsInProgress: UserAnswers = setTrusteeCompletionStatusIndividualDetails(isComplete = false, toggled = true)

  private val userAnswersAddressDetailsCompleted: UserAnswers = setTrusteeCompletionStatusAddressDetails(isComplete = true, toggled = true)
  private val userAnswersAddressDetailsInProgress: UserAnswers = setTrusteeCompletionStatusAddressDetails(isComplete = false, toggled = true)

  private val userAnswersContactDetailsCompleted: UserAnswers = setTrusteeCompletionStatusContactDetails(isComplete = true, toggled = true)
  private val userAnswersContactDetailsInProgress: UserAnswers = setTrusteeCompletionStatusContactDetails(isComplete = false, toggled = true)

  private val emptyAnswers = UserAnswers()
}
