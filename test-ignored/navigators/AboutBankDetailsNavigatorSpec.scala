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
import identifiers.{BankAccountDetailsId, UKBankAccountId}
import models.{BankAccountDetails, NormalMode, UpdateMode}
import models.register.SortCode
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class AboutBankDetailsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AboutBankDetailsNavigatorSpec._

  private def routes = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (UKBankAccountId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (UKBankAccountId, ukBankAccount, ukBankDetailsPage, false, Some(ukBankDetailsPage), false),
    (UKBankAccountId, noUKBankAccount, checkYourAnswersPage, false, Some(checkYourAnswersPage), false),
    (BankAccountDetailsId, emptyAnswers, checkYourAnswersPage, false, Some(checkYourAnswersPage), false),
    (BankAccountDetailsId, ukBankAccountDetails, checkYourAnswersPage, false, Some(checkYourAnswersPage), false)
  )

  val navigator = new AboutBankDetailsNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  "AboutBankDetailsNavigator" must {

    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes, dataDescriber)

    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}
object AboutBankDetailsNavigatorSpec {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val ukBankAccount = UserAnswers(Json.obj()).set(UKBankAccountId)(true).get
  private val noUKBankAccount = UserAnswers(Json.obj()).set(UKBankAccountId)(false).get

  private val bankDetails = BankAccountDetails("test bank", "test account",
    SortCode("34", "45", "67"), "1234567890")

  private val ukBankAccountDetails = UserAnswers(Json.obj()).set(BankAccountDetailsId)(bankDetails).get

  private val ukBankDetailsPage: Call = controllers.routes.BankAccountDetailsController.onPageLoad(NormalMode)
  private val checkYourAnswersPage: Call = controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad()
  private val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}


