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

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company._
import identifiers.register.establishers.individual.AddressYearsId
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
    ".nextPage(CompanyRegistrationNumber)" must {
      "return a 'Call' to 'UTR' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyRegistrationNumberId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyUniqueTaxReference)" must {
      "return a 'Call' to 'Address Post Code' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyUniqueTaxReferenceId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressPostCode)" must {
      "return a 'Call' to 'Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressList)" must {
      "return a 'Call' to 'Address Manual' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddress)" must {
      "return a 'Call' to 'Address Years' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(CompanyAddressYears" must {
      "return a 'Call' to 'Previous Address Postcode' page when 'AddressYears' is 'LessThanOneYear'" in {
        (0 to 1).foreach {
          index =>
            val answers = new UserAnswers(Json.obj(
              EstablishersId.toString -> Json.arr(
                Json.obj(
                  AddressYearsId.toString ->
                    "under_a_year"
                )
              )
            ))
            val result = navigator.nextPage(CompanyAddressYearsId(index), NormalMode)(answers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0)
        }
      }
      "return a 'Call' to 'Company Contact Details' page when 'AddressYears' is 'MoreThanOneYear'" in {
        (0 to 10).foreach {
          index =>
            val answers = new UserAnswers(Json.obj(
              EstablishersId.toString -> Json.arr(
                Json.obj(
                  AddressYearsId.toString ->
                    "over_a_year"
                )
              )
            ))
            val result = navigator.nextPage(CompanyAddressYearsId(index), NormalMode)(answers)
            result mustEqual controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, 0)
        }
      }
    }
  }
  "CheckMode" when {
  }
}
