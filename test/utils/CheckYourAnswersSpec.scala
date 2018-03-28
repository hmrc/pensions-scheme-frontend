/*
 * Copyright 2018 HM Revenue & Customs
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

import identifiers.TypedIdentifier
import models.register.establishers.individual.UniqueTaxReference
import models.requests.DataRequest
import models.{CheckMode, CompanyDetails, CompanyRegistrationNumber, Index}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.CheckYourAnswers.Ops._
import viewmodels.AnswerRow

class CheckYourAnswersSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  val onwardUrl = "onwardUrl"

  def testIdentifier[A]: TypedIdentifier[A] = new TypedIdentifier[A] {
    override def toString = "testId"
  }

  "CheckYourAnswers" must {

    "produce row of answers" when {

      "string" in {

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> "value")))

        testIdentifier[String].row(onwardUrl) must equal(Seq(AnswerRow("testId.checkYourAnswersLabel", Seq("value"), false, onwardUrl)))

      }

      "companyDetails" when {

        "only name exists" in {

          val companyDetails = CompanyDetails("Company Name", None, None)

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )))

          testIdentifier[CompanyDetails].row("onwardUrl") must equal(Seq(AnswerRow(
            "messages__common__cya__name",
            Seq(companyDetails.companyName),
            false,
            onwardUrl
          )))

        }
        "vat number exists" in {

          val companyDetails = CompanyDetails("Company Name", Some("VAT123"), None)

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__company__cya__vat",
              Seq(s"${companyDetails.vatNumber.get}"),
              false,
              onwardUrl
            )))

        }
        "paye ref exists" in {

          val companyDetails = CompanyDetails("Company Name", None, Some("PAYE/123"))

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__company__cya__paye_ern",
              Seq(s"${companyDetails.payeNumber.get}"),
              false,
              onwardUrl
            )))

        }

        "all values exist" in {

          val companyDetails = CompanyDetails("Company Name", Some("VAT123"), Some("PAYE/123"))

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__company__cya__vat",
              Seq(s"${companyDetails.vatNumber.get}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__company__cya__paye_ern",
              Seq(s"${companyDetails.payeNumber.get}"),
              false,
              onwardUrl
            )))

        }
      }

      "CRN" when {

        "yes" in {

          val crn = CompanyRegistrationNumber.Yes("0987654")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> crn)))

          testIdentifier[CompanyRegistrationNumber].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__company__cya__crn_yes_no",
              Seq(s"${CompanyRegistrationNumber.Yes}"),
              true,
              onwardUrl
            ),
            AnswerRow(
              "messages__common__crn",
              Seq(s"${crn.crn}"),
              true,
              onwardUrl
            )))
        }
        "no" in {

          val crn = CompanyRegistrationNumber.No("Not sure")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> crn)))

          testIdentifier[CompanyRegistrationNumber].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__company__cya__crn_yes_no",
              Seq(s"${CompanyRegistrationNumber.No}"),
              true,
              onwardUrl
            ),
            AnswerRow(
              "messages__company__cya__crn_no_reason",
              Seq(s"${crn.reason}"),
              true,
              onwardUrl
            )
          ))

        }
      }

      "UTR" when {

        "yes" in {

          val utr = UniqueTaxReference.Yes("7654321244")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> utr)))

          testIdentifier[UniqueTaxReference].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__establisher_individual_utr_question_cya_label",
              Seq(s"${UniqueTaxReference.Yes}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__establisher_individual_utr_cya_label",
              Seq({utr.utr}),
              false,
              onwardUrl
            )
          ))

        }

        "no" in {

          val utr = UniqueTaxReference.No("Not sure")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> utr)))

          testIdentifier[UniqueTaxReference].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__establisher_individual_utr_question_cya_label",
              Seq(s"${UniqueTaxReference.No}"),
              false,
              onwardUrl
            ),
            AnswerRow(
              "messages__establisher_individual_utr_reason_cya_label",
              Seq(utr.reason),
              false,
              onwardUrl
            )))

        }

      }

    }

  }

}