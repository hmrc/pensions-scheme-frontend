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

import base.SpecBase
import config.FrontendAppConfig
import identifiers.register._
import models.register.{SchemeDetails, SchemeType}
import models._
import org.scalatest.MustMatchers
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class RegisterNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  private def navigator(isEstablisherRestricted: Boolean = false) = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.restrict-establisher" -> isEstablisherRestricted))
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    new RegisterNavigator(appConfig)
  }

  private def routesWithRestrictedEstablisher = Table(
    ("Id",                          "User Answers",       "Next Page (Normal Mode)",              "Next Page (Check Mode)"),
    // Start - continue or what you will need
    (ContinueRegistrationId,        emptyAnswers,         whatYouWillNeed,                        None),
    (ContinueRegistrationId,        savedLastPage,        lastPage,                               None),
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
    (CheckYourAnswersId,            noEstablishers,       addEstablisher,                         None),
    (CheckYourAnswersId,            hasEstablishers,      schemeReview,                           None),
    (CheckYourAnswersId,            needsTrustees,        addTrustee,                             None),

    // Review, declarations, success - return from establishers
    (SchemeReviewId,                hasCompanies,         declarationDormant,                     None),
    (SchemeReviewId,                emptyAnswers,         declaration,                            None),
    (DeclarationDormantId,          emptyAnswers,         declaration,                            None),
    (DeclarationId,                 emptyAnswers,         declarationDuties,                      None),
    (DeclarationDutiesId,           dutiesTrue,           schemeSuccess,                          None),
    (DeclarationDutiesId,           dutiesFalse,          adviserDetails,                         None),
    (DeclarationDutiesId,           emptyAnswers,         expired,                                None)
  )
  s"${navigator().getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator(), routesWithRestrictedEstablisher, dataDescriber)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec extends Enumerable.Implicits {

  private val lastPage: Call = Call("GET", "http://www.test.com")

  private val emptyAnswers = UserAnswers(Json.obj())
  private val securedBenefitsTrue = UserAnswers().securedBenefits(true)
  private val securedBenefitsFalse = UserAnswers().securedBenefits(false)
  private val ukBankAccountTrue = UserAnswers().ukBankAccount(true)
  private val ukBankAccountFalse = UserAnswers().ukBankAccount(false)
  private val dutiesTrue = UserAnswers().declarationDuties(true)
  private val dutiesFalse = UserAnswers().declarationDuties(false)
  private val hasCompanies = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test-company-name", None, None))
  private val noEstablishers = emptyAnswers
  private val hasEstablishers = hasCompanies.schemeDetails(SchemeDetails("test-scheme-name", SchemeType.GroupLifeDeath))
  private val needsTrustees = hasCompanies.schemeDetails(SchemeDetails("test-scheme-name", SchemeType.SingleTrust))
  private val savedLastPage = UserAnswers().lastPage(LastPage(lastPage.method, lastPage.url))

  private def benefits(mode: Mode) = controllers.register.routes.BenefitsController.onPageLoad(mode)
  private def benefitsInsurer(mode: Mode) = controllers.register.routes.BenefitsInsurerController.onPageLoad(mode)
  private def checkYourAnswers = controllers.register.routes.CheckYourAnswersController.onPageLoad()
  private def declaration = controllers.register.routes.DeclarationController.onPageLoad()
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
  private def schemeReview = controllers.register.routes.SchemeReviewController.onPageLoad()
  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()
  private def securedBenefits(mode: Mode) = controllers.register.routes.SecuredBenefitsController.onPageLoad(mode)
  private def uKBankAccount(mode: Mode) = controllers.register.routes.UKBankAccountController.onPageLoad(mode)
  private def uKBankDetails(mode: Mode) = controllers.register.routes.UKBankDetailsController.onPageLoad(mode)
  private def whatYouWillNeed = controllers.routes.WhatYouWillNeedController.onPageLoad()

  private def addTrustee = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)
  private def adviserDetails = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode)
  private def establisherKind = controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0)
  private def addEstablisher = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
