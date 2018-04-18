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

import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual._
import models.{CheckMode, NormalMode}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class TrusteesIndividualNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {

  val navigator = new TrusteesIndividualNavigator()
  val emptyAnswers = UserAnswers(Json.obj())

  "Normal Mode" when {
    ".nextPage(TrusteeDetails)" must {
      "return a `Call` to `TrusteeNino` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteeNino)" must {
      "return a Call to UTR page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeNinoId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(UniqueTaxReference)" must {
      "return a 'Call' to 'Address Post Code' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(UniqueTaxReferenceId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(NormalMode, index)
        }
      }
    }
    ".nextPage(IndividualAddressPostCode)" must {
      "return a 'Call' to 'Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualPostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(IndividualAddressList)" must {
      "return a 'Call' to 'Address Manual' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteeAddress)" must {
      "return a 'Call' to 'Address Years' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteeAddressYears)" must {
      "return a 'Call' to 'Previous Address Postcode' page when 'TrusteeAddressYears' is 'LessThanOneYear'" in {
        val answers = UserAnswers(Json.obj(
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(TrusteeAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, 0)
      }

      "return a 'Call' to 'Trustee Contact Details' page when 'AddressYears' is 'MoreThanOneYear'" in {
        val answers = UserAnswers(Json.obj(
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(TrusteeAddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, 0)
      }
    }
    ".nextPage(IndividualPreviousAddressPostCode)" must {
      "return a 'Call' to 'Previous Address Picker' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualPreviousAddressPostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteePreviousAddressPicker)" must {
      "return a 'Call' to 'Previous Address Manual' page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteePreviousAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteePreviousAddressManual)" must {
      "return a 'Call' to 'Trustee Contact Details" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteePreviousAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(TrusteeContactDetails)" must {
      "return a 'Call' to 'Individual Check Your Answers" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeContactDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

  }

  "CheckMode" when {
    ".nextPage(TrusteeDetails)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }
    ".nextPage(TrusteeNino)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeNinoId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(UniquetaxReference)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(UniqueTaxReferenceId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(IndividualPostCodeLookupId)" must {
      "return a `Call` to `IndividualAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualPostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(IndividualAddressListId)" must {
      "return a `Call` to `TrusteeAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(TrusteeAddressId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(TrusteeAddressYears)" must {

      "return a `Call` to `IndividualPreviousPostCodeLookup` page when `CompanyAddressYears` is `UnderAYear`" in {
        val answers = UserAnswers(Json.obj(
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeAddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(TrusteeAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, 0)
      }

      "return a `Call` to `CheckYourAnswersPage` page when `TrusteeAddressYears` is `OverAYear`" in {
        val answers = UserAnswers(Json.obj(
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeAddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(TrusteeAddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(0)
      }

      "return a `Call` to `SessionExpired` page when `TrusteeAddressYears` is undefined" in {
        val result = navigator.nextPage(TrusteeAddressYearsId(0), CheckMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(IndividualPreviousAddressPostCodeLookup)" must {
      "return a `Call` to `TrusteePreviousAddressList`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(IndividualPreviousAddressPostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(TrusteePreviousAddressList)" must {
      "return a `Call` to `TrusteePreviousAddress`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteePreviousAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(TrusteePreviousAddress)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteePreviousAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(TrusteeContactDetails)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(TrusteeContactDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }
  }
}
