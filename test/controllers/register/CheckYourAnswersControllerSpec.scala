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

import utils.FakeCountryOptions
import controllers.actions._
import play.api.test.Helpers._
import models.NormalMode
import controllers.ControllerSpecBase
import identifiers.register.{BenefitsId, SchemeDetailsId, UKBankAccountId}
import models.register.{Benefits, SchemeDetails, SchemeType}
import play.api.libs.json.Json
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, new FakeCountryOptions)

  val postUrl = routes.CheckYourAnswersController.onSubmit()
  val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeDetailsId.toString ->
        SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
      BenefitsId.toString -> Benefits.options.head.value,
      UKBankAccountId.toString -> false
    ))
  )

  val schemeDetailsSection = AnswerSection(
    Some("messages__scheme_details__title"),
    Seq(
      AnswerRow("messages__scheme_details__name_label", Seq("Test Scheme Name"), false, "/pensions-scheme/register/changeSchemeDetails"),
      AnswerRow("messages__scheme_details__type_legend_short",
        Seq(s"messages__scheme_details__type_${SchemeType.SingleTrust}"), true, "/pensions-scheme/register/changeSchemeDetails")
    )
  )
  val schemeBenefitsSection = AnswerSection(
    Some("messages__scheme_benefits_section"),
    Seq(AnswerRow(
      "messages__benefits__title", Seq(s"messages__benefits__${Benefits.options.head.value}"), true, "/pensions-scheme/register/changeBenefits"))
  )
  val bankAccountSection = AnswerSection(
    Some("messages__uk_bank_account_details__title"),
    Seq(AnswerRow(
      "uKBankAccount.checkYourAnswersLabel", Seq("site.no"), true, "/pensions-scheme/register/changeUKBankAccount"))
  )

  def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(
      schemeDetailsSection,
      schemeBenefitsSection,
      bankAccountSection
    ),
    Some("messages_cya_secondary_header"),
    postUrl
  )(fakeRequest, messages).toString

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to add establisher page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url)
      }
    }
  }
}




