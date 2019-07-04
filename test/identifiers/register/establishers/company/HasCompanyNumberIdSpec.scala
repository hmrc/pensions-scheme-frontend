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

import models._
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class HasCompanyNumberIdSpec extends WordSpec with MustMatchers with OptionValues {

  "Cleanup" when {

    def answers(hasCrn: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(HasCompanyNumberId(0))(hasCrn)
      .flatMap(_.set(CompanyRegistrationNumberVariationsId(0))(ReferenceValue("test-crn")))
      .flatMap(_.set(NoCompanyNumberId(0))("reason"))
      .asOpt.value

    "`HasCompanyNumber` is set to `false`" must {

      val result: UserAnswers = answers().set(HasCompanyNumberId(0))(false).asOpt.value

      "remove the data for `CompanyRegistrationNumberVariations`" in {
        result.get(CompanyRegistrationNumberVariationsId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyNumber` is set to `true`" must {

      val result: UserAnswers = answers(false).set(HasCompanyNumberId(0))(true).asOpt.value

      "remove the data for `CompanyRegistrationNumberVariations`" in {
        result.get(NoCompanyNumberId(0)) mustNot be(defined)
      }
    }

    "`HasCompanyNumber` is not present" must {

      val result: UserAnswers = answers().remove(HasCompanyNumberId(0)).asOpt.value

      "not remove the data for `CompanyRegistrationNumberVariations`" in {
        result.get(CompanyRegistrationNumberVariationsId(0)) mustBe defined
      }

      "not remove the data for `NoCompanyNumber`" in {
        result.get(NoCompanyNumberId(0)) mustBe defined
      }
    }
  }
}
