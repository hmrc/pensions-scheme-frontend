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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import models.{CheckMode, Index, NormalMode}
import org.joda.time.LocalDate
import play.api.mvc.Call
import play.api.test.Helpers._
import utils._
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET" in {
      val result = controller(getMandatoryTrustee).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired page" when {
      "GET" when {
        "trustee name is not present" in {
          val result = controller(getEmptyData).onPageLoad(firstIndex)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

    "redirect to Add Trustee page" when {
      "POST is called" in {
        val result = controller().onSubmit(firstIndex)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {
  val schemeName = "Test Scheme Name"
  val firstIndex = Index(0)
  lazy val trusteeDetailsRoute: String = routes.TrusteeDetailsController.onPageLoad(CheckMode, firstIndex).url
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit(firstIndex)
  lazy val trusteeDetailsSection = AnswerSection(None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq("Test Trustee Name"), answerIsMessageKey = false, trusteeDetailsRoute),
      AnswerRow("messages__common__dob", Seq(s"${DateHelper.formatDate(LocalDate.now)}"), answerIsMessageKey = false, trusteeDetailsRoute)
    )
  )
  lazy val contactDetailsSection = AnswerSection(
    Some("messages__checkYourAnswers__section__contact_details"),
    Seq.empty[AnswerRow]
  )
  val onwardRoute = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions
    )

  lazy val viewAsString: String = check_your_answers(
    frontendAppConfig,
    Seq(
      trusteeDetailsSection,
      contactDetailsSection
    ),
    Some(Message("messages__common__trustee_secondary_header", schemeName)),
    postUrl
  )(fakeRequest, messages).toString
}
