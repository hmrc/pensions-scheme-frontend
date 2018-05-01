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

package controllers.register.adviser

import connectors.{FakeDataCacheConnector, PensionsSchemeConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import models.CheckMode
import models.register.SchemeSubmissionResponse
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getMandatoryAdviser).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {
  val schemeName = "Test Scheme Name"
  lazy val adviserDetailsRoute: String = routes.AdviserDetailsController.onPageLoad(CheckMode).url
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
  lazy val adviserSection = AnswerSection(None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq("name"), answerIsMessageKey = false, adviserDetailsRoute),
      AnswerRow("messages__adviserDetails__email", Seq("email"), answerIsMessageKey = false, adviserDetailsRoute)
      )
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("test-scheme-id")

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
      Future.successful(validSchemeSubmissionResponse)
    }
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute),
      new FakeCountryOptions,
      fakePensionsSchemeConnector
    )

  lazy val viewAsString: String = check_your_answers(
    frontendAppConfig,
    Seq(adviserSection),
    Some(Message("messages__adviser__secondary_heading")),
    postUrl
  )(fakeRequest, messages).toString

}
