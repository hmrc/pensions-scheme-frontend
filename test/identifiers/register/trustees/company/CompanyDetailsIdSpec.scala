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

package identifiers.register.trustees.company

import base.SpecBase
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{IsTrusteeNewId, MoreThanTenTrusteesId}
import models.person.PersonDetails
import models.requests.DataRequest
import models.{CompanyDetails, Link, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, UserAnswers}
import viewmodels.AnswerRow

class CompanyDetailsIdSpec extends SpecBase with Enumerable.Implicits {

  private def individualTrustee(index: Int) = PersonDetails(
    s"test-trustee-$index",
    None,
    "test-last-name",
    LocalDate.now()
  )

  private def companyTrustee(index: Int) = CompanyDetails(
    s"test-company-$index"
  )

  "Cleanup" must {

    val answers = UserAnswers(Json.obj())
      .set(CompanyDetailsId(0))(companyTrustee(0))
      .flatMap(_.set(TrusteeDetailsId(1))(individualTrustee(1)))
      .flatMap(_.set(MoreThanTenTrusteesId)(true))
      .asOpt.value

    "One trustee is deleted from a set of 10 while the `more than ten trustees` flag was set to yes" when {

      val result: UserAnswers = answers.remove(CompanyDetailsId(1)).asOpt.value

      "remove the data for `More than 10 trustees`" in {
        result.get(MoreThanTenTrusteesId) mustNot be(defined)
      }


    }

  }

  "cya" when {

    val onwardUrl = "onwardUrl"

    def answers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails("test company")).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyDetailsId(0).row(onwardUrl, NormalMode) must equal(Seq(
          AnswerRow("messages__common__cya__name",List("test company"),false,
            Some(Link("site.change",onwardUrl,Some("Change test company’s name"))))
        ))
      }
    }

    "in update mode for new trustee - company" must {

      def answersNew: UserAnswers = answers.set(IsTrusteeNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers
        CompanyDetailsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__cya__name",List("test company"),false,
            Some(Link("site.change",onwardUrl,Some("Change test company’s name"))))
        ))
      }
    }

    "in update mode for existing trustee - company" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        CompanyDetailsId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow("messages__common__cya__name",List("test company"),false,None)
        ))
      }
    }
  }

}
