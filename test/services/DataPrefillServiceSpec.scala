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

package services

import base.SpecBase
import identifiers.register.establishers.company.director.DirectorPhoneNumberId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteeKindId}
import matchers.JsonMatchers
import models.prefill.IndividualDetails
import models.register.trustees.TrusteeKind
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.{JsArray, Json}
import services.DataPrefillService.DirectorIdentifier
import utils.{Enumerable, UaJsValueGenerators, UserAnswers}

import java.time.LocalDate

class DataPrefillServiceSpec extends SpecBase with JsonMatchers with Enumerable.Implicits with UaJsValueGenerators {
  private val dataPrefillService = new DataPrefillService()

  "copySelectedDirectorsToTrustees" must {
    "append all the selected directors from two establishers to trustees excluding deleted directors and remove " +
      "(clean) trustees which consist only of kind nodes (i.e. user been to trustee kind page but gone no further)" in {
      forAll(uaJsValueWithNoNinoTwoTrusteesTwoEstablishersThreeDirectorsEach) {
        ua =>
          val userAnswers =
            UserAnswers(ua)
              .setOrException(TrusteeKindId(2))(TrusteeKind.Individual)
              .setOrException(IsTrusteeNewId(2))(true)

          val result = dataPrefillService.copySelectedDirectorsToTrustees(
            userAnswers,
            Seq(
              DirectorIdentifier(establisherIndex = 0, directorIndex = 2),
              DirectorIdentifier(establisherIndex = 1, directorIndex = 0)
            )
          )

          val path = result.json \ "trustees"

          val actualUA = UserAnswers(ua)

          (path \ 0 \ "trusteeNino" \ "value").asOpt[String] mustBe None
          (path \ 0 \ "trusteeDetails" \ "firstName").as[String] mustBe "Test"
          (path \ 0 \ "trusteeDetails" \ "lastName").as[String] mustBe "User 7"
          (path \ 0 \ "trusteeContactDetails" \ "emailAddress").as[String] mustBe "aaa@gmail.com"


          (path \ 1 \ "trusteeNino" \ "value").asOpt[String] mustBe None
          (path \ 1 \ "trusteeDetails" \ "firstName").as[String] mustBe "Test"
          (path \ 1 \ "trusteeDetails" \ "lastName").as[String] mustBe "User 8"
          (path \ 1 \ "trusteeContactDetails" \ "emailAddress").as[String] mustBe "aaa@gmail.com"

          (path \ 2 \ "trusteeNino" \ "value").asOpt[String] mustBe None
          (path \ 2 \ "trusteeDetails" \ "firstName").as[String] mustBe "Test"
          (path \ 2 \ "trusteeDetails" \ "lastName").as[String] mustBe "User 3"
          (path \ 2 \ "trusteeContactDetails" \ "emailAddress").as[String] mustBe "aaa@gmail.com"
          (path \ 2 \ "trusteeContactDetails" \ "phoneNumber").as[String] mustBe actualUA.get(DirectorPhoneNumberId(0, 2)).getOrElse("")

          (path \ 3 \ "trusteeNino" \ "value").asOpt[String] mustBe None
          (path \ 3 \ "trusteeDetails" \ "firstName").as[String] mustBe "Test"
          (path \ 3 \ "trusteeDetails" \ "lastName").as[String] mustBe "User 4"
          (path \ 3 \ "trusteeContactDetails" \ "emailAddress").as[String] mustBe "aaa@gmail.com"
          (path \ 3 \ "trusteeContactDetails" \ "phoneNumber").as[String] mustBe actualUA.get(DirectorPhoneNumberId(1, 0)).getOrElse("")
      }
    }
  }

  "cleanTrustees" must {
    "remove any trustee nodes which contain only the trustee kind/ director also trustee elements" in {
      val trusteesJsArray =
        Json.parse(
          """
            |        [
            |            {
            |                "isTrusteeNew" : true,
            |                "trusteeKind" : "individual",
            |                "trusteeDetails" : {
            |                    "firstName" : "asas",
            |                    "lastName" : "asa",
            |                    "isDeleted" : true
            |                }
            |            },
            |            {
            |                "isTrusteeNew" : true,
            |                "trusteeKind" : "individual"
            |            },
            |            {
            |                "isTrusteeNew" : true,
            |                "trusteeKind" : "individual"
            |            }
            |        ]
            |""".stripMargin).as[JsArray]

      val expectedResultJsArray =
        Json.parse(
          """
            |        [
            |            {
            |                "isTrusteeNew" : true,
            |                "trusteeKind" : "individual",
            |                "trusteeDetails" : {
            |                    "firstName" : "asas",
            |                    "lastName" : "asa",
            |                    "isDeleted" : true
            |                }
            |            }
            |        ]
            |""".stripMargin).as[JsArray]

      dataPrefillService.cleanTrustees(trusteesJsArray) mustBe expectedResultJsArray
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

    "return the directors which are non deleted from TWO establishers with 3 directors (1 deleted) each" in {
      forAll(uaJsValueWithNoNinoTwoTrusteesTwoEstablishersThreeDirectorsEach) {
        ua => {
          val result = dataPrefillService.getListOfDirectorsToBeCopied(UserAnswers(ua))
          result mustBe Seq(
            IndividualDetails("Test", "User 1", false, None, Some(LocalDate.parse("1999-01-13")), 0, true, Some(0)),
            IndividualDetails("Test", "User 3", false, None, Some(LocalDate.parse("1999-03-13")), 2, true, Some(0)),
            IndividualDetails("Test", "User 4", false, None, Some(LocalDate.parse("1999-04-13")), 0, true, Some(1)),
            IndividualDetails("Test", "User 6", false, None, Some(LocalDate.parse("1999-06-13")), 2, true, Some(1))
          )
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

    "return the trustees which are non deleted, completed, no nino and their name and dob is not matching with any of the existing directors " +
      "and more than one" in {
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





