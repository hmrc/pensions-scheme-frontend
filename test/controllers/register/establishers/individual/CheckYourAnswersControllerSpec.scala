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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.establishers.IsEstablisherCompleteId
import models.{CheckMode, Index}
import org.joda.time.LocalDate
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import utils._
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val firstIndex = Index(0)
  val testSchemeName = "Test Scheme Name"

  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  lazy val answers: Seq[AnswerRow] = Seq(
    AnswerRow(
      "messages__establisher_individual_name_cya_label",
      Seq("test first name test last name"),
      answerIsMessageKey = false,
      Some(routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url),
      Message("messages__visuallyhidden__common__name", "test first name test last name")
    ),
    AnswerRow(
      "messages__establisher_individual_dob_cya_label",
      Seq(DateHelper.formatDate(LocalDate.now)),
      answerIsMessageKey = false,
      Some(routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url),
      Message("messages__visuallyhidden__common__dob", "test first name test last name")
    )
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      checkYourAnswersFactory,
      FakeSectionComplete,
      new FakeNavigator(onwardRoute)
    )

  "Check Your Answers Controller" must {

    "return 200 and the correct view for a GET" in {
      val postUrl = routes.CheckYourAnswersController.onSubmit(firstIndex)
      val result = controller().onPageLoad(firstIndex)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe check_your_answers(frontendAppConfig,
        Seq(AnswerSection(None, answers)), Some(testSchemeName), postUrl)(fakeRequest, messages).toString
    }

    "redirect to Session Expired page for a GET when establisher name is not present" in {
      val result = controller(getEmptyData).onPageLoad(firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit(firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "mark establisher as complete on submit" in {
      val result = controller().onSubmit(firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      FakeSectionComplete.verify(IsEstablisherCompleteId(firstIndex), true)
    }

  }

}
