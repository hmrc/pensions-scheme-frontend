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

package identifiers.register.establishers.partnership.partner

import base.SpecBase
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, ReferenceValue, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class PartnerHasUTRIdSpec extends SpecBase {
  import PartnerHasUTRIdSpec._

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(PartnerHasUTRId(0, 0))(hasUtr)
      .flatMap(_.set(PartnerEnterUTRId(0, 0))(ReferenceValue("test-utr")))
      .flatMap(_.set(PartnerNoUTRReasonId(0, 0))("reason"))
      .asOpt.value

    "`PartnerHasUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(PartnerHasUTRId(0, 0))(false).asOpt.value

      "remove the data for `PartnerUTR`" in {
        result.get(PartnerEnterUTRId(0, 0)) mustNot be(defined)
      }
    }

    "`PartnerHasUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(PartnerHasUTRId(0, 0))(true).asOpt.value

      "remove the data for `NoUTRReason`" in {
        result.get(PartnerNoUTRReasonId(0, 0)) mustNot be(defined)
      }
    }

    "`PartnerHasUTR` is not present" must {

      val result: UserAnswers = answers().remove(PartnerHasUTRId(0, 0)).asOpt.value

      "not remove the data for `PartnerUTR`" in {
        result.get(PartnerEnterUTRId(0, 0)) mustBe defined
      }

      "not remove the data for `NoUTRReason`" in {
        result.get(PartnerNoUTRReasonId(0, 0)) mustBe defined
      }
    }
  }

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(PartnerNameId(0, 0))(name).flatMap(
      _.set(PartnerHasUTRId(0, 0))(true)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnerHasUTRId(0, 0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher" must {

      def answersNew: UserAnswers = answers.set(IsNewPartnerId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

        PartnerHasUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher" must {

      "not display any row" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))


        PartnerHasUTRId(0, 0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Nil
      }
    }
  }
}

object PartnerHasUTRIdSpec extends SpecBase {
  val onwardUrl = "onwardUrl"
  val name = PersonName("Test", "Name")

  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(
      label = Message("messages__hasUTR", name.fullName),
      answer = List("site.yes"),
      answerIsMessageKey = true,
      changeUrl = Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__dynamic_hasUtr", name.fullName))))
    )
  )
}
