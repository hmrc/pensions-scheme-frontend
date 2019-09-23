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
import models.NormalMode
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsValue
import utils.{Enumerable, UserAnswers}

class DataCompletionEstablishersSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import DataCompletionEstablishersSpec._

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

    "isDirectorCompleteHnS" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isDirectorCompleteHnS(0, 0) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isDirectorCompleteHnS(0, 0) mustBe false
      }
    }
  }

  "Establisher Partnership completion status should be returned correctly" when {
    "isEstablisherPartnershipDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherPartnershipDetailsComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherPartnershipDetailsComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherPartnershipDetailsComplete(2) mustBe Some(false)
      }
    }

    "isEstablisherPartnershipAddressComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherPartnershipAddressComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherPartnershipAddressComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherPartnershipAddressComplete(2) mustBe Some(false)
      }
    }

    "isEstablisherPartnershipContactDetailsComplete" must {
      "return None when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherPartnershipContactDetailsComplete(2) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherPartnershipContactDetailsComplete(2) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherPartnershipContactDetailsComplete(2) mustBe Some(false)
      }
    }

    "isEstablisherPartnershipComplete with hns toggle on" must {
      "return false when all answers are missing" in {
        UserAnswers(userAnswersUninitiated).isEstablisherPartnershipComplete(2, mode, true) mustBe false
      }

      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherPartnershipComplete(2, mode, true) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherPartnershipComplete(2, mode, true) mustBe false
      }
    }

  }

  "Establisher Individual completion status should be returned correctly" when {
    "isEstablisherIndividualComplete H&S enabled" must {
      "return true when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherIndividualComplete(isHnSEnabled = true, 1) mustBe true
      }

      "return false when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherIndividualComplete(isHnSEnabled = true, 1) mustBe false
      }
    }

    "isEstablisherIndividualDetailsComplete" must {
      "return None when no answers are present" in {
        UserAnswers(userAnswersUninitiated).isEstablisherIndividualDetailsComplete(1) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherIndividualDetailsComplete(1) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherIndividualDetailsComplete(1) mustBe Some(false)
      }
    }

    "isEstablisherIndividualAddressComplete" must {
      "return None when no answers are present" in {
        UserAnswers(userAnswersUninitiated).isEstablisherIndividualAddressComplete(1) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherIndividualAddressComplete(1) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherIndividualAddressComplete(1) mustBe Some(false)
      }
    }

    "isEstablisherIndividualContactDetailsComplete" must {
      "return None when no answers are present" in {
        UserAnswers(userAnswersUninitiated).isEstablisherIndividualContactDetailsComplete(1) mustBe None
      }

      "return Some(true) when all answers are present" in {
        UserAnswers(userAnswersCompleted).isEstablisherIndividualContactDetailsComplete(1) mustBe Some(true)
      }

      "return Some(false) when some answer is missing" in {
        UserAnswers(userAnswersInProgress).isEstablisherIndividualContactDetailsComplete(1) mustBe Some(false)
      }
    }
  }
}

object DataCompletionEstablishersSpec extends JsonFileReader with DataCompletionHelper  {
  private val mode = NormalMode
  private val userAnswersCompleted: JsValue = readJsonFromFile("/payloadHnS.json")
  private val userAnswersInProgress: JsValue = readJsonFromFile("/payloadHnSInProgress.json")
  private val userAnswersUninitiated: JsValue = readJsonFromFile("/payloadHnSUninitiated.json")

  private val emptyAnswers = UserAnswers()
}
