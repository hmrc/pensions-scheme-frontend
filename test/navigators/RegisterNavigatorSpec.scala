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
import identifiers.{UserResearchDetailsId, VariationDeclarationId}
import models._
import models.register.SchemeType
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
    (DeclarationDormantId, beforeYouStartCompleted, declaration, false, None, false),

    // Review, declarations, success - return from establishers
    (DeclarationId, hasEstablishers, schemeSuccess, false, None, false),

    // User Research page - return to SchemeOverview
    (UserResearchDetailsId, emptyAnswers, schemeOverview(frontendAppConfig), false, None, false)
  )

  private def updateRoute = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    // Start - continue or what you will need
    (VariationDeclarationId, emptyAnswers, variationSucess, false, None, false)
  )

  "RegisterNavigator" must {
    val navigator = new RegisterNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routesWithRestrictedEstablisher, dataDescriber, NormalMode)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoute, dataDescriber, UpdateMode, Some("srn"))
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object RegisterNavigatorSpec extends OptionValues{

  private val lastPage: Call = Call("GET", "http://www.test.com")

  private val emptyAnswers = UserAnswers(Json.obj())
  private val ukBankAccountTrue = UserAnswers().ukBankAccount(true)
  private val ukBankAccountFalse = UserAnswers().ukBankAccount(false)
  private val hasCompanies = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test-company-name"))
  private val hasPartnership = UserAnswers().establisherPartnershipDetails(0, models.PartnershipDetails("test-company-name"))
  private val hasEstablishers = hasCompanies.schemeName("test-scheme-name").schemeType(SchemeType.GroupLifeDeath)
  private val savedLastPage = UserAnswers().lastPage(LastPage(lastPage.method, lastPage.url))
  private val beforeYouStartInProgress = UserAnswers().schemeName("Test Scheme")
  private val beforeYouStartCompleted = beforeYouStartInProgress.schemeType(SchemeType.SingleTrust).
    establishedCountry(country = "GB").declarationDuties(haveWorkingKnowledge = true)

  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def beforeYouStart = controllers.routes.BeforeYouStartController.onPageLoad()

  private def declaration = controllers.register.routes.DeclarationController.onPageLoad()

  private def expired = controllers.routes.SessionExpiredController.onPageLoad()

  private def schemeOverview(appConfig: FrontendAppConfig) = appConfig.managePensionsSchemeOverviewUrl

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)
  private def variationSucess: Call = controllers.register.routes.SchemeVariationsSuccessController.onPageLoad("srn")

}
