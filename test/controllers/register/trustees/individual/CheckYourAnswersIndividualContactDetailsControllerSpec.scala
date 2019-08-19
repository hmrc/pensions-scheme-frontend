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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.individual.{TrusteeEmailId, TrusteeNameId, TrusteePhoneId}
import models.Mode.checkMode
import models._
import models.person.PersonName
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersIndividualContactDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {
  private val index = Index(0)
  private val srn = Some("test-srn")
  private val trusteeName = "test name"
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    routes.CheckYourAnswersIndividualContactDetailsController.onSubmit(mode, index, srn)

  private val fullAnswers = UserAnswers().set(TrusteeEmailId(0))(value = "test@test.com").flatMap(_.set(TrusteePhoneId(0))(value = "12345"))
    .flatMap(_.set(TrusteeNameId(0))(PersonName("test", "name"))).asOpt.value

  private def answerSection(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = {
    Seq(AnswerSection(None, StringCYA[TrusteeEmailId](
      Some(messages("messages__common_email__heading", trusteeName)),
      Some(messages("messages__common_email__visually_hidden_change_label", trusteeName))
    )().row(TrusteeEmailId(index))(routes.TrusteeEmailController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers) ++
      StringCYA[TrusteePhoneId](
        Some(messages("messages__common_phone__heading", trusteeName)),
        Some(messages("messages__common_phone__visually_hidden_change_label", trusteeName))
      )().row(TrusteePhoneId(index))(
        routes.TrusteePhoneController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers)
    ))
  }

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false): String =
    check_your_answers(frontendAppConfig, answerSections, postUrl, None, hideEditLinks = false,
      srn = srn, hideSaveAndContinueButton = hideButton)(fakeRequest, messages).toString

  "CheckYourAnswersIndividualContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode))
          app.stop()
        }

        "Update Mode" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true).overrides(
            bind[AllowChangeHelper].toInstance(allowChangeHelper(saveAndContinueButton = true))
          ).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
          val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, postUrl = submitUrl(UpdateMode, srn), hideButton = true)
          app.stop()
        }
      }
    }

    "on a POST" must {
      "redirect to task list page" in {
        val app = applicationBuilder(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true).build()

        val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
        val result = controller.onSubmit(NormalMode, index, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)
        app.stop()
      }
    }
  }
}

