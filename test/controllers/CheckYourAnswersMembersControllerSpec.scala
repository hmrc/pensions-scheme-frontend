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

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.IsAboutMembersCompleteId
import models.{CheckMode, Members}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import utils.{FakeSectionComplete, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersMembersControllerSpec extends ControllerSpecBase with OptionValues{

  import CheckYourAnswersMembersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SchemeTaskListController.onPageLoad().url
        FakeSectionComplete.verify(IsAboutMembersCompleteId, true)
      }
    }
  }
}

object CheckYourAnswersMembersControllerSpec extends ControllerSpecBase {

  private val schemeName = "Test Scheme Name"
  private val postUrl = routes.CheckYourAnswersMembersController.onSubmit()
  private val data = UserAnswers().schemeName(schemeName).currentMembers(Members.One).futureMembers(Members.None).dataRetrievalAction

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersMembersController =
    new CheckYourAnswersMembersController(
      frontendAppConfigWithHubEnabled,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete
    )

  private val membersSection = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("messages__current_members_cya_label", schemeName),
        Seq(s"messages__members__${Members.One}"),
        answerIsMessageKey = true,
        Some(controllers.routes.CurrentMembersController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__current_members_change", schemeName)
      ),
      AnswerRow(
        messages("messages__future_members_cya_label", schemeName),
        Seq(s"messages__members__${Members.None}"),
        answerIsMessageKey = true,
        Some(controllers.routes.FutureMembersController.onPageLoad(CheckMode).url),
        messages("messages__visuallyhidden__future_members_change", schemeName)
      )
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfigWithHubEnabled,
    Seq(
      membersSection
    ),
    postUrl,
    Some(schemeName)
  )(fakeRequest, messages).toString

}


