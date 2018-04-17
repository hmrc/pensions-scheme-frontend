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
import identifiers.register.establishers.individual._
import models.{CheckMode, NormalMode}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class EstablishersIndividualNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {

  val navigator = new EstablishersIndividualNavigator()
  val emptyAnswers = UserAnswers(Json.obj())

  "NormalMode" when {

    ".nextPage(EstablishersDetailsId)" must {
      "return a `Call` to `EstablisherNino` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(EstablisherDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(EstablisherNinoId)" must {
      "return a `Call` to `UniqueTaxReference` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(EstablisherNinoId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(UniqueTaxReferenceId)" must {
      "return a `Call` to `PostCodeLookup` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(UniqueTaxReferenceId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(PostCodeLookupId)" must {
      "return a `Call` to `AddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.AddressListController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(AddressListId)" must {
      "return a `Call` to `Address` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(AddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.AddressController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(AddressId)" must {
      "return a `Call` to `AddressYears` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(AddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(AddressYears)" must {

      "return a `Call` to `ContactDetails` page when `AddressYears` is `OverAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              AddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(AddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, 0)
      }

      "return a `Call` to `PreviousPostCodeLookup` page when `AddressYears` is `UnderAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              AddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(AddressYearsId(0), NormalMode)(answers)
        result mustEqual controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(NormalMode, 0)
      }

      "return a `Call` to `SessionExpired` page when `AddressYears` is undefined" in {
        val result = navigator.nextPage(AddressYearsId(0), NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(PreviousAddressPostCodeLookup)" must {
      "return a `Call` to `PreviousAddressList`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousPostCodeLookupId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(PreviousAddressList)" must {
      "return a `Call` to `PreviousAddress`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousAddressListId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(PreviousAddress)" must {
      "return a `Call` to `ContactDetails`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousAddressId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, index)
        }
      }
    }

    ".nextPage(ContactDetails)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(ContactDetailsId(index), NormalMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(CheckYourAnswers)" must {
      "return a `Call` to `AddTrusteeController`" in {
        val result = navigator.nextPage(CheckYourAnswersId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
      }
    }
  }

  "CheckMode" when {

    ".nextPage(EstablishersDetailsId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(EstablisherDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(EstablisherNinoId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(EstablisherNinoId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(UniqueTaxReferenceId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(UniqueTaxReferenceId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(PostCodeLookupId)" must {
      "return a `Call` to `AddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.AddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(AddressListId)" must {
      "return a `Call` to `Address` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(AddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(AddressId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(AddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(AddressYears)" must {

      "return a `Call` to `CheckYourAnswersPage` page when `AddressYears` is `OverAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              AddressYearsId.toString ->
                "over_a_year"
            )
          )
        ))
        val result = navigator.nextPage(AddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(0)
      }

      "return a `Call` to `PreviousPostCodeLookup` page when `AddressYears` is `UnderAYear`" in {
        val answers = UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              AddressYearsId.toString ->
                "under_a_year"
            )
          )
        ))
        val result = navigator.nextPage(AddressYearsId(0), CheckMode)(answers)
        result mustEqual controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(CheckMode, 0)
      }

      "return a `Call` to `SessionExpired` page when `AddressYears` is undefined" in {
        val result = navigator.nextPage(AddressYearsId(0), CheckMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(PreviousAddressPostCodeLookup)" must {
      "return a `Call` to `PreviousAddressList`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousPostCodeLookupId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(PreviousAddressList)" must {
      "return a `Call` to `PreviousAddress`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousAddressListId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, index)
        }
      }
    }

    ".nextPage(PreviousAddress)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(PreviousAddressId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }

    ".nextPage(ContactDetails)" must {
      "return a `Call` to `CheckYourAnswers`" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(ContactDetailsId(index), CheckMode)(emptyAnswers)
            result mustEqual controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
        }
      }
    }
  }
}
