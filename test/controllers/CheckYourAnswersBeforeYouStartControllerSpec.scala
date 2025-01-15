/*
 * Copyright 2024 HM Revenue & Customs
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
import models._
import models.register.SchemeType
import play.api.mvc.Call
import play.api.test.Helpers._

import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersBeforeYouStartControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view with return to tasklist" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }

      "return OK and the correct view with return to Manage" in {
        val result = controller(schemeInfo).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsStringWithReturnToManage
      }

      "return OK and NOT display submit button with return to tasklist when in update mode" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(UpdateMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
        status(result) mustBe OK
        assertNotRenderedById(asDocument(contentAsString(result)), "submit")
      }

      "return OK and DO display submit button with return to tasklist when in normal mode" in {
        val result = controller(schemeInfoWithCompleteFlag).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
        status(result) mustBe OK
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }

    }

  }
}

object CheckYourAnswersBeforeYouStartControllerSpec extends ControllerSpecBase {

  private val view = injector.instanceOf[checkYourAnswers]
  private def controller(
                          dataRetrievalAction: DataRetrievalAction
                        ): CheckYourAnswersBeforeYouStartController =
    new CheckYourAnswersBeforeYouStartController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      getEmptyDataPsp,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      controllerComponents,
      view
    )

  private def postUrl: Call = routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)

  private val schemeInfo = UserAnswers().schemeName(schemeName = "Test Scheme").dataRetrievalAction

  private val schemeInfoWithCompleteFlag = UserAnswers().schemeName(schemeName = "Test Scheme").
    schemeType(SchemeType.SingleTrust).establishedCountry(country = "GB").
    declarationDuties(haveWorkingKnowledge = true).dataRetrievalAction

  private val beforeYouStartIncomplete = AnswerSection(
    None,
    Seq(
      AnswerRow(
        "schemeName.checkYourAnswersLabel",
        Seq("Test Scheme"),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.SchemeNameController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__schemeName"))))
      )))

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
        messages("schemeEstablishedCountry.checkYourAnswersLabel", "Test Scheme"),
        Seq("Country of GB"),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.EstablishedCountryController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__schemeEstablishedCountry", "Test Scheme"))))
      ),
      AnswerRow(
        "messages__workingKnowledge__title",
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(Link("site.change", routes.WorkingKnowledgeController.onPageLoad(CheckMode).url,
          Some("messages__visuallyhidden__declarationDuties")))
      )
    )
  )

  private def vm(returnOverview: Boolean, data: AnswerSection) = CYAViewModel(
    answerSections = Seq(data),
    href = postUrl,
    schemeName = Some("Test Scheme"),
    returnOverview = returnOverview,
    hideEditLinks = false,
    srn = EmptyOptionalSchemeReferenceNumber,
    hideSaveAndContinueButton = false,
    title = Message("checkYourAnswers.hs.title"),
    h1 =  Message("checkYourAnswers.hs.title")
  )

  private def viewAsString: String =
    view(vm(returnOverview = false, beforeYouStart))(fakeRequest, messages).toString

  private def viewAsStringWithReturnToManage: String =
    view(vm(returnOverview = true, beforeYouStartIncomplete))(fakeRequest, messages).toString

}
