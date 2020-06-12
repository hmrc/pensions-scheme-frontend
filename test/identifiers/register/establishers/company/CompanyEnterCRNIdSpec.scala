/*
 * Copyright 2020 HM Revenue & Customs
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

package identifiers.register.establishers.company

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

class CompanyEnterCRNIdSpec extends SpecBase {

  private val companyName = "the company"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__checkYourAnswers__establishers__company__number"),
      List("companyRegistrationNumber"), false, Some(Link("site.change", onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_crn", Message("messages__theCompany"))))))
  )

  "Cleanup" when {
    def answers: UserAnswers = UserAnswers(Json.obj())
      .set(CompanyNoCRNReasonId(0))("reason").asOpt.value

    "remove the data for `NoCompanyNumber`" in {
      val result: UserAnswers = answers.set(CompanyEnterCRNId(0))(ReferenceValue("crn", true)).asOpt.value
      result.get(CompanyNoCRNReasonId(0)) mustNot be(defined)
    }
  }

  "cya" when {

    def answers: UserAnswers = UserAnswers().set(CompanyEnterCRNId(0))(ReferenceValue("companyRegistrationNumber")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyEnterCRNId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - companyRegistrationNumber" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyEnterCRNId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company companyRegistrationNumber" must {

      "return answers rows without change links if companyRegistrationNumber is available and not editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterCRNId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__checkYourAnswers__establishers__company__number"), List("companyRegistrationNumber"), false, None)
        ))
      }

      "return answers rows with change links if companyRegistrationNumber is available and editable" in {
        val answers = UserAnswers().set(CompanyEnterCRNId(0))(ReferenceValue("companyRegistrationNumber", true)).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterCRNId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if companyRegistrationNumber is not available" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterCRNId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__checkYourAnswers__establishers__company__number"),
            Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_crn", Message("messages__theCompany"))))))))
      }
    }
  }
}
