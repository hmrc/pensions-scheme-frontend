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

package identifiers.register.establishers.company

import base.SpecBase
import identifiers.register.establishers.IsEstablisherNewId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class HasCompanyEnterUTRIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test company name"
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(Message("messages__hasUTR", name), List("site.yes"), true, Some(Link("site.change",onwardUrl,
      Some(Message("messages__visuallyhidden__dynamic_hasUtr", name)))))
  )

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(HasCompanyUTRId(0))(hasUtr)
      .flatMap(_.set(CompanyEnterUTRId(0))(ReferenceValue("test-utr")))
      .flatMap(_.set(CompanyNoUTRReasonId(0))("reason"))
      .asOpt.value

    "`HasCompanyUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(HasCompanyUTRId(0))(false).asOpt.value

      "remove the data for `CompanyUTR`" in {
        result.get(CompanyEnterUTRId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(HasCompanyUTRId(0))(true).asOpt.value

      "remove the data for `CompanyRegistrationNumberVariations`" in {
        result.get(CompanyNoUTRReasonId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyUTR` is not present" must {

      val result: UserAnswers = answers().remove(HasCompanyUTRId(0)).asOpt.value

      "not remove the data for `CompanyUTR`" in {
        result.get(CompanyEnterUTRId(0)) mustBe defined
      }

      "not remove the data for `NoCompanyUTR`" in {
        result.get(CompanyNoUTRReasonId(0)) mustBe defined
      }
    }
  }

  "cya" when {

    val answers: UserAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(name)).flatMap(
      _.set(HasCompanyUTRId(0))(true)).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        HasCompanyUTRId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new establisher - company paye" must {

      def answersNew: UserAnswers = answers.set(IsEstablisherNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, Some(PsaId("A0000000")))
        HasCompanyUTRId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing establisher - company paye" must {

      "not display any row" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))
        HasCompanyUTRId(0).row(onwardUrl, UpdateMode) mustEqual Nil
      }
    }
  }
}
