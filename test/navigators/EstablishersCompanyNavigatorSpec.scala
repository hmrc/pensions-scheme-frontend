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
import models.{CheckMode, NormalMode}
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
      "return a 'Call' to 'Previous Address Postcode' page when 'CompanyAddressYears' is 'LessThanOneYear'" in {
        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0)
      }
      "return a 'Call' to 'Company Contact Details' page when 'AddressYears' is 'MoreThanOneYear'" in {
        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, 0)
      }
    }
    ".nextPage(PreviousAddressPostCode" must {
      "return a 'Call' to 'Previous Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressPostcodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }
  }
  ".nextPage(PreviousAddressPicker" must {
    "return a 'Call' to 'Previous Address Manual' page" in {
      (0 to 10).foreach {
        index =>
          val result = navigator.nextPage(CompanyPreviousAddressListId(index), NormalMode)(emptyAnswers)
          result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode, index)
      }
    }
  }
  ".nextPage(PreviousAddressManual" must {
    "return a 'Call' to 'Company Contact Details" in {
      (0 to 10).foreach {
        index =>
          val result = navigator.nextPage(CompanyPreviousAddressId(index), NormalMode)(emptyAnswers)
          result mustEqual controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index)
      }
    }
  }
  ".nextPage(CompanyContactDetails" must {
    "return a 'Call' to 'Company Check Your Answers" in {
      (0 to 10).foreach {
        index =>
          val result = navigator.nextPage(CompanyContactDetailsId(index), NormalMode)(emptyAnswers)
          result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
      }
    }
  }

  "CheckMode" when {

    ".nextPage(CompanyDetails)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyRegistrationNumber)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyRegistrationNumberId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyUniqueTaxReference)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyUniqueTaxReferenceId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyPostCodeLookupId)" must {
      "return a `Call` to `CompanyAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyAddressListId)" must {
      "return a `Call` to `CompanyAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyAddressId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyAddressYears)" must {

      "return a `Call` to `CompanyPreviousPostCodeLookup` page when `CompanyAddressYears` is `UnderAYear`" in {
        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, 0)
      }

      "return a `Call` to `CheckYourAnswersPage` page when `CompanyAddressYears` is `OverAYear`" in {
        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(0)
      }

      "return a `Call` to `SessionExpired` page when `CompanyAddressYears` is undefined" in {
        val result = navigator.nextPage(CompanyAddressYearsId(0), CheckMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(CompanyPreviousAddressPostCodeLookup)" must {
      "return a `Call` to `CompanyPreviousAddressList`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressPostcodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyPreviousAddressList)" must {
      "return a `Call` to `CompanyPreviousAddress`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(CompanyPreviousAddress)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyPreviousAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CompanyContactDetails)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(CompanyContactDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }
  }
}
