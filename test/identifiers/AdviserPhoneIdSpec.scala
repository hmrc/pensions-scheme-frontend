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

package identifiers

import base.SpecBase
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class AdviserPhoneIdSpec extends SpecBase with Enumerable.Implicits  {

  val onwardUrl = "onwardUrl"
  val name = "adviserName"
  val phone = "0111"
  implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("adviserPhone.checkYourAnswersLabel", name),Seq(phone), false,
      Some(Link("site.change",onwardUrl, Some(Message("messages__visuallyhidden__adviserPhone", name)))))
  )
  private val answerRowsWithNoChangeLinks = Seq(
    AnswerRow(Message("adviserPhone.checkYourAnswersLabel", name),Seq(phone), false)
  )
  val answers: UserAnswers = UserAnswers().set(AdviserPhoneId)(phone).flatMap(
    _.set(AdviserNameId)(name)).asOpt.get

  "cya" when {

    "in normal mode" must {

      "return answers rows with change links" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        AdviserPhoneId.row(onwardUrl, NormalMode)(request,implicitly) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode " must {

      "return answers rows without links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        implicit val userAnswers: UserAnswers = request.userAnswers
        AdviserPhoneId.row(onwardUrl, UpdateMode)(request,implicitly) must equal(answerRowsWithNoChangeLinks)
      }
    }

  }
}
