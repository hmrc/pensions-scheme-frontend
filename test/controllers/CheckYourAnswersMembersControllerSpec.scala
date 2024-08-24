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

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models._
import org.scalatest.OptionValues
import play.api.test.Helpers._

import utils.UserAnswers
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersMembersControllerSpec extends ControllerSpecBase with OptionValues{

  import CheckYourAnswersMembersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(NormalMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode)
      }

      "return OK and NOT display submit button with return to tasklist when in update mode" in {
        val result = controller(data).onPageLoad(UpdateMode, None)(fakeRequest)
        status(result) mustBe OK
        assertNotRenderedById(asDocument(contentAsString(result)), "submit")
      }

      "return OK and DO display submit button with return to tasklist when in normal mode" in {
        val result = controller(data).onPageLoad(NormalMode, srn)(fakeRequest)
        status(result) mustBe OK
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }
    }
  }
}

object CheckYourAnswersMembersControllerSpec extends ControllerSpecBase {

  private val schemeName = "Test Scheme Name"
  private val postUrl = routes.PsaSchemeTaskListController.onPageLoad(NormalMode, srn)
  private val data = UserAnswers().schemeName(schemeName).currentMembers(Members.One).futureMembers(Members.None).dataRetrievalAction

  private val view = injector.instanceOf[checkYourAnswers]
  private def controller(dataRetrievalAction: DataRetrievalAction): CheckYourAnswersMembersController =
    new CheckYourAnswersMembersController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      getEmptyDataPsp,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      controllerComponents,
      view
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

  private def heading(name: String, mode: Mode): String = if (mode == NormalMode) Message("checkYourAnswers.hs.title") else
    Message("messages__membershipDetailsFor", name)

  private def vm(mode: Mode) = CYAViewModel(
    answerSections = Seq(membersSection),
    href = postUrl,
    schemeName = Some(schemeName),
    returnOverview = false,
    hideEditLinks = false,
    srn = None,
    hideSaveAndContinueButton = false,
    title = heading(Message("messages__theScheme").resolve, mode),
    h1 = heading(schemeName, mode)
  )

  private def viewAsString(mode: Mode): String = view(vm(mode))(fakeRequest, messages).toString

}


