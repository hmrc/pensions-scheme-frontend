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
import identifiers.AnyMoreChangesId
import models.UpdateMode
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class VariationsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import VariationsNavigatorSpec._

  private def updateRoutes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AnyMoreChangesId, someMoreChanges, variationsTaskList, false, None, false),
    (AnyMoreChangesId, noMoreChanges, variationsTaskList, false, None, false),
    (AnyMoreChangesId, emptyAnswers, sessionExpired, false, None, false)
  )

  "VariationsNavigator" must {
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode, srn)
    behave like nonMatchingNavigator(navigator)
  }
}




object VariationsNavigatorSpec extends SpecBase with OptionValues {

  private val navigator = new VariationsNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
  private val emptyAnswers = UserAnswers(Json.obj())
  val srnValue = "S123"
  val srn = Some(srnValue)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def variationsTaskList = controllers.routes.PSASchemeDetailsController.onPageLoad(srnValue)

  private def none: Call = controllers.routes.IndexController.onPageLoad()
  val someMoreChanges = UserAnswers(Json.obj()).set(AnyMoreChangesId)(true).asOpt.value
  val noMoreChanges = UserAnswers(Json.obj()).set(AnyMoreChangesId)(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
