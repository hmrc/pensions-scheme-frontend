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

package identifiers

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.{AnswerRow, Message}

class MoneyPurchaseBenefitsIdSpec extends SpecBase with Enumerable.Implicits {

  val onwardUrl = "onwardUrl"
  val name = "schemeName"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__moneyPurchaseBenefits__cya", name),Seq(messages("messages__moneyPurchaseBenefits__02")), false,
      Some(Link("site.change",onwardUrl, Some(Message("messages__moneyPurchaseBenefits__cya_hidden", name)))))
  )

  private val addRow = Seq(AnswerRow(Message("messages__moneyPurchaseBenefits__cya", name), Seq("site.not_entered"), answerIsMessageKey = true,
    Some(Link("site.add", onwardUrl, Some(Message("messages__moneyPurchaseBenefits__cya_hidden", name))))))

  val answers: UserAnswers = UserAnswers().set(SchemeNameId)(name).flatMap(
    _.set(MoneyPurchaseBenefitsId)(MoneyPurchaseBenefits.CashBalance)).asOpt.get

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        MoneyPurchaseBenefitsId.row(onwardUrl, NormalMode)(request,implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode " must {

      "return answers rows with links if question is answered" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        MoneyPurchaseBenefitsId.row(onwardUrl, UpdateMode)(request,implicitly) must equal(answerRowsWithChangeLinks)
      }

      "return answers rows with add link if question is unanswered and toggle is on" in {
        val answers: UserAnswers = UserAnswers().set(SchemeNameId)(name).flatMap(
          _.set(TypeOfBenefitsId)(TypeOfBenefits.MoneyPurchaseDefinedMix)).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        MoneyPurchaseBenefitsId.row(onwardUrl, UpdateMode)(request,implicitly) must equal(addRow)
      }

      "return empty answers row if question is unanswered and toggle is off" in {
        val answers: UserAnswers = UserAnswers().set(SchemeNameId)(name).flatMap(
          _.set(TypeOfBenefitsId)(TypeOfBenefits.MoneyPurchaseDefinedMix)).asOpt.get
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        MoneyPurchaseBenefitsId.row(onwardUrl, UpdateMode)(request,implicitly) must equal(Nil)
      }
    }

  }
}
