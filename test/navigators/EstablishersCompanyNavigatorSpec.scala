/*
 * Copyright 2018 HM Revenue & Customs
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

package navigators

import identifiers.register.establishers.company.{CompanyDetailsId, CompanyRegistrationNumberId}
import models.NormalMode
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FunSuite, MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class EstablishersCompanyNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {

  val navigator = new EstablishersCompanyNavigator()
  val emptyAnswers = new UserAnswers(Json.obj())

  "NormalMode" when {

    ".nextPage(CompanyDetails)" must {
      "return a `Call` to `CompanyRegistrationNumber` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyRegistrationNumber" must {
      "return a 'Call' to 'UTR' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyRegistrationNumberId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, index)
        }
      }
    }
  }
  "CheckMode" when {

  }
}
