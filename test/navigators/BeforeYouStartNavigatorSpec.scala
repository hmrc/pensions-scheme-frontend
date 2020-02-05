/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors.FakeUserAnswersCacheConnector
import controllers.routes._
import identifiers._
import models.register.SchemeType
import models.{CheckMode, NormalMode, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class BeforeYouStartNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import BeforeYouStartNavigatorSpec._

  private def routes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (SchemeNameId, emptyAnswers, schemeTypePage, false, Some(checkYourAnswersPage), false),
    (SchemeTypeId, schemeTypeGroupLife, haveAnyTrusteesPage, false, Some(haveAnyTrusteesCheckPage), false),
    (SchemeTypeId, schemeTypeSingleTrust, establishedCountryPage, false, Some(checkYourAnswersPage), false),
    (SchemeTypeId, emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (HaveAnyTrusteesId, emptyAnswers, establishedCountryPage, false, Some(checkYourAnswersPage), false),
    (EstablishedCountryId, emptyAnswers, workingKnowledgePage, false, Some(checkYourAnswersPage), false),
    (DeclarationDutiesId, emptyAnswers, checkYourAnswersPage, false, Some(checkYourAnswersPage), false)
  )

  val navigator = new BeforeYouStartNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
  "BeforeYouStartNavigator" must {

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes, dataDescriber)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object BeforeYouStartNavigatorSpec {

  private val emptyAnswers = UserAnswers(Json.obj())
  private def dataDescriber(answers: UserAnswers): String = answers.toString
  private val schemeTypePage: Call = SchemeTypeController.onPageLoad(NormalMode)
  private val haveAnyTrusteesPage: Call = HaveAnyTrusteesController.onPageLoad(NormalMode)
  private val haveAnyTrusteesCheckPage: Call = HaveAnyTrusteesController.onPageLoad(CheckMode)
  private val establishedCountryPage: Call = EstablishedCountryController.onPageLoad(NormalMode)
  private val workingKnowledgePage: Call = WorkingKnowledgeController.onPageLoad(NormalMode)
  private val checkYourAnswersPage: Call = controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, None)
  private val taskListPage: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)
  private val sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private val schemeTypeSingleTrust = UserAnswers(Json.obj(SchemeTypeId.toString -> SchemeType.SingleTrust))
  private val schemeTypeGroupLife = UserAnswers(Json.obj(SchemeTypeId.toString -> SchemeType.GroupLifeDeath))
}



