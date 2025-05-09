/*
 * Copyright 2024 HM Revenue & Customs
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

package identifiers.register.establishers.partnership.partner

import base.SpecBase
import models.*
import models.person.PersonName
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops.*
import utils.{CountryOptions, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnerEnterNINOIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val partnerName = PersonName("test", "partner")
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__enterNINO", partnerName.fullName), List("nino"),false,Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_national_insurance_number", partnerName.fullName)))))
  )

  "cya" when {

    def answers: UserAnswers = UserAnswers().
      partnerName(firstIndex = 0, secondIndex = 0, partnerName)
      .set(PartnerEnterNINOId(0, 0))(ReferenceValue("nino")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnerEnterNINOId(0, 0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new partner -  nino" must {

      def answersNew: UserAnswers = answers.set(IsNewPartnerId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        PartnerEnterNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing partner nino" must {

      "return answers rows without change links if nino is available and not editable" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnerEnterNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(Message("messages__enterNINO", partnerName.fullName), List("nino"),false, None)
        ))
      }

      "return answers rows with change links if nino is available and editable" in {
        val answers = UserAnswers().partnerName(firstIndex = 0, secondIndex = 0, partnerName).
          set(PartnerEnterNINOId(0, 0))(ReferenceValue("nino", isEditable = true)).asOpt.get
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnerEnterNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if nino is not available" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          UserAnswers().partnerName(firstIndex = 0, secondIndex = 0, partnerName), Some(PsaId("A0000000")))


        PartnerEnterNINOId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(Message("messages__enterNINO", partnerName.fullName), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_national_insurance_number", partnerName.fullName)))))))
      }
    }
  }
}
