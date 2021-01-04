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

package identifiers.register.establishers.partnership

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

class PartnershipEnterVATIdSpec extends SpecBase {
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val partnershipName = "test partnership name"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__enterVAT", partnershipName),
      List("vat"), answerIsMessageKey = false, Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_vat_number", partnershipName)))))
  )

  private val ua = UserAnswers()
    .set(PartnershipDetailsId(0))(PartnershipDetails(partnershipName)).asOpt.value

  "cya" when {

    def answers: UserAnswers = ua.set(PartnershipEnterVATId(0))(ReferenceValue("vat")).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        PartnershipEnterVATId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - partnership" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        PartnershipEnterVATId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - partnership" must {

      "return answers rows without change links if vat is available and not editable" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipEnterVATId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(Message("messages__enterVAT", partnershipName),List("vat"), answerIsMessageKey = false, None)
        ))
      }

      "return answers rows with change links if vat is available and editable" in {
        val answers = ua.set(PartnershipEnterVATId(0))(ReferenceValue("vat", isEditable = true)).asOpt.get
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipEnterVATId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if vat is not available" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", ua, Some(PsaId("A0000000")))

        PartnershipEnterVATId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(Message("messages__enterVAT", partnershipName), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_vat_number", partnershipName)))))))
      }
    }
  }
}
