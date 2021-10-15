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

package identifiers

import base.SpecBase
import models.address.{Address, TolerantAddress}
import models.requests.DataRequest
import models.{Link, UpdateMode}
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{CountryOptions, Enumerable, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class InsuranceCompanyNameIdSpec extends SpecBase with ArgumentMatchers with ScalaCheckPropertyChecks with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answers = UserAnswers(Json.obj())
      .set(BenefitsSecuredByInsuranceId)(true)
      .flatMap(_.set(InsuranceCompanyNameId)("test"))
      .flatMap(_.set(InsurancePolicyNumberId)("test"))
      .flatMap(_.set(InsurerEnterPostCodeId)(Seq.empty))
      .flatMap(_.set(InsurerSelectAddressId)(TolerantAddress(None, None, None, None, None, None)))
      .flatMap(_.set(InsurerConfirmAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "InsuranceCompanyNameId is updated with new value" must {

      val result: UserAnswers = answers.set(InsuranceCompanyNameId)("test123").asOpt.value

      "remove the data for `InsurancePolicyNumber`" in {
        result.get(InsurancePolicyNumberId) mustNot be(defined)
      }

      "remove the data for `InsurerEnterPostCode`" in {
        result.get(InsurerEnterPostCodeId) mustNot be(defined)
      }

      "remove the data for `InsurerSelectAddress`" in {
        result.get(InsurerSelectAddressId) mustNot be(defined)
      }

      "remove the data for `InsurerConfirmAddress`" in {
        result.get(InsurerConfirmAddressId) mustNot be(defined)
      }
    }
  }

  "updateRow" when {

    "no data defined for InsuranceCompanyNameId" must {

      implicit val countryOptions = new CountryOptions(Seq(InputOption("AU", "Australia"),
        InputOption("GB", "United Kingdom")))

      "return empty AnswerRow when BenefitsSecuredByInsuranceId is false" in {
        val answers = UserAnswers(Json.obj())
          .set(BenefitsSecuredByInsuranceId)(false)
          .asOpt.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        val onwardUrl = "onwardUrl"
        InsuranceCompanyNameId.row(onwardUrl, UpdateMode) must equal(Seq.empty[AnswerRow])
      }

      "return correct AnswerRow when BenefitsSecuredByInsuranceId is true" in {
        val answers = UserAnswers(Json.obj())
          .set(BenefitsSecuredByInsuranceId)(true)
          .asOpt.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, Some(PsaId("A0000000")))

        val onwardUrl = "onwardUrl"
        InsuranceCompanyNameId.row(onwardUrl, UpdateMode) must equal(Seq(AnswerRow(Message("insuranceCompanyName.checkYourAnswersLabel"),
          List("site.not_entered"),true,Some(Link("site.add",onwardUrl,Some(Message("messages__visuallyhidden__insuranceCompanyName")))))))
      }
    }
  }
}
