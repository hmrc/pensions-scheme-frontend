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

import AdviserNavigatorSpec._
import identifiers.register.adviser._
import models.{CheckMode, Mode, NormalMode}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class AdviserNavigatorSpec extends WordSpec with MustMatchers with NavigatorBehaviour {

  private val routes = Table(
    ("Id",                            "User Answers",       "Next Page (Normal Mode)",              "Next Page (Check Mode)"),
    (AdviserDetailsId,                emptyAnswers,         adviserPostCodeLookup(NormalMode),      Some(checkYourAnswersPage)),
    (AdviserAddressPostCodeLookupId,  emptyAnswers,         adviserAddressList(NormalMode),         Some(adviserAddressList(CheckMode))),
    (AdviserAddressListId,            emptyAnswers,         adviserAddress(NormalMode),             Some(adviserAddress(CheckMode))),
    (AdviserAddressId,                emptyAnswers,         checkYourAnswersPage,                   Some(checkYourAnswersPage)),
    (CheckYourAnswersId,              emptyAnswers,         schemeSuccess,                          None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes, dataDescriber)
  }

}

object AdviserNavigatorSpec {

  private val navigator = new AdviserNavigator()

  private val emptyAnswers = UserAnswers(Json.obj())

  private def adviserAddress(mode: Mode) = controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode)
  private def adviserAddressList(mode: Mode) = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)
  private def adviserPostCodeLookup(mode: Mode) = controllers.register.adviser.routes.AdviserPostCodeLookupController.onPageLoad(mode)
  private def checkYourAnswersPage = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()
  private def schemeSuccess = controllers.register.routes.SchemeSuccessController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
