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

package controllers

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.{BankAccountDetailsId, SchemeNameId, UKBankAccountId}
import models.register._
import models.{BankAccountDetails, CheckMode, Link, NormalMode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersBankDetailsControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersBankDetailsControllerSpec._

  "CheckYourAnswersBankDetailsController Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

object CheckYourAnswersBankDetailsControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersBankDetailsController =
    new CheckYourAnswersBankDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions
    )

  private val postUrl = routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  val bankDetails = BankAccountDetails("test bank name", "test account name",
    SortCode("34", "45", "67"), "test account number")

  private val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      BankAccountDetailsId.toString -> Json.toJson(bankDetails),
      UKBankAccountId.toString -> true,
      SchemeNameId.toString -> "Test Scheme Name"
    ))
  )


  private val bankAccountSection = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("uKBankAccount.checkYourAnswersLabel", "Test Scheme Name"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.UKBankAccountController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__uKBankAccount", "Test Scheme Name"))))
      ),
      AnswerRow(
        messages("uKBankDetails.checkYourAnswersLabel", "Test Scheme Name"),
        Seq(bankDetails.bankName,
          bankDetails.accountName,
          s"${bankDetails.sortCode.first}-${bankDetails.sortCode.second}-${bankDetails.sortCode.third}",
          bankDetails.accountNumber),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.BankAccountDetailsController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__uKBankDetails", "Test Scheme Name"))))
      )
    )
  )

  val vm = CYAViewModel(
    answerSections = Seq(bankAccountSection),
    href = postUrl,
    schemeName = Some("Test Scheme Name"),
    returnOverview = false,
    hideEditLinks = false,
    srn = None,
    hideSaveAndContinueButton = false,
    title = Message("checkYourAnswers.hs.title"),
    h1 = Message("checkYourAnswers.hs.title")
  )

  private def viewAsString(): String = checkYourAnswers(frontendAppConfig, vm)(fakeRequest, messages).toString

}


