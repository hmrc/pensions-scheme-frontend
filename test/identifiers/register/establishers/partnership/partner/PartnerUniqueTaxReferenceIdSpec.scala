/*
 * Copyright 2019 HM Revenue & Customs
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
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import viewmodels.AnswerRow
import utils.checkyouranswers.Ops._

class PartnerUniqueTaxReferenceIdSpec extends SpecBase {

  "cya" when {
    val onwardUrl = "onwardUrl"

    def answers(utr: UniqueTaxReference): UserAnswers = UserAnswers(Json.obj()).set(PartnerUniqueTaxReferenceId(0, 0))(utr).asOpt.value

    val utrYes = UniqueTaxReference.Yes("1111111111")
    val utrNo = UniqueTaxReference.No("Not sure")

    "in normal mode" must {

      "return answers rows with change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrYes), PsaId("A0000000"))
        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__partner_utr_question_cya_label", Seq(s"${UniqueTaxReference.Yes}"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_yes_no")))),
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utrYes.utr), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr"))))
        ))
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrNo), PsaId("A0000000"))
        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__partner_utr_question_cya_label", Seq(s"${UniqueTaxReference.No}"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_yes_no")))),
          AnswerRow(
            "messages__establisher_individual_utr_reason_cya_label", Seq(utrNo.reason), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_no"))))
        ))
      }
    }

    "in update mode for new partners" must {

      def answersNew(utr: UniqueTaxReference): UserAnswers = answers(utr).set(IsNewPartnerId(0, 0))(true).asOpt.value

      "return answers rows with change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(utrYes), PsaId("A0000000"))
        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__partner_utr_question_cya_label", Seq(s"${UniqueTaxReference.Yes}"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_yes_no")))),
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utrYes.utr), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr"))))
        ))
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew(utrNo), PsaId("A0000000"))
        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__partner_utr_question_cya_label", Seq(s"${UniqueTaxReference.No}"), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_yes_no")))),
          AnswerRow(
            "messages__establisher_individual_utr_reason_cya_label", Seq(utrNo.reason), false,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__partner__utr_no"))))
        ))
      }
    }

    "in update mode for existing partners" must {

      "return answers rows without change links for utr with yes" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrYes), PsaId("A0000000"))

        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq(utrYes.utr), false, None)
        ))
      }

      "return answers rows with change links for utr with no" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(utrNo), PsaId("A0000000"))

        PartnerUniqueTaxReferenceId(0, 0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__establisher_individual_utr_cya_label", Seq("site.not_entered"), true, None)
        ))
      }
    }
  }
}
