/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import identifiers._
import identifiers.racdac.{ContractOrPolicyNumberId, DeclarationId, RACDACNameId}
import models.{CheckMode, NormalMode}
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class RACDACNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RACDACNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "RACDACNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(RACDACNameId)(someStringValue, contractOrPolicyNumberPage),
          row(ContractOrPolicyNumberId)(someStringValue, cyaPage),
          row(DeclarationId)(true, successPage)
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(RACDACNameId)(someStringValue, cyaPage),
          row(ContractOrPolicyNumberId)(someStringValue, cyaPage)
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }
  }
}

object RACDACNavigatorSpec {
  private val contractOrPolicyNumberPage: Call      = controllers.racdac.routes.ContractOrPolicyNumberController.onPageLoad(NormalMode)
  private val cyaPage: Call      = controllers.racdac.routes.CheckYourAnswersController.onPageLoad(NormalMode, None)
  private val successPage: Call      = controllers.racdac.routes.SchemeSuccessController.onPageLoad()
}
