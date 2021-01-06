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

package identifiers.register.trustees.partnership

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnershipEnterUTRIdSpec extends SpecBase {
  
  import PartnershipEnterUTRIdSpec._

  "cleanup" when {
    "`PartnershipUTR` changed to a new value" must {
     val result = ua.set(PartnershipEnterUTRId(0))(ReferenceValue("value")).asOpt.value

      "remove the data for `PartnershipNoUTRReasonId`" in {
        result.get(PartnershipNoUTRReasonId(0)) mustNot be(defined)
      }
    }
  }

  "cya" when {

    def answers(isEditable: Boolean = false): UserAnswers = UserAnswers()
      .trusteePartnershipDetails(index = 0, PartnershipDetails(name))
      .set(PartnershipEnterUTRId(0))(ReferenceValue(utr, isEditable)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), Some(PsaId("A0000000")))

        PartnershipEnterUTRId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode" when {
      def answersNew: UserAnswers = answers().set(IsTrusteeNewId(0))(true).asOpt.value

      "for new trustee" must {

        "return answers rows with change links" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))

          PartnershipEnterUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
        }
      }

      "for existing trustee" must {

        "return row with add link if there is no data available" in {
          val answerRowWithAddLink = AnswerRow(Message("messages__enterUTR", name), List("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add",onwardUrl,
              Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name))
            )))
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().trusteePartnershipDetails(index = 0, PartnershipDetails(name)), Some(PsaId("A0000000")))


          PartnershipEnterUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Seq(answerRowWithAddLink)
        }

        "return row without change link if there is data avalable and is not editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(), Some(PsaId("A0000000")))


          PartnershipEnterUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithoutChangeLink
        }

        "return row with change link if there is data available and is editable" in {
          val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers(isEditable = true), Some(PsaId("A0000000")))


          PartnershipEnterUTRId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual answerRowsWithChangeLinks
        }
      }
    }
  }
}

object PartnershipEnterUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test partnership name"
  val utr = "1234567890"
  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)

  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__enterUTR", name), List(utr), false, Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name)))))
  )

  private val answerRowsWithoutChangeLink = Seq(
    AnswerRow(Message("messages__enterUTR", name), List(utr), false, None))

  private def ua = UserAnswers(Json.obj())
    .trusteePartnershipDetails(index = 0, PartnershipDetails(name))
    .set(PartnershipNoUTRReasonId(0))("value")
    .asOpt
    .value
}
