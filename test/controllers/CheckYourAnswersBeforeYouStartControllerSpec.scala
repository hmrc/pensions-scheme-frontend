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
import identifiers.register.trustees.HaveAnyTrusteesId
import identifiers.{EstablishedCountryId, IsBeforeYouStartCompleteId, SchemeNameId, SchemeTypeId}
import models._
import models.register.SchemeType
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeCountryOptions
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers_old

class CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersBeforeYouStartControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view with return to tasklist" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and the correct view with return to Manage" in {
        val result = controller(schemeInfo).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsStringWithReturnToManage()
      }

      "return OK and NOT display submit button with return to tasklist when in update mode" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(UpdateMode, None)(fakeRequest)
        status(result) mustBe OK
        assertNotRenderedById(asDocument(contentAsString(result)), "submit")
      }

      "return OK and DO display submit button with return to tasklist when in normal mode" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(NormalMode, None)(fakeRequest)
        status(result) mustBe OK
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }

    }
  }
}

object CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersBeforeYouStartController =
    new CheckYourAnswersBeforeYouStartController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      FakeUserAnswersService
    )

  private def postUrl(mode:Mode) = controllers.routes.SchemeTaskListController.onPageLoad(mode, None)

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
        Some(Link("site.change", routes.SchemeNameController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__schemeName"))))
      ),
      AnswerRow(
        messages("schemeType.checkYourAnswersLabel", "Test Scheme"),
        Seq(s"messages__scheme_type_${SchemeType.SingleTrust}"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.SchemeTypeController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__schemeType", "Test Scheme"))))
      ),
      AnswerRow(
        messages("haveAnyTrustees.checkYourAnswersLabel", "Test Scheme"),
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__haveAnyTrustees", "Test Scheme"))))
      ),
      AnswerRow(
        messages("schemeEstablishedCountry.hns_checkYourAnswersLabel", "Test Scheme"),
        Seq("Country of GB"),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.EstablishedCountryController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__hns_schemeEstablishedCountry", "Test Scheme"))))
      ),
      AnswerRow(
        "messages__workingKnowledge__title",
        Seq("site.no"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.WorkingKnowledgeController.onPageLoad(CheckMode).url,
          Some("messages__visuallyhidden__declarationDuties")))
      )
    )
  )

  private def viewAsString(): String = check_your_answers_old(
    frontendAppConfig,
    Seq(beforeYouStart),
    postUrl(NormalMode),
    Some("Test Scheme"),
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    mode = NormalMode
  )(fakeRequest, messages).toString

  private def viewAsStringWithReturnToManage(): String = check_your_answers_old(
    frontendAppConfig,
    Seq(beforeYouStart),
    postUrl(NormalMode),
    Some("Test Scheme"),
    returnOverview=true,
    hideEditLinks = false,
    hideSaveAndContinueButton = false
  )(fakeRequest, messages).toString

}
