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

package identifiers.register.trustees.partnership

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models.{Link, NormalMode, PartnershipDetails, ReferenceValue, UpdateMode}
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.AnswerRow

class PartnershipHasPAYEIdSpec extends SpecBase {
  
  import PartnershipHasPAYEIdSpec._

  "cleanup" when {
    "`PartnershipHasPAYE` changed to false" must {
     val result = ua(true).set(PartnershipHasPAYEId(0))(false).asOpt.value

      "remove the data for `PartnershipEnterPAYEId`" in {
        result.get(PartnershipEnterPAYEId(0)) mustNot be(defined)
      }
    }

    "`PartnershipHasPAYE` changed to true" must {
      val result = ua(false).set(PartnershipHasPAYEId(0))(true).asOpt.value

      "not remove the data for `PartnershipEnterPAYEId`" in {
        result.get(PartnershipEnterPAYEId(0)) must be(defined)
      }
    }
  }

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(PartnershipDetailsId(0))(PartnershipDetails(name)).flatMap(
      _.set(PartnershipHasPAYEId(0))(true)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnershipHasPAYEId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee - partnership paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnershipHasPAYEId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee - partnership paye" must {

      "not display any row" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        PartnershipHasPAYEId(0).row(onwardUrl, UpdateMode)(request, implicitly) mustEqual Nil
      }
    }
  }
}

object PartnershipHasPAYEIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test partnership name"

  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(messages("messages__hasPaye__h1", name), List("site.yes"), true, Some(Link("site.change",onwardUrl,
      Some(messages("messages__visuallyhidden__dynamic_hasPaye", name)))))
  )

  private def ua(v:Boolean) = UserAnswers(Json.obj())
    .set(PartnershipHasPAYEId(0))(v)
    .flatMap(_.set(PartnershipEnterPAYEId(0))(ReferenceValue("value")))
    .asOpt
    .value
}

