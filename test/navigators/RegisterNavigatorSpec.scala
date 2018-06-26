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
import connectors.FakeDataCacheConnector
import identifiers.register._
import models._
import models.register.{SchemeDetails, SchemeType}
import org.scalatest.MustMatchers
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class RegisterNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  private def routesWithRestrictedEstablisher = Table(
    ("Id",                          "User Answers",       "Next Page (Normal Mode)",              "Save (NM)",  "Next Page (Check Mode)",             "Save (CM)"),
    // Start - continue or what you will need
    (ContinueRegistrationId,        emptyAnswers,         whatYouWillNeed,                        false,        None,                                 false),
    (ContinueRegistrationId,        savedLastPage,        lastPage,                               false,        None,                                 false),
    (WhatYouWillNeedId,             emptyAnswers,         schemeDetails(NormalMode),              false,        None,                                 false),

    // Scheme registration
    (SchemeDetailsId,               emptyAnswers,         schemeEstablishedCountry(NormalMode),   true,         Some(checkYourAnswers),               true),
    (SchemeEstablishedCountryId,    emptyAnswers,         membership(NormalMode),                 true,         Some(checkYourAnswers),               true),
    (MembershipId,                  emptyAnswers,         membershipFuture(NormalMode),           true,         Some(checkYourAnswers),               true),
    (MembershipFutureId,            emptyAnswers,         investmentRegulated(NormalMode),        true,         Some(checkYourAnswers),               true),
    (InvestmentRegulatedId,         emptyAnswers,         occupationalPensionScheme(NormalMode),  true,         Some(checkYourAnswers),               true),
    (OccupationalPensionSchemeId,   emptyAnswers,         benefits(NormalMode),                   true,         Some(checkYourAnswers),               true),
    (BenefitsId,                    emptyAnswers,         securedBenefits(NormalMode),            true,         Some(checkYourAnswers),               true),
    (SecuredBenefitsId,             securedBenefitsTrue,  benefitsInsurer(NormalMode),            true,         Some(benefitsInsurer(CheckMode)),     true),
    (SecuredBenefitsId,             securedBenefitsFalse, uKBankAccount(NormalMode),              true,         Some(checkYourAnswers),               true),
    (SecuredBenefitsId,             emptyAnswers,         expired,                                false,        Some(expired),                        false),
    (BenefitsInsurerId,             emptyAnswers,         insurerPostCodeLookup(NormalMode),      true,         Some(checkYourAnswers),               true),
    (InsurerPostCodeLookupId,       emptyAnswers,         insurerAddressList(NormalMode),         true,         Some(insurerAddressList(CheckMode)),  true),
    (InsurerAddressListId,          emptyAnswers,         insurerAddress(NormalMode),             true,         Some(insurerAddress(CheckMode)),      true),
    (InsurerAddressId,              emptyAnswers,         uKBankAccount(NormalMode),              true,         Some(checkYourAnswers),               true),
    (UKBankAccountId,               ukBankAccountTrue,    uKBankDetails(NormalMode),              true,         Some(uKBankDetails(CheckMode)),       true),
    (UKBankAccountId,               ukBankAccountFalse,   checkYourAnswers,                       true,         Some(checkYourAnswers),               true),
    (UKBankAccountId,               emptyAnswers,         expired,                                false,        Some(expired),                        false),
    (UKBankDetailsId,               emptyAnswers,         checkYourAnswers,                       true,         Some(checkYourAnswers),               true),

    //Check your answers - jump off to establishers
    (CheckYourAnswersId,            noEstablishers,       addEstablisher,                         true,         None,                                 false),
    (CheckYourAnswersId,            hasEstablishers,      schemeReview,                           true,         None,                                 false),
    (CheckYourAnswersId,            needsTrustees,        addTrustee,                             true,         None,                                 false),

    // Review, declarations, success - return from establishers
    (SchemeReviewId,                hasCompanies,         declarationDormant,                     true,         None,                                 false),
    (SchemeReviewId,                emptyAnswers,         declaration,                            true,         None,                                 false),
    (DeclarationDormantId,          emptyAnswers,         declaration,                            true,         None,                                 false),
    (DeclarationId,                 emptyAnswers,         declarationDuties,                      true,         None,                                 false),
    (DeclarationDutiesId,           dutiesTrue,           schemeSuccess,                          false,        None,                                 false),
    (DeclarationDutiesId,           dutiesFalse,          adviserDetails,                         true,         None,                                 false),
    (DeclarationDutiesId,           emptyAnswers,         expired,                                false,        None,                                 false)
  )

  "RegisterNavigator" must {
    appRunning()
    val navigator = testNavigator()
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routesWithRestrictedEstablisher, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec {

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

  private def testNavigator(isEstablisherRestricted: Boolean = false): RegisterNavigator = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.restrict-establisher" -> isEstablisherRestricted))
    val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]
    new RegisterNavigator(FakeDataCacheConnector, appConfig)
  }

}
