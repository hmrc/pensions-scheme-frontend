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

package identifiers.register.establishers.company

import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class HasCompanyUTRIdSpec extends WordSpec with MustMatchers with OptionValues {

  "Cleanup" when {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(HasCompanyUTRId(0))(hasUtr)
      .flatMap(_.set(CompanyUTRId(0))("test-utr"))
      .flatMap(_.set(NoCompanyUTRId(0))("reason"))
      .asOpt.value

    "`HasCompanyUTR` is set to `false`" must {

      val result: UserAnswers = answers().set(HasCompanyUTRId(0))(false).asOpt.value

      "remove the data for `CompanyUTR`" in {
        result.get(CompanyUTRId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyUTR` is set to `true`" must {

      val result: UserAnswers = answers(false).set(HasCompanyUTRId(0))(true).asOpt.value

      "remove the data for `CompanyRegistrationNumberVariations`" in {
        result.get(NoCompanyUTRId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyUTR` is not present" must {

      val result: UserAnswers = answers().remove(HasCompanyUTRId(0)).asOpt.value

      "not remove the data for `CompanyUTR`" in {
        result.get(CompanyUTRId(0)) mustBe defined
      }

      "not remove the data for `NoCompanyUTR`" in {
        result.get(NoCompanyUTRId(0)) mustBe defined
      }
    }
  }
}
