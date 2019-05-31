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
import connectors.FakeUserAnswersCacheConnector
import identifiers._
import models.{CheckMode, Mode, NormalMode, UpdateMode}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class WorkingKnowledgeNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import WorkingKnowledgeNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AdviserNameId, emptyAnswers, adviserEmail(NormalMode), true, Some(adviserCYA), true),
    (AdviserEmailId, emptyAnswers, adviserPhone(NormalMode), true, Some(adviserCYA), true),
    (AdviserPhoneId, emptyAnswers, adviserPostCodeLookup(NormalMode), true, Some(adviserCYA), true),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressList(NormalMode), true, Some(adviserAddressList(CheckMode)), true),
    (AdviserAddressListId, emptyAnswers, adviserAddress(NormalMode), true, Some(adviserAddress(CheckMode)), true),
    (AdviserAddressId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (AdviserCheckYourAnswersId, emptyAnswers, taskList, true, None, false)
  )

  "WorkingKnowledgeNavigator" must {
    val navigator = new WorkingKnowledgeNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object WorkingKnowledgeNavigatorSpec {

  private val emptyAnswers = UserAnswers(Json.obj())

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def adviserAddress(mode: Mode) = controllers.routes.AdviserAddressController.onPageLoad(mode)

  private def adviserAddressList(mode: Mode) = controllers.routes.AdviserAddressListController.onPageLoad(mode)

  private def adviserPostCodeLookup(mode: Mode) = controllers.routes.AdviserPostCodeLookupController.onPageLoad(mode)

  private def checkYourAnswersPage = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()

  private def adviserEmail(mode: Mode): Call = controllers.routes.AdviserEmailAddressController.onPageLoad(NormalMode)
  private def adviserPhone(mode: Mode): Call = controllers.routes.AdviserPhoneController.onPageLoad(NormalMode)

  private def adviserCYA: Call = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()

}


