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

import connectors.{PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import identifiers.IsAboutMembersCompleteId
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import models.{CheckMode, Link, Members, NormalMode}
import org.scalatest.OptionValues
import play.api.test.Helpers._
import utils.{FakeSectionComplete, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.Future

class CheckYourAnswersMembersControllerSpec extends ControllerSpecBase with OptionValues with MockitoSugar{

  import CheckYourAnswersMembersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(data).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(NormalMode, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SchemeTaskListController.onPageLoad().url
        FakeSectionComplete.verify(IsAboutMembersCompleteId, true)
      }
    }
  }
}

object CheckYourAnswersMembersControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val schemeName = "Test Scheme Name"
  private val postUrl = routes.CheckYourAnswersMembersController.onSubmit(NormalMode, None)
  private val data = UserAnswers().schemeName(schemeName).currentMembers(Members.One).futureMembers(Members.None).dataRetrievalAction

  private val allowAccess: AllowAccessForNonSuspendedUsersActionProvider = new AllowAccessForNonSuspendedUsersActionProvider {

    val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
    val schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
    val updateConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
    val optionSRN: Option[String] = None

    override def apply(srn: Option[String]): AllowAccessForNonSuspendedUsersAction = new AllowAccessForNonSuspendedUsersAction(srn) {
      override protected def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = Future.successful(None)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersMembersController =
    new CheckYourAnswersMembersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      allowAccess
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

  private def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(
      membersSection
    ),
    postUrl,
    Some(schemeName),
    viewOnly = false
  )(fakeRequest, messages).toString

}


