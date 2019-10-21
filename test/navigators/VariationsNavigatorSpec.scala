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
import models.{NormalMode, UpdateMode}
import org.scalatest.OptionValues
import play.api.libs.json.Json
import utils.UserAnswers

class VariationsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import VariationsNavigatorSpec._

  private def updateRoutes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AnyMoreChangesId, someMoreChanges, variationsTaskList, false, None, false),
    (AnyMoreChangesId, noMoreChangesWithComplete, declaration, false, None, false),
    (AnyMoreChangesId, noMoreChangesWithIncomplete, stillChanges, false, None, false),
    (AnyMoreChangesId, emptyAnswers, sessionExpired, false, None, false)
  )

  "VariationsNavigator" must {
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode, srn)
    behave like nonMatchingNavigator(navigator, UpdateMode)
    behave like nonMatchingNavigator(navigator, NormalMode)
  }
}




object VariationsNavigatorSpec extends SpecBase with OptionValues {

  private val navigator = new VariationsNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)
  private val emptyAnswers = UserAnswers(Json.obj())
  val srnValue = "S123"
  val srn = Some(srnValue)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def variationsTaskList = controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn)
  private def stillChanges = controllers.register.routes.StillNeedDetailsController.onPageLoad(srn)
  private def declaration = controllers.routes.VariationDeclarationController.onPageLoad(srn)

  val someMoreChanges = UserAnswers(Json.obj()).set(AnyMoreChangesId)(true).asOpt.value

  val noMoreChangesWithComplete = UserAnswers().set(AnyMoreChangesId)(false).flatMap(
    _.set(BenefitsSecuredByInsuranceId)(false).flatMap(
    _.set(InsuranceDetailsChangedId)(true)
  )).asOpt.value

  val noMoreChangesWithIncomplete = UserAnswers().set(AnyMoreChangesId)(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
