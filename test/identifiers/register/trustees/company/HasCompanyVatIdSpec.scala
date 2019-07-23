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
import models._
import play.api.libs.json.Json
import utils.UserAnswers

class HasCompanyVatIdSpec extends SpecBase {

  val onwardUrl = "onwardUrl"
  val name = "test company name"

  "Cleanup" when {

    def answers(hasVat: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(HasCompanyVATId(0))(hasVat)
      .flatMap(_.set(CompanyVatVariationsId(0))(ReferenceValue("test-crn")))
      .asOpt.value

    "`HasCompanyVat` is set to `false`" must {

      val result: UserAnswers = answers().set(HasCompanyVATId(0))(false).asOpt.value

      "remove the data for `CompanyVatVariations`" in {
        result.get(CompanyVatVariationsId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyVat` is set to `true`" must {

      val result: UserAnswers = answers(false).set(HasCompanyVATId(0))(true).asOpt.value

      "no clean up for `CompanyVatVariations`" in {
        result.get(CompanyVatVariationsId(0)) must be(defined)
      }
    }

    "`HasCompanyVat` is not present" must {

      val result: UserAnswers = answers().remove(HasCompanyVATId(0)).asOpt.value

      "no clean up for `CompanyVatVariations`" in {
        result.get(CompanyVatVariationsId(0)) mustBe defined
      }
    }
  }

}
