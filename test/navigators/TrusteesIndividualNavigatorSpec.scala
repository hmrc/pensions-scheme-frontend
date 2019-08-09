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
import controllers.register.trustees.company.routes._
import controllers.register.trustees.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import identifiers.register.trustees.individual.TrusteeNameId
import models.Mode._
import models._
import models.person.PersonName
import org.scalatest.MustMatchers
import org.scalatest.prop.TableFor3
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import TrusteesIndividualNavigatorSpec._

  "TrusteesIndividualNavigator" must {

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalAndUpdateModeRoutes(NormalMode))
    behave like navigatorWithRoutesForMode(CheckMode)(navigator, routesCheckMode(CheckMode))

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, normalAndUpdateModeRoutes(UpdateMode))

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, routesCheckUpdateMode(CheckUpdateMode))
    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, setNewTrusteeIdentifier(routesCheckMode(CheckUpdateMode)))
  }
}

object TrusteesIndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {
  private def setNewTrusteeIdentifier(table: TableFor3[Identifier, UserAnswers, Call]): TableFor3[Identifier, UserAnswers, Call] = table.map(tuple =>
    (tuple._1, tuple._2.set(IsTrusteeNewId(0))(true).asOpt.value, tuple._3)
  )

  private def addTrusteePage(mode: Mode): Call = AddTrusteeController.onPageLoad(mode, None)
  private def sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  def normalAndUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(0))(PersonName("FirstName", "LastName"), addTrusteePage(mode))
    )

  def routesCheckMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(0))(PersonName("FirstName", "LastName"), sessionExpiredPage)
    )

  def routesCheckUpdateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] = {
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(TrusteeNameId(0))(PersonName("FirstName", "LastName"), sessionExpiredPage)
    )
  }

  val navigator: Navigator = injector.instanceOf[TrusteesIndividualNavigator]
}