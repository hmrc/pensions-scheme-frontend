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
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

class CompanyEnterPAYEIdSpec extends SpecBase {

  private def answers(isEditable: Boolean) =
    UserAnswers()
      .set(CompanyDetailsId(0))(CompanyDetails(companyName))
      .flatMap(
        _.set(CompanyEnterPAYEId(0))(ReferenceValue("paye", isEditable))
      )
      .asOpt
      .value

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val companyName                     = "test company"
  val onwardUrl                               = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      "messages__common__cya__paye",
      List("paye"),
      false,
      Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_paye", companyName))))
    )
  )

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = false), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers         = request.userAnswers
        CompanyEnterPAYEId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - company paye" must {

      def answersNew: UserAnswers = answers(isEditable = false).set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers         = request.userAnswers
        CompanyEnterPAYEId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = false), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers         = request.userAnswers

        CompanyEnterPAYEId(0).row(onwardUrl, UpdateMode) must equal(
          Seq(
            AnswerRow("messages__common__cya__paye", List("paye"), false, None)
          ))
      }

      "return answers rows with change links if paye is available and editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = true), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers         = request.userAnswers

        CompanyEnterPAYEId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if no answer is found" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(), PsaId("A0000000"))
        implicit val userAnswers: UserAnswers         = request.userAnswers

        CompanyEnterPAYEId(0).row(onwardUrl, CheckUpdateMode) must equal(
          Seq(AnswerRow(
            "messages__common__cya__paye",
            Seq("site.not_entered"),
            answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_paye", "the company"))))
          )))
      }
    }
  }
}
