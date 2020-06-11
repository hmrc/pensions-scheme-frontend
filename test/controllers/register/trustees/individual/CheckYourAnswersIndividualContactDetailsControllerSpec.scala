/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.individual.{TrusteeEmailId, TrusteeNameId, TrusteePhoneId}
import models.Mode.checkMode
import models._
import models.person.PersonName
import navigators.Navigator
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersIndividualContactDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {
  private val index = Index(0)
  private val srn = Some("test-srn")
  private val trusteeName = "test name"
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private val fullAnswers = UserAnswers().set(TrusteeEmailId(0))(value = "test@test.com").flatMap(_.set(TrusteePhoneId(0))(value = "12345"))
    .flatMap(_.set(TrusteeNameId(0))(PersonName("test", "name"))).asOpt.value

  private def answerSection(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = {
    Seq(AnswerSection(None, StringCYA[TrusteeEmailId](
      Some(messages("messages__enterEmail", trusteeName)),
      Some(messages("messages__visuallyhidden__dynamic_email_address", trusteeName))
    )().row(TrusteeEmailId(index))(routes.TrusteeEmailController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers) ++
      StringCYA[TrusteePhoneId](
        Some(messages("messages__enterPhoneNumber", trusteeName)),
        Some(messages("messages__visuallyhidden__dynamic_phone_number", trusteeName))
      )().row(TrusteePhoneId(index))(
        routes.TrusteePhoneController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false,
                   title:Message, h1:Message): String =
    view(CYAViewModel(
      answerSections = answerSections,
      href = postUrl,
      schemeName = None,
      returnOverview = false,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = hideButton,
      title = title,
      h1 = h1
    ))(fakeRequest, messages).toString

  "CheckYourAnswersIndividualContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
            title = Message("checkYourAnswers.hs.heading"),
            h1 = Message("checkYourAnswers.hs.heading"))
          app.stop()
        }

        "Update Mode" in {
          running(_.overrides(
            bind[Navigator].toInstance(FakeNavigator),
            bind[AuthAction].toInstance(FakeAuthAction),
            bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].to(fullAnswers.dataRetrievalAction),
            bind[AllowChangeHelper].toInstance(allowChangeHelper(saveAndContinueButton = true)),
            bind[AllowAccessActionProvider].qualifiedWith(classOf[NoSuspendedCheck]).to(FakeAllowAccessProvider())
          )) {
            app =>

              val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

              status(result) mustBe OK
              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, postUrl = submitUrl(UpdateMode, srn), hideButton = true,
                title = Message("messages__contactDetailsFor", Message("messages__thePerson")),
                h1 = Message("messages__contactDetailsFor", trusteeName))
              app.stop()
          }
        }
      }
    }
  }
}

