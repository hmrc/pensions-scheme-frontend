/*
 * Copyright 2021 HM Revenue & Customs
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
import viewmodels.{AnswerRow, Message}

class CompanyEnterVATIdSpec extends SpecBase {

  private val companyName = "test company"
  private def answers(isEditable: Boolean): UserAnswers = UserAnswers()
    .set(CompanyDetailsId(0))(CompanyDetails(companyName))
    .flatMap(
      _.set(CompanyEnterVATId(0))(ReferenceValue("vat", isEditable))
    ).asOpt.value

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__common__cya__vat"),List("vat"),false,Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_vat_number", companyName)))))
  )

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = false), Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyEnterVATId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - company vat" must {

      def answersNew: UserAnswers = answers(isEditable = false).set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        CompanyEnterVATId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company vat" must {

      "return answers rows without change links if vat is available and not editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = false), Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterVATId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__vat"),List("vat"),false, None)
        ))
      }

      "return answers rows with change links if vat is available and editable" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = true), Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterVATId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if vat is not available" in {
        val answers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(companyName)).asOpt.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers

        CompanyEnterVATId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(Message("messages__common__cya__vat"), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_vat_number", companyName)))))))
      }
    }
  }
}
