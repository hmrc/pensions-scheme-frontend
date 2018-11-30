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
import connectors.FakeUserAnswersCacheConnector
import identifiers.register.adviser._
import models.{CheckMode, Mode, NormalMode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AdviserNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AdviserDetailsId, emptyAnswers, adviserPostCodeLookup(NormalMode), true, Some(checkYourAnswersPage), true),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressList(NormalMode), true, Some(adviserAddressList(CheckMode)), true),
    (AdviserAddressListId, emptyAnswers, adviserAddress(NormalMode), true, Some(adviserAddress(CheckMode)), true),
    (AdviserAddressId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, schemeSuccess, true, None, true)
  )

  val navigator = new AdviserNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  "AdviserNavigator" must {
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

object AdviserNavigatorSpec {

  val emptyAnswers = UserAnswers(Json.obj())

  def taskList:Call = controllers.routes.SchemeTaskListController.onPageLoad()

  def dataDescriber(answers: UserAnswers): String = answers.toString

  private def adviserAddress(mode: Mode) = controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode)

  private def adviserAddressList(mode: Mode) = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)

  private def adviserPostCodeLookup(mode: Mode) = controllers.register.adviser.routes.AdviserPostCodeLookupController.onPageLoad(mode)

  private def checkYourAnswersPage = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()

  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def task = controllers.register.routes.SchemeSuccessController.onPageLoad()

}

class AdviserNavigatorHnSSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  override lazy val app = new GuiceApplicationBuilder().configure(
    "features.useManagePensionsFrontend" -> true,
    "features.is-hub-enabled" -> true
  ).build()

  private def routesWithWorkingKnowldge = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),

    //Check your answers - back to task list page
    (CheckYourAnswersId, emptyAnswers, taskList, true, None, false)
  )

  "AdviserHsNavigator" must {
    val navigator = new AdviserNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routesWithWorkingKnowldge, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}
