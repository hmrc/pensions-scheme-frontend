/*
 * Copyright 2019 HM Revenue & Customs
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
import connectors.FakeUserAnswersCacheConnector
import identifiers.register._
import identifiers.{IsBeforeYouStartCompleteId, UserResearchDetailsId}
import models._
import models.address.Address
import models.register.{SchemeDetails, SchemeType}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class RegisterNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import RegisterNavigatorSpec._

  private def routesWithRestrictedEstablisher = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    // Start - continue or what you will need
    (ContinueRegistrationId, emptyAnswers, beforeYouStart, false, None, false),
    (ContinueRegistrationId, beforeYouStartInProgress, beforeYouStart, false, None, false),
    (ContinueRegistrationId, beforeYouStartCompleted, taskList, false, None, false),

    // Scheme registration
    (CheckYourAnswersId, emptyAnswers, taskList, false, None, false),

    // Review, declarations, success - return from establishers
    (DeclarationId, hasEstablishers, schemeSuccess, false, None, false),
    (DeclarationDutiesId, dutiesTrue, adviserCheckYourAnswers, true, Some(adviserCheckYourAnswers), true),
    (DeclarationDutiesId, dutiesFalse, adviserName, true, Some(adviserName), true),
    (DeclarationDutiesId, emptyAnswers, expired, false, None, false),

    // User Research page - return to SchemeOverview
    (UserResearchDetailsId, emptyAnswers, schemeOverview(frontendAppConfig), false, None, false)
  )

  "RegisterNavigator" must {
    val navigator = new RegisterNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routesWithRestrictedEstablisher, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec extends OptionValues{

  private val lastPage: Call = Call("GET", "http://www.test.com")

  private val emptyAnswers = UserAnswers(Json.obj())

  private val securedBenefitsTrue = UserAnswers().securedBenefits(true)
  private val securedBenefitsFalse = UserAnswers().securedBenefits(false)
  private val ukBankAccountTrue = UserAnswers().ukBankAccount(true)
  private val ukBankAccountFalse = UserAnswers().ukBankAccount(false)
  private val dutiesTrue = UserAnswers().declarationDuties(true)
  private val dutiesFalse = UserAnswers().declarationDuties(false)
  private val hasCompanies = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test-company-name", None, None))
  private val hasPartnership = UserAnswers().establisherPartnershipDetails(0, models.PartnershipDetails("test-company-name"))
  private val hasEstablishers = hasCompanies.schemeName("test-scheme-name").schemeType(SchemeType.GroupLifeDeath)
  private val savedLastPage = UserAnswers().lastPage(LastPage(lastPage.method, lastPage.url))
  private val insurerAddress = UserAnswers().insurerAddress(Address("line-1", "line-2", None, None, None, "GB"))
  private val beforeYouStartCompleted = UserAnswers().set(IsBeforeYouStartCompleteId)(true).asOpt.value
  private val beforeYouStartInProgress = UserAnswers().set(IsBeforeYouStartCompleteId)(false).asOpt.value

  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def whatYouWillNeed = controllers.routes.WhatYouWillNeedController.onPageLoad()

  private def beforeYouStart = controllers.routes.BeforeYouStartController.onPageLoad()

  private def adviserName = controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def schemeOverview(appConfig: FrontendAppConfig) = appConfig.managePensionsSchemeOverviewUrl

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad()

  private def adviserCheckYourAnswers: Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()

}
