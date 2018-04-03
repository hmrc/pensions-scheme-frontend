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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.establishers.company._
import models.{CheckMode, Index}
import org.joda.time.LocalDate
import play.api.test.Helpers._
import utils.{CheckYourAnswersFactory, CountryOptions, DateHelper, InputOption}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val countryOptions: CountryOptions = new CountryOptions(Seq(InputOption("GB", "United Kingdom")))
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val testSchemeName = "Test Scheme Name"

  val checkYourAnswersFactory = new CheckYourAnswersFactory(countryOptions)

  def postUrl = director.routes.CheckYourAnswersController.onSubmit(establisherIndex)

  lazy val answersDirectorDetails: Seq[AnswerRow] =
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first middle last"), false,
        director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex)).url
      ),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(new LocalDate(1990, 2, 2))), false,
        director.routes.DirectorDetailsController.onPageLoad(CheckMode, Index(establisherIndex), Index(directorIndex)).url
      )
    )

  lazy val answers = Seq(
    AnswerSection(Some("messages__director__cya__details_heading"), answersDirectorDetails),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq.empty[AnswerRow])
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany) =
    new director.CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, checkYourAnswersFactory)

  def viewAsString() = check_your_answers(frontendAppConfig, answers, Some(testSchemeName), postUrl)(fakeRequest, messages).toString

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getMandatoryEstablisherCompanyDirector).onPageLoad(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired page for a GET when establisher name is not present" in {
      val result = controller(getEmptyData).onPageLoad(establisherIndex, directorIndex)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
