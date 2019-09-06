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

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import identifiers.IsAboutMembersCompleteId
import models._
import org.scalatest.OptionValues
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.UserAnswers
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers_old

class CheckYourAnswersMembersControllerSpec extends ControllerSpecBase with OptionValues{

  import CheckYourAnswersMembersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and NOT display submit button with return to tasklist when in update mode" in {
        val result = controller(data).onPageLoad(UpdateMode, None)(fakeRequest)
        status(result) mustBe OK
        assertNotRenderedById(asDocument(contentAsString(result)), "submit")
      }

      "return OK and DO display submit button with return to tasklist when in normal mode" in {
        val result = controller(data).onPageLoad(NormalMode, None)(fakeRequest)
        status(result) mustBe OK
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(NormalMode, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url
        FakeUserAnswersService.verify(IsAboutMembersCompleteId, true)
      }
    }
  }
}

object CheckYourAnswersMembersControllerSpec extends ControllerSpecBase {

  private val schemeName = "Test Scheme Name"
  private val postUrl = routes.CheckYourAnswersMembersController.onSubmit(NormalMode, None)
  private val data = UserAnswers().schemeName(schemeName).currentMembers(Members.One).futureMembers(Members.None).dataRetrievalAction

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersMembersController =
    new CheckYourAnswersMembersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService
    )

  private val membersSection = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("messages__current_members_cya_label", schemeName),
        Seq(s"messages__members__${Members.One}"),
        answerIsMessageKey = true,
        Some(Link("site.change", controllers.routes.CurrentMembersController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__current_members_change", schemeName))))
      ),
      AnswerRow(
        messages("messages__future_members_cya_label", schemeName),
        Seq(s"messages__members__${Members.None}"),
        answerIsMessageKey = true,
        Some(Link("site.change", controllers.routes.FutureMembersController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__future_members_change", schemeName))))
      )
    )
  )

  private def viewAsString(): String = check_your_answers_old(
    frontendAppConfig,
    Seq(
      membersSection
    ),
    postUrl,
    Some(schemeName),
    hideEditLinks = false,
    hideSaveAndContinueButton = false
  )(fakeRequest, messages).toString

}


