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

package controllers

import controllers.actions._
import identifiers.register._
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.{EstablishedCountryId, IsBeforeYouStartCompleteId, SchemeNameId, SchemeTypeId}
import models.CheckMode
import models.register.SchemeType
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeSectionComplete}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersBeforeYouStartControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view with return to tasklist" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and the correct view with return to Manage" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsStringWithReturnToManage()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeSectionComplete.verify(IsBeforeYouStartCompleteId, true)
      }
    }

  }
}

object CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.SchemeTaskListController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersBeforeYouStartController =
    new CheckYourAnswersBeforeYouStartController(
      frontendAppConfigWithHubEnabled,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      FakeSectionComplete
    )

  private val postUrl = routes.CheckYourAnswersBeforeYouStartController.onSubmit()

  private val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeNameId.toString -> "Test Scheme",
      SchemeTypeId.toString -> SchemeType.SingleTrust,
      HaveAnyTrusteesId.toString -> true,
      EstablishedCountryId.toString -> "GB",
      identifiers.DeclarationDutiesId.toString -> false
    ))
  )

  private val schemeInfoWithCompleteFlag = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeNameId.toString -> "Test Scheme",
      SchemeTypeId.toString -> SchemeType.SingleTrust,
      HaveAnyTrusteesId.toString -> true,
      EstablishedCountryId.toString -> "GB",
      identifiers.DeclarationDutiesId.toString -> false,
      identifiers.IsBeforeYouStartCompleteId.toString -> true
    ))
  )

  private val beforeYouStart = AnswerSection(
    None,
    Seq(
      AnswerRow(
        "schemeName.checkYourAnswersLabel",
        Seq("Test Scheme"),
        answerIsMessageKey = false,
        Some(routes.SchemeNameController.onPageLoad(CheckMode).url),
        "messages__visuallyhidden__schemeName"
      ),
      AnswerRow(
        messages("schemeType.checkYourAnswersLabel", "Test Scheme"),
        Seq(s"messages__scheme_type_${SchemeType.SingleTrust}"),
        answerIsMessageKey = true,
        Some(routes.SchemeTypeController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__schemeType", "Test Scheme")
      ),
      AnswerRow(
        messages("haveAnyTrustees.checkYourAnswersLabel", "Test Scheme"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__haveAnyTrustees", "Test Scheme")
      ),
      AnswerRow(
        messages("schemeEstablishedCountry.hns_checkYourAnswersLabel", "Test Scheme"),
        Seq("Country of GB"),
        answerIsMessageKey = false,
        Some(routes.EstablishedCountryController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__hns_schemeEstablishedCountry", "Test Scheme")
      ),
      AnswerRow(
        "messages__workingKnowledge__title",
        Seq("site.no"),
        answerIsMessageKey = true,
        Some(routes.WorkingKnowledgeController.onPageLoad(CheckMode).url),
        "messages__visuallyhidden__declarationDuties"
      )
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfigWithHubEnabled,
    Seq(beforeYouStart),
    postUrl,
    Some("Test Scheme")
  )(fakeRequest, messages).toString

  private def viewAsStringWithReturnToManage(): String = check_your_answers(
    frontendAppConfigWithHubEnabled,
    Seq(beforeYouStart),
    postUrl,
    Some("Test Scheme"),
    returnOverview=true
  )(fakeRequest, messages).toString

}
