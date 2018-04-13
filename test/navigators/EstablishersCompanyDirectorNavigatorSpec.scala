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

import controllers.actions.FakeDataRetrievalAction
import identifiers.register.establishers.company.director._
import models.{CheckMode, CompanyDetails, Index, NormalMode}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers
import controllers.register.establishers.company.director.routes
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.register.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate

class EstablishersCompanyDirectorNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {
  val navigator = new EstablishersCompanyDirectorNavigator()
  val emptyAnswers = new UserAnswers(Json.obj())
  val establisherIndex = Index(0)

  "NormalMode" when {

    ".nextPage(DirectorDetailsId)" must {
      "return a `Call` to `DirectorNino` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorDetailsId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorNinoController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorNinoId)" must {
      "return a `Call` to `DirectorUniqueTaxReference` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorNinoId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorUniqueTaxReferenceId)" must {
      "return a `Call` to `DirectorAddressPostcodeLookup` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorUniqueTaxReferenceId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressPostcodeLookupId)" must {
      "return a `Call` to `DirectorAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressPostcodeLookupId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressListController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressListId)" must {
      "return a `Call` to `DirectorAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressListId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressId)" must {
      "return a `Call` to `DirectorAddressYears` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressYearsController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(AddressYears)" must {

      "return a `Call` to `ContactDetails` page when `AddressYears` is `OverAYear`" in {
        val answers = new UserAnswers(Json.obj(
            EstablishersId.toString -> Json.arr(
              Json.obj(
                "director" -> Json.arr(
                  Json.obj(
                    DirectorAddressYearsId.toString -> "over_a_year"
                  )
                )
              )
            )
          ))

        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), NormalMode)(answers)
        result mustEqual routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, 0)
      }

      "return a `Call` to `PreviousPostCodeLookup` page when `AddressYears` is `UnderAYear`" in {

        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              "director" -> Json.arr(
                Json.obj(
                  DirectorAddressYearsId.toString -> "under_a_year"
                )
              )
            )
          )
        ))

        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), NormalMode)(answers)
        result mustEqual routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, 0)
      }

      "return a `Call` to `SessionExpired` page when `AddressYears` is undefined" in {
        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(DirectorPreviousAddressPostcodeLookupId)" must {
      "return a `Call` to `DirectorPreviousAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressPostcodeLookupId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorPreviousAddressListId)" must {
      "return a `Call` to `DirectorPreviousAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressListId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorPreviousAddressController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorPreviousAddressId)" must {
      "return a `Call` to `DirectorContactDetails` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorContactDetailsId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorContactDetailsId(establisherIndex, index), NormalMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

  }

  "CheckMode" when {

    ".nextPage(DirectorDetailsId)" must {
      "return a `Call` to `DirectorNino` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorDetailsId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorNinoId)" must {
      "return a `Call` to `DirectorUniqueTaxReference` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorNinoId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorUniqueTaxReferenceId)" must {
      "return a `Call` to `DirectorAddressPostcodeLookup` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorUniqueTaxReferenceId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressPostcodeLookupId)" must {
      "return a `Call` to `DirectorAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressPostcodeLookupId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressListController.onPageLoad(CheckMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressListId)" must {
      "return a `Call` to `DirectorAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressListId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.DirectorAddressController.onPageLoad(CheckMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorAddressId)" must {
      "return a `Call` to `DirectorAddressYears` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorAddressId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

    ".nextPage(AddressYears)" must {

      "return a `Call` to `ContactDetails` page when `AddressYears` is `OverAYear`" in {

        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              "director" -> Json.arr(
                Json.obj(
                  DirectorAddressYearsId.toString -> "over_a_year"
                )
              )
            )
          )
        ))

        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), CheckMode)(answers)
        result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, 0)
      }

      "return a `Call` to `PreviousPostCodeLookup` page when `AddressYears` is `UnderAYear`" in {
        val answers = new UserAnswers(Json.obj(
          EstablishersId.toString -> Json.arr(
            Json.obj(
              "director" -> Json.arr(
                Json.obj(
                  DirectorAddressYearsId.toString -> "under_a_year"
                )
              )
            )
          )
        ))
        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), CheckMode)(answers)
        result mustEqual routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, establisherIndex, 0)
      }

      "return a `Call` to `SessionExpired` page when `AddressYears` is undefined" in {
        val result = navigator.nextPage(DirectorAddressYearsId(0, 0), CheckMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(DirectorPreviousAddressPostcodeLookupId)" must {
      "return a `Call` to `DirectorPreviousAddressList` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressPostcodeLookupId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorPreviousAddressListId)" must {
      "return a `Call` to `DirectorPreviousAddress` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressListId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.DirectorPreviousAddressController.onPageLoad(CheckMode, establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorPreviousAddressId)" must {
      "return a `Call` to `DirectorContactDetails` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorPreviousAddressId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

    ".nextPage(DirectorContactDetailsId)" must {
      "return a `Call` to `CheckYourAnswers` page" in {
        (0 to 10).foreach {
          index =>
            val result = navigator.nextPage(DirectorContactDetailsId(establisherIndex, index), CheckMode)(emptyAnswers)
            result mustEqual routes.CheckYourAnswersController.onPageLoad(establisherIndex, index)
        }
      }
    }

  }
}




