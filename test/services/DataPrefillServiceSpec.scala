/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import base.SpecBase
import matchers.JsonMatchers
import models.prefill.IndividualDetails
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import utils.{Enumerable, UaJsValueGenerators, UserAnswers}
import java.time.LocalDate

class DataPrefillServiceSpec extends SpecBase with JsonMatchers with Enumerable.Implicits with UaJsValueGenerators {
  private val dataPrefillService = new DataPrefillService()

  "copyAllDirectorsToTrustees" must {
    "copy all the selected directors to trustees" in {
      forAll(uaJsValueWithNino) {
        ua => {
          val result = dataPrefillService.copyAllDirectorsToTrustees(UserAnswers(ua), Seq(1), 0)
          val path = result.json \ "trustees"
          (path \ 0 \ "nino" \ "value").as[String] mustBe "CS700100A"
          (path \ 0 \ "trusteeDetails" \ "firstName").as[String] mustBe "Test"
          (path \ 0 \ "trusteeDetails" \ "lastName").as[String] mustBe "User 1"
        }
      }
    }
  }

  "copyAllTrusteesToDirectors" must {
    "copy all the selected trustees to directors" in {
      forAll(uaJsValueWithNoNino) {
        ua => {
          val result = dataPrefillService.copyAllTrusteesToDirectors(UserAnswers(ua), Seq(1), 0)
          val path = result.json \ "establishers" \ 0
          (path \ "director" \ 3 \ "directorDetails" \ "firstName").as[String] mustBe "Test"
          (path \ "director" \ 3 \ "directorDetails" \ "lastName").as[String] mustBe "User 4"
        }
      }
    }
  }

  "getListOfDirectors" must {
    "return the directors which are non deleted, completed and their nino is not matching with any of the existing trustees" in {
      forAll(uaJsValueWithNino) {
        ua => {
          val result = dataPrefillService.getListOfDirectorsToBeCopied(UserAnswers(ua))
          result mustBe Seq(IndividualDetails("Test", "User 3", false, Some("CS700300A"), Some(LocalDate.parse("1999-03-13")), 2, true, Some(0)))
        }
      }
    }

    "return the directors which are non deleted, completed, no nino and their name and dob is not matching with any of the existing trustees" in {
      forAll(uaJsValueWithNoNino) {
        ua => {
          val result = dataPrefillService.getListOfDirectorsToBeCopied(UserAnswers(ua))
          result mustBe Seq(IndividualDetails("Test", "User 3", false, None, Some(LocalDate.parse("1999-03-13")), 2, true, Some(0)))
        }
      }
    }
  }

  "getListOfTrusteesToBeCopied" must {
    "return the trustees which are non deleted, completed, no nino and their name and dob is not matching with any of the existing directors" in {
      forAll(uaJsValueWithNoNino) {
        ua => {
          val result = dataPrefillService.getListOfTrusteesToBeCopied(0)(UserAnswers(ua))
          result mustBe Seq(IndividualDetails("Test", "User 4", false, Some("CS700400A"), Some(LocalDate.parse("1999-04-13")), 1, true, None))
        }
      }
    }

    "return no trustees when their nino is matching with any of the existing directors" in {
      forAll(uaJsValueWithNino) {
        ua => {
          val result = dataPrefillService.getListOfTrusteesToBeCopied(0)(UserAnswers(ua))
          result mustBe Nil
        }
      }
    }
  }
}





