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

import models.NormalMode
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers
import identifiers.register._
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{AddTrusteeId, TrusteeKindId, TrusteesId}
import models.person.PersonDetails
import models.register.SchemeType
import models.register.trustees.TrusteeKind
import org.joda.time.LocalDate

class RegisterNavigatorSpec extends WordSpec with MustMatchers with PropertyChecks {

  val navigator = new RegisterNavigator()
  val emptyAnswers = new UserAnswers(Json.obj())

  "Pages should post to the correct next page" when {

    ".nextPage(SchemeDetailsId)" must {
      "return a 'call' to 'SchemeEstablishedCountryController' page" in {
        val result = navigator.nextPage(SchemeDetailsId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(SchemeEstablishedCountryId)" must {
      "return a 'call' to 'MembershipController' page" in {
        val result = navigator.nextPage(SchemeEstablishedCountryId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.MembershipController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(MembershipId)" must {
      "return a 'call' to 'MembershipFutureController' page" in {
        val result = navigator.nextPage(MembershipId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.MembershipFutureController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(MembershipFutureId)" must {
      "return a 'call' to 'InvestmentRegulatedController' page" in {
        val result = navigator.nextPage(MembershipFutureId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.InvestmentRegulatedController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(InverstmentRegulatedId)" must {
      "return a 'call' to 'OccupationalController' page" in {
        val result = navigator.nextPage(InvestmentRegulatedId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(InvestmentRegulatedId)" must {
      "return a 'call' to 'OccupationalPensionSchemeController' page" in {
        val result = navigator.nextPage(InvestmentRegulatedId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(OccupationalPensionSchemeId)" must {
      "return a 'call' to 'BenefitsController' page" in {
        val result = navigator.nextPage(OccupationalPensionSchemeId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.BenefitsController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(BenefitsId)" must {
      "return a 'call' to 'SecuredBenefitsController' page" in {
        val result = navigator.nextPage(BenefitsId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.SecuredBenefitsController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(SecuredBenefitsId)" must {

      "return a 'call' to 'BenefitsInsuranceController' page if 'SecuredBenefits' is true" in {
        val answers = new UserAnswers(Json.obj(
          SecuredBenefitsId.toString -> true
        ))
        val result = navigator.nextPage(SecuredBenefitsId, NormalMode)(answers)
        result mustEqual controllers.register.routes.BenefitsInsurerController.onPageLoad(NormalMode)
      }

      "return a 'call' to 'UKBankAccountController' page if 'SecuredBenefits' is false" in {
        val answers = new UserAnswers(Json.obj(
          SecuredBenefitsId.toString -> false
        ))
        val result = navigator.nextPage(SecuredBenefitsId, NormalMode)(answers)
        result mustEqual controllers.register.routes.UKBankAccountController.onPageLoad(NormalMode)
      }

      "return a `Call` to `SessionExpired` page when `SecuredBenefits` is undefined" in {
        val result = navigator.nextPage(SecuredBenefitsId, NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(BenefitsInsuranceId)" must {
      "return a 'call' to 'InsurerPostCodeLookupController' page" in {
        val result = navigator.nextPage(BenefitsInsurerId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(InsurerPostCodeLookupId)" must {
      "return a 'call' to 'InsurerAddressListController' page" in {
        val result = navigator.nextPage(InsurerPostCodeLookupId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.InsurerAddressListController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(InsurerAddressListId)" must {
      "return a 'call' to 'InsurerAddressController' page" in {
        val result = navigator.nextPage(InsurerAddressListId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.InsurerAddressController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(InsurerAddressId)" must {
      "return a 'call' to 'UKBankAccountController' page" in {
        val result = navigator.nextPage(InsurerAddressId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.routes.UKBankAccountController.onPageLoad(NormalMode)
      }
    }

    ".nextPage(UKBankAccountId)" must {

      "return a 'call' to 'BenefitsInsuranceController' page if 'SecuredBenefits' is true" in {
        val answers = new UserAnswers(Json.obj(
          UKBankAccountId.toString -> true
        ))
        val result = navigator.nextPage(UKBankAccountId, NormalMode)(answers)
        result mustEqual controllers.register.routes.UKBankDetailsController.onPageLoad(NormalMode)
      }

      "return a `Call` to `SessionExpired` page when `SecuredBenefits` is undefined" in {
        val result = navigator.nextPage(SecuredBenefitsId, NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }

    ".nextPage(AddTrusteeId)" must {

      "return a `call` to `TrusteeKindController` page with index 1 if no trustees are added yet and 'AddTrustee' is true" in {
        val answers = new UserAnswers(Json.obj(
          AddTrusteeId.toString -> true
        ))
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeKindController` page with index 1 if no trustees are added yet and" +
        " 'AddTrustee' is undefined" in {
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(emptyAnswers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeKindController` page with index 2 if one trustee has already been added and 'AddTrustee' is true" in {
        val answers = new UserAnswers(Json.obj(
          AddTrusteeId.toString -> true,
          TrusteesId.toString -> Json.arr(
            Json.obj(
              TrusteeDetailsId.toString -> PersonDetails("first", Some("middle"), "last", LocalDate.now)
            )
          )
        ))
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 1)
      }

      "return a `call` to `SchemeReviewController` page if 'AddTrustee' is false" in {
        val answers = new UserAnswers(
          Json.obj(
            AddTrusteeId.toString -> false
          )
        )
        val result = navigator.nextPage(AddTrusteeId, NormalMode)(answers)
        result mustEqual controllers.register.routes.SchemeReviewController.onPageLoad()
      }
    }

    ".nextPage(TrusteeKindId)" must {

      "return a `call` to `TrusteeDetailsController` page if 'TrusteeKind' is 'Individual'" in {
        val answers = new UserAnswers(
          Json.obj(
            TrusteesId.toString -> Json.arr(
              Json.obj(
                TrusteeKindId.toString -> "individual"
              )
            )
          )
        )
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `TrusteeDetailsController` page if 'TrusteeKind' is 'Company'" in {
        val answers = new UserAnswers(
          Json.obj(
            TrusteesId.toString -> Json.arr(
              Json.obj(
                TrusteeKindId.toString -> "company"
              )
            )
          )
        )
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(answers)
        result mustEqual controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0)
      }

      "return a `call` to `SessionExpired` page if TrusteeKind is undefined" in {
        val result = navigator.nextPage(TrusteeKindId(0), NormalMode)(emptyAnswers)
        result mustEqual controllers.routes.SessionExpiredController.onPageLoad()
      }
    }
  }
}