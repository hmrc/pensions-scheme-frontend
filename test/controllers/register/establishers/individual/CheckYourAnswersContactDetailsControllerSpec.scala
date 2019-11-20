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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.establishers.individual.routes.{EstablisherEmailController, EstablisherPhoneController}
import models.Mode.checkMode
import models._
import models.person.PersonName
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.annotations.NoSuspendedCheck
import utils.{CountryOptions, FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersContactDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some("test-srn")
  private val establisherName = PersonName("test", "name")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  private val email = "test@test.com"
  private val phone = "1234"

  private val fullAnswers = UserAnswers().establishersIndividualName(index, establisherName).
    establishersIndividualEmail(index, email = email).establishersIndividualPhone(index, phone = "1234")

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private def answerSection(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = {
    val emailAnswerRow = AnswerRow(
      messages("messages__enterEmail", establisherName.fullName),
      Seq(email),
      answerIsMessageKey = false,
      Some(Link("site.change", EstablisherEmailController.onPageLoad(checkMode(mode), index, srn).url,
        Some(messages("messages__visuallyhidden__dynamic_email_address", establisherName.fullName))))
    )

    val phoneAnswerRow = AnswerRow(
      messages("messages__enterPhoneNumber", establisherName.fullName),
      Seq(phone),
      answerIsMessageKey = false,
      Some(Link("site.change", EstablisherPhoneController.onPageLoad(checkMode(mode), index, srn).url,
        Some(messages("messages__visuallyhidden__dynamic_phone_number", establisherName.fullName))))
    )

    Seq(AnswerSection(None, Seq(emailAnswerRow, phoneAnswerRow)))
  }

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false): String =
    checkYourAnswers(
      frontendAppConfig,
      CYAViewModel(
        answerSections = answerSections,
        href = postUrl,
        schemeName = None,
        returnOverview = false,
        hideEditLinks = false,
        srn = srn,
        hideSaveAndContinueButton = hideButton,
        title = Message("checkYourAnswers.hs.title"),
        h1 = Message("checkYourAnswers.hs.heading")
      )
    )(fakeRequest, messages).toString

  "CheckYourAnswersContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          running(_.overrides(modules(fullAnswers.dataRetrievalAction): _*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersContactDetailsController]
              val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(NormalMode))
          }
        }

        "Update Mode" in {
          running(_.overrides(
            bind[AuthAction].toInstance(FakeAuthAction),
            bind(classOf[AllowAccessActionProvider]).qualifiedWith(classOf[NoSuspendedCheck]).toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].toInstance(fullAnswers.dataRetrievalAction))) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, submitUrl(UpdateMode, srn), hideButton = true)
          }
        }
      }
    }
  }
}


