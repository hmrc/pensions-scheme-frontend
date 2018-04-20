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

import identifiers.register._
import models.register.DeclarationDormant
import models.{CheckMode, Mode, NormalMode}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class RegisterNavigatorSpec extends WordSpec with MustMatchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  private val routes = Table(
    ("Id",                          "User Answers",       "Next Page (Normal Mode)",              "Next Page (Check Mode)"),
    // Start - what you will need
    (WhatYouWillNeedId,             emptyAnswers,         schemeDetails(NormalMode),              None),

    // Scheme registration
    (SchemeDetailsId,               emptyAnswers,         schemeEstablishedCountry(NormalMode),   Some(checkYourAnswers)),
    (SchemeEstablishedCountryId,    emptyAnswers,         membership(NormalMode),                 Some(checkYourAnswers)),
    (MembershipId,                  emptyAnswers,         membershipFuture(NormalMode),           Some(checkYourAnswers)),
    (MembershipFutureId,            emptyAnswers,         investmentRegulated(NormalMode),        Some(checkYourAnswers)),
    (InvestmentRegulatedId,         emptyAnswers,         occupationalPensionScheme(NormalMode),  Some(checkYourAnswers)),
    (OccupationalPensionSchemeId,   emptyAnswers,         benefits(NormalMode),                   Some(checkYourAnswers)),
    (BenefitsId,                    emptyAnswers,         securedBenefits(NormalMode),            Some(checkYourAnswers)),
    (SecuredBenefitsId,             securedBenefitsTrue,  benefitsInsurer(NormalMode),            Some(benefitsInsurer(CheckMode))),
    (SecuredBenefitsId,             securedBenefitsFalse, uKBankAccount(NormalMode),              Some(checkYourAnswers)),
    (SecuredBenefitsId,             emptyAnswers,         expired,                                Some(expired)),
    (BenefitsInsurerId,             emptyAnswers,         insurerPostCodeLookup(NormalMode),      Some(checkYourAnswers)),
    (InsurerPostCodeLookupId,       emptyAnswers,         insurerAddressList(NormalMode),         Some(insurerAddressList(CheckMode))),
    (InsurerAddressListId,          emptyAnswers,         insurerAddress(NormalMode),             Some(insurerAddress(CheckMode))),
    (InsurerAddressId,              emptyAnswers,         uKBankAccount(NormalMode),              Some(checkYourAnswers)),
    (UKBankAccountId,               ukBankAccountTrue,    uKBankDetails(NormalMode),              Some(uKBankDetails(CheckMode))),
    (UKBankAccountId,               ukBankAccountFalse,   checkYourAnswers,                       Some(checkYourAnswers)),
    (UKBankAccountId,               emptyAnswers,         expired,                                Some(expired)),
    (UKBankDetailsId,               emptyAnswers,         checkYourAnswers,                       Some(checkYourAnswers)),

    //Check your answers - jump off to establishers
    (CheckYourAnswersId,            emptyAnswers,         establisherKind,                        None),

    // Review, declarations, success - return from establishers
    (SchemeReviewId,                emptyAnswers,         declarationDormant,                     None),
    (DeclarationDormantId,          notDormant,           declarationDuties,                      None),
    (DeclarationDormantId,          dormant,              index,                                  None),
    (DeclarationDormantId,          emptyAnswers,         expired,                                None),
    (DeclarationDutiesId,           acceptDutiesTrue,     schemeSuccess,                          None),
    (DeclarationDutiesId,           acceptDutiesFalse,    index,                                  None),
    (DeclarationDutiesId,           emptyAnswers,         expired,                                None),
    (SchemeSuccessId,               emptyAnswers,         index,                                  None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes)
  }

}

object RegisterNavigatorSpec extends OptionValues with Enumerable.Implicits {

  private val navigator = new RegisterNavigator()

  private val emptyAnswers = UserAnswers(Json.obj())
  private val securedBenefitsTrue = UserAnswers().set(SecuredBenefitsId)(true).asOpt.value
  private val securedBenefitsFalse = UserAnswers().set(SecuredBenefitsId)(false).asOpt.value
  private val ukBankAccountTrue = UserAnswers().set(UKBankAccountId)(true).asOpt.value
  private val ukBankAccountFalse = UserAnswers().set(UKBankAccountId)(false).asOpt.value
  private val notDormant = UserAnswers().set(DeclarationDormantId)(DeclarationDormant.No).asOpt.value
  private val dormant = UserAnswers().set(DeclarationDormantId)(DeclarationDormant.Yes).asOpt.value
  private val acceptDutiesTrue = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
  private val acceptDutiesFalse = UserAnswers().set(DeclarationDutiesId)(false).asOpt.value

  private def benefits(mode: Mode) = controllers.register.routes.BenefitsController.onPageLoad(mode)
  private def benefitsInsurer(mode: Mode) = controllers.register.routes.BenefitsInsurerController.onPageLoad(mode)
  private def checkYourAnswers = controllers.register.routes.CheckYourAnswersController.onPageLoad()
  private def declarationDormant = controllers.register.routes.DeclarationDormantController.onPageLoad()
  private def declarationDuties = controllers.register.routes.DeclarationDutiesController.onPageLoad()
  private def insurerAddress(mode: Mode) = controllers.register.routes.InsurerAddressController.onPageLoad(mode)
  private def insurerAddressList(mode: Mode) = controllers.register.routes.InsurerAddressListController.onPageLoad(mode)
  private def insurerPostCodeLookup(mode: Mode) = controllers.register.routes.InsurerPostCodeLookupController.onPageLoad(mode)
  private def investmentRegulated(mode: Mode) = controllers.register.routes.InvestmentRegulatedController.onPageLoad(mode)
  private def membershipFuture(mode: Mode) = controllers.register.routes.MembershipFutureController.onPageLoad(mode)
  private def membership(mode: Mode) = controllers.register.routes.MembershipController.onPageLoad(mode)
  private def occupationalPensionScheme(mode: Mode) = controllers.register.routes.OccupationalPensionSchemeController.onPageLoad(mode)
  private def schemeDetails(mode: Mode) = controllers.register.routes.SchemeDetailsController.onPageLoad(mode)
  private def schemeEstablishedCountry(mode: Mode) = controllers.register.routes.SchemeEstablishedCountryController.onPageLoad(mode)
  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()
  private def securedBenefits(mode: Mode) = controllers.register.routes.SecuredBenefitsController.onPageLoad(mode)
  private def uKBankAccount(mode: Mode) = controllers.register.routes.UKBankAccountController.onPageLoad(mode)
  private def uKBankDetails(mode: Mode) = controllers.register.routes.UKBankDetailsController.onPageLoad(mode)

  private def establisherKind = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0)
  private def expired = controllers.routes.SessionExpiredController.onPageLoad()
  private def index = controllers.routes.IndexController.onPageLoad()

}
