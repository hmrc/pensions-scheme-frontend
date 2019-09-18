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

package identifiers.register.trustees.individual

import base.SpecBase
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.person.PersonName
import models.requests.DataRequest
import org.joda.time.LocalDate
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

class TrusteeDOBIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  val date = new LocalDate()
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(messages("messages__DOB__heading", "Test Name"),List(DateHelper.formatDate(date)), false, Some(Link("site.change",onwardUrl,
      Some(messages("messages__visuallyhidden__dynamic_dob", "Test Name")))))
  )

  "cya" when {

    def answers: UserAnswers = UserAnswers().set(TrusteeDOBId(0))(date).flatMap(_.set(TrusteeNameId(0))(PersonName("Test", "Name"))).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeDOBId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        TrusteeDOBId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        TrusteeDOBId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(messages("messages__DOB__heading", "Test Name"), List(DateHelper.formatDate(date)),false, None)
        ))
      }
    }
  }
}
