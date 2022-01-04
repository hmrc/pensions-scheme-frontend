/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.{BankAccountDetailsId, Identifier, UKBankAccountId}
import models.register.SortCode
import models.{BankAccountDetails, CheckMode, NormalMode}
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AboutBankDetailsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBankDetailsNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "AboutBankDetailsNavigator" when {
    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(UKBankAccountId)(true, ukBankDetailsPage),
          row(UKBankAccountId)(false, checkYourAnswersPage),
          row(BankAccountDetailsId)(bankDetails, checkYourAnswersPage)
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(UKBankAccountId)(true, ukBankDetailsPage),
          row(UKBankAccountId)(false, checkYourAnswersPage),
          row(BankAccountDetailsId)(bankDetails, checkYourAnswersPage)
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }
  }

}
object AboutBankDetailsNavigatorSpec {
  private val bankDetails = BankAccountDetails(SortCode("34", "45", "67"), "1234567890")

  private val ukBankDetailsPage: Call    = controllers.routes.BankAccountDetailsController.onPageLoad(NormalMode)
  private val checkYourAnswersPage: Call = controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad()

}
