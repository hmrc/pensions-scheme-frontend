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
import models.{CheckMode, Index}
import models.register.CountryOptions
import org.joda.time.LocalDate
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import utils.{DateHelper, InputOption}
import viewmodels.{AnswerRow, AnswerSection}
import play.api.test.Helpers._
import views.html.register.establishers.individual.check_your_answers_individual

class CheckYourAnswersIndividualControllerSpec extends ControllerSpecBase {

  val inputOptions = Seq(InputOption("GB", "United Kingdom"))
  val countryOptions: CountryOptions = new CountryOptions(inputOptions)
  val firstIndex = Index(0)
  val testSchemeName = "Test Scheme Name"

  val seqAnswers: Seq[AnswerRow] = Seq(AnswerRow("establisherDetails.name.checkYourAnswersLabel",
    Seq("test first name test last name"), false,
    controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url),
    AnswerRow("establisherDetails.dateOfBirth.checkYourAnswersLabel",
      Seq(DateHelper.formatDate(LocalDate.now)), false,
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url))

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): CheckYourAnswersIndividualController =
    new CheckYourAnswersIndividualController(frontendAppConfig, messagesApi, FakeAuthAction, dataRetrievalAction,
      new DataRequiredActionImpl, countryOptions)

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET" in {
      val result = controller().onPageLoad(firstIndex)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe check_your_answers_individual(frontendAppConfig,
        Seq(AnswerSection(None, seqAnswers)), testSchemeName)(fakeRequest, messages).toString
    }

    "redirect to Session Expired page for a GET when establisher name is not present" in {
      val result = controller(getEmptyData).onPageLoad(firstIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if not existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(firstIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
