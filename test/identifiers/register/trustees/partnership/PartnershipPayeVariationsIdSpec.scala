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
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

class PartnershipPayeVariationsIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  private val partnershipName = "test partnership name"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(messages("messages__cya__paye", partnershipName),List("paye"),false,Some(Link("site.change",onwardUrl,
      Some(messages("messages__visuallyhidden__dynamic_paye", partnershipName)))))
  )

  private val ua = UserAnswers().set(PartnershipDetailsId(0))(PartnershipDetails(partnershipName)).asOpt.value

  "cya" when {

    def answers: UserAnswers = ua.set(PartnershipPayeVariationsId(0))(ReferenceValue("paye")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnershipPayeVariationsId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee - partnership paye" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnershipPayeVariationsId(0).row(onwardUrl, NormalMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee - partnership paye" must {

      "return answers rows without change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        PartnershipPayeVariationsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(messages("messages__cya__paye", partnershipName),List("paye"),false,None)
        ))
      }

      "return answers rows with change links if paye is available and editable" in {
        val answers = ua.set(PartnershipPayeVariationsId(0))(ReferenceValue("paye", true)).asOpt.get
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        PartnershipPayeVariationsId(0).row(onwardUrl, UpdateMode)(request, implicitly) must equal(answerRowsWithChangeLinks)
      }

      "display an add link if no answer if found" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", ua, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        PartnershipPayeVariationsId(0).row(onwardUrl, CheckUpdateMode)(request, implicitly) must equal(Seq(
          AnswerRow(messages("messages__cya__paye", partnershipName), Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_paye", partnershipName)))))))
      }
    }
  }
}
