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
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.{DirectorHasNINOId, DirectorNewNinoId, DirectorNoNINOReasonId}
import identifiers.register.trustees.individual.{TrusteeHasUTRId, TrusteeNoUTRReasonId, TrusteeUTRId}
import models.NormalMode
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsValue

class DataCompletionSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import DataCompletionSpec._

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

  // TRUSTEE INDIVIDUAL

  "isTrusteeIndividualCompleteNonHnS" must {
    "return true when all answers are present" in {
      UserAnswers(userAnswersCompletedNonHnS).isTrusteeIndividualCompleteNonHnS(0) mustBe true
    }

    "return false when some answer is missing" in {
      UserAnswers(userAnswersInProgress).isTrusteeIndividualCompleteNonHnS(0) mustBe false
    }
  }

  "isTrusteeIndividualCompleteHnS" must {
    "return true when all answers are present" in {
      UserAnswers(userAnswersCompleted).isTrusteeIndividualCompleteHnS(0) mustBe true
    }

    "return false when some answer is missing" in {
      UserAnswers(userAnswersInProgress).isTrusteeIndividualCompleteHnS(0) mustBe false
    }
  }
}

object DataCompletionSpec extends JsonFileReader {

  val mode = NormalMode
  val userAnswersCompleted: JsValue = readJsonFromFile("/payloadHnS.json")
  val userAnswersInProgress: JsValue = readJsonFromFile("/payloadHnSInProgress.json")

  val userAnswersCompletedNonHnS: JsValue = readJsonFromFile("/payload.json")
  val userAnswersUninitiated: JsValue = readJsonFromFile("/payloadHnSUninitiated.json")
}
