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

package identifiers.register.establishers.partnership

import base.SpecBase
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class PartnershipHasUTRIdSpec extends SpecBase {

  import PartnershipHasUTRIdSpec._

  "cleanup" when {
    "`PartnershipHasUTR` changed to false" must {
      val result = ua(true)
        .set(PartnershipHasUTRId(0))(false)
        .asOpt.value

      "remove the data for `PartnershipEnterUTRId`" in {
        result.get(PartnershipEnterUTRId(0)) mustNot be(defined)
      }
    }

    "`PartnershipHasUTR` changed to true" must {
      val result = ua(false)
        .set(PartnershipHasUTRId(0))(true)
        .asOpt.value

      "remove the data for `PartnershipNoUTRReasonId`" in {
        result.get(PartnershipNoUTRReasonId(0)) mustNot be(defined)
      }
    }
  }

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(PartnershipDetailsId(0))(PartnershipDetails(name)).flatMap(
      _.set(PartnershipHasUTRId(0))(value = true)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        PartnershipHasUTRId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - partnership" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(value = true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        PartnershipHasUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - partnership" must {

      "not display any row" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        PartnershipHasUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Nil
      }
    }
  }
}

object PartnershipHasUTRIdSpec extends SpecBase {
  val onwardUrl = "onwardUrl"
  val name = "test partnership name"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__hasUTR", name), List("site.yes"), true, Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_hasUtr", name)))))
  )

  private def ua(v: Boolean) =
    UserAnswers(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipHasUTRId.toString -> v,
          PartnershipEnterUTRId.toString -> "value",
          PartnershipNoUTRReasonId.toString -> "value"
        )
      )
    ))

}
