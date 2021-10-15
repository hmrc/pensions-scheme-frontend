/*
 * Copyright 2021 HM Revenue & Customs
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

package navigators.trustees.individuals

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import controllers.register.trustees.individual.routes._
import generators.Generators
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models._
import navigators.{Navigator, NavigatorBehaviour}
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  import TrusteesIndividualContactDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "NormalMode" must {
    def navigationForNewTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Next Page"),
        row(TrusteeEmailId(index))(someStringValue, TrusteePhoneController.onPageLoad(NormalMode, index, None)),
        row(TrusteePhoneId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigationForNewTrusteeIndividual, None)
  }

  "CheckMode" must {
    def checkModeRoutes: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(TrusteeEmailId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None)),
        row(TrusteePhoneId(index))(someStringValue, cyaContactDetailsPage(NormalMode, index, None))
      )

    behave like navigatorWithRoutesForMode(CheckMode)(navigator, checkModeRoutes, None)
  }

  "UpdateMode" must {
    def navigationForUpdateModeTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(TrusteeEmailId(index))(someStringValue, TrusteePhoneController.onPageLoad(UpdateMode, index, srn)),
        row(TrusteePhoneId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn))
      )

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigationForUpdateModeTrusteeIndividual, srn)
  }

  "CheckUpdateMode" must {
    def navigationForCheckUpdateModeTrusteeIndividual: TableFor3[Identifier, UserAnswers, Call] =
      Table(
        ("Id", "UserAnswers", "Expected next page"),
        row(TrusteeEmailId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteeEmailId(index))(someStringValue, anyMoreChangesPage(srn)),
        row(TrusteePhoneId(index))(someStringValue, cyaContactDetailsPage(UpdateMode, index, srn), Some(newTrusteeUserAnswers)),
        row(TrusteePhoneId(index))(someStringValue, anyMoreChangesPage(srn))
      )

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigationForCheckUpdateModeTrusteeIndividual, srn)
  }

}

object TrusteesIndividualContactDetailsNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour with Generators {
  private lazy val index            = 0
  private val newTrusteeUserAnswers = UserAnswers().set(IsTrusteeNewId(index))(true).asOpt.value
  private val srn                   = Some("srn")

  private def cyaContactDetailsPage(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersIndividualContactDetailsController.onPageLoad(Mode.journeyMode(mode), index, srn)

}
