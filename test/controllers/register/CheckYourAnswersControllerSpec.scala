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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.{BenefitsId, SchemeDetailsId, UKBankAccountId}
import models.CheckMode
import models.register.{Benefits, SchemeDetails, SchemeType}
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      fakeNavigator
    )

  private val postUrl = routes.CheckYourAnswersController.onSubmit()

  private val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      BenefitsId.toString -> Benefits.options.head.value,
      UKBankAccountId.toString -> false
    ))
  )

  private val schemeDetailsSection = AnswerSection(
    Some("messages__scheme_details__title"),
    Seq(
      AnswerRow(
        "messages__scheme_details__name_label",
        Seq("Test Scheme Name"),
        answerIsMessageKey = false,
        Some(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url)
      ),
      AnswerRow(
        "messages__scheme_details__type_legend_short",
        Seq(s"messages__scheme_details__type_${SchemeType.SingleTrust}"),
        answerIsMessageKey = true,
        Some(controllers.register.routes.SchemeDetailsController.onPageLoad(CheckMode).url)
      )
    )
  )

  private val schemeBenefitsSection = AnswerSection(
    Some("messages__scheme_benefits_section"),
    Seq(
      AnswerRow(
        "messages__benefits__title",
        Seq(s"messages__benefits__${Benefits.options.head.value}"),
        answerIsMessageKey = true,
        Some(controllers.register.routes.BenefitsController.onPageLoad(CheckMode).url)
      )
    )
  )

  private val bankAccountSection = AnswerSection(
    Some("messages__uk_bank_account_details__title"),
    Seq(
      AnswerRow(
        "uKBankAccount.checkYourAnswersLabel",
        Seq("site.no"),
        answerIsMessageKey = true,
        Some(controllers.register.routes.UKBankAccountController.onPageLoad(CheckMode).url)
      )
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(
      schemeDetailsSection,
      schemeBenefitsSection,
      bankAccountSection
    ),
    Some("messages_cya_secondary_header"),
    postUrl
  )(fakeRequest, messages).toString

}
