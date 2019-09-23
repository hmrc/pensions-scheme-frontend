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

package utils.datacompletion

import base.JsonFileReader
import helpers.DataCompletionHelper
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorHasNINOId, DirectorNewNinoId, DirectorNoNINOReasonId}
import identifiers.register.trustees.individual.{TrusteeHasNINOId, TrusteeNewNinoId, TrusteeNoNINOReasonId}
import identifiers.register.trustees.{company => tc}
import models.NormalMode
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsValue
import utils.{Enumerable, UserAnswers}

class DataCompletionTrusteesSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import DataCompletionTrusteesSpec._

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

object DataCompletionTrusteesSpec extends JsonFileReader with DataCompletionHelper  {
  
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
