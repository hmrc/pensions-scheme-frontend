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

import controllers.actions._
import identifiers.EstablishedCountryId
import identifiers.register._
import identifiers.register.trustees.HaveAnyTrusteesId
import models.CheckMode
import models.register.{Benefits, SchemeDetails, SchemeType}
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.{FakeCountryOptions, FakeNavigator, FakeSectionComplete}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        val result = controller(schemeInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "onSubmit is called" must {
      "redirect to next page" in {
        val result = controller().onSubmit(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(onwardRoute)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      fakeNavigator,
      FakeSectionComplete
    )

  private val postUrl = routes.CheckYourAnswersController.onSubmit()

  private val schemeInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      SchemeNameId.toString -> "Test Scheme",
      SchemeTypeId.toString -> SchemeType.SingleTrust,
      HaveAnyTrusteesId.toString -> true,
      EstablishedCountryId.toString -> "GB",
      DeclarationDutiesId.toString -> false
    ))
  )

  private val beforeYouStart = AnswerSection(
    None,
    Seq(
      AnswerRow(
        "schemeName.checkYourAnswersLabel",
        Seq("Test Scheme"),
        answerIsMessageKey = false,
        Some(controllers.register.routes.SchemeNameController.onPageLoad(CheckMode).url),
        "messages__visuallyhidden__schemeName"
      ),
      AnswerRow(
        "What type of scheme is Test Scheme?",
        Seq(s"messages__scheme_type_${SchemeType.SingleTrust}"),
        answerIsMessageKey = true,
        Some(register.routes.SchemeTypeController.onPageLoad(CheckMode).url),
        "Change the type of scheme Test Scheme is"
      ),
      AnswerRow(
        "Does Test Scheme have any trustees?",
        Seq("site.yes"),
        answerIsMessageKey = true,
        Some(controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(CheckMode).url),
        "Change if Test Scheme has any trustees"
      ),
      AnswerRow(
        "Which country was Test Scheme established in?",
        Seq("GB"),
        answerIsMessageKey = false,
        Some(controllers.routes.EstablishedCountryController.onPageLoad(CheckMode).url),
        "Change the country Test Scheme was established in"
      ),
      AnswerRow(
        "messages__workingKnowledge__title",
        Seq("site.no"),
        answerIsMessageKey = true,
        Some(controllers.routes.WorkingKnowledgeController.onPageLoad(CheckMode).url),
        "messages__visuallyhidden__declarationDuties"
      )
    )
  )

  private def viewAsString(): String = check_your_answers(
    frontendAppConfig,
    Seq(beforeYouStart),
    postUrl
  )(fakeRequest, messages).toString

}
