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

import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import controllers.ControllerSpecBase
import models.Index
import org.joda.time.LocalDate
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import utils.{CheckYourAnswersFactory, CountryOptions, DateHelper, InputOption}
import viewmodels.{AnswerRow, AnswerSection}
import play.api.test.Helpers._
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val firstIndex = Index(0)
  val testSchemeName = "Test Scheme Name"

  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  val answers: Seq[AnswerRow] = Seq(
    AnswerRow(
      "messages__establisher_individual_name_cya_label",
      Seq("test first name test last name"),
      answerIsMessageKey = false,
      "/pensions-scheme/register/establishers/1/individual/changeEstablisherDetails"
    ),
    AnswerRow(
      "messages__establisher_individual_dob_cya_label",
      Seq(DateHelper.formatDate(LocalDate.now)),
      answerIsMessageKey = false,
      "/pensions-scheme/register/establishers/1/individual/changeEstablisherDetails"
    )
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): CheckYourAnswersController =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction, dataRetrievalAction,
      new DataRequiredActionImpl, checkYourAnswersFactory)

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
  }

}
