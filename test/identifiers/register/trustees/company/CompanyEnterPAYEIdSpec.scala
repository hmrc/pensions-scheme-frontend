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

package identifiers.register.trustees.company

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

class CompanyEnterPAYEIdSpec extends SpecBase {

  private val companyName = "name"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__common__cya__paye", companyName),
      List("paye"),false,Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_paye", companyName)))))
  )

  "cya" when {

    val onwardUrl = "onwardUrl"

    val answers: UserAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(companyName)).flatMap(
      _.set(CompanyEnterPAYEId(0))(ReferenceValue("paye"))).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        CompanyEnterPAYEId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - company paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        CompanyEnterPAYEId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        CompanyEnterPAYEId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__paye", companyName), List("paye"),false,None)
        ))
      }

      "return answers rows with change links if paye is available and editable" in {

        val answers: UserAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(companyName)).flatMap(
          _.set(CompanyEnterPAYEId(0))(ReferenceValue("paye", true))).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        CompanyEnterPAYEId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if no answer if found" in {

        val answers: UserAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(companyName)).asOpt.get

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        CompanyEnterPAYEId(0).row(onwardUrl, CheckUpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__paye", companyName), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_paye", companyName)))))))
      }
    }
  }
}
