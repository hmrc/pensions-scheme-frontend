/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.partnership.{PartnershipEmailId, PartnershipPhoneNumberId}
import models.Mode.checkMode
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersPartnershipContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some("test-srn")
  private val partnershipDetails = PartnershipDetails("Test partnership")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val fullAnswers = UserAnswers().establisherPartnershipDetails(index, partnershipDetails).
    set(PartnershipEmailId(index))("test@test.com").asOpt.value.
    set(PartnershipPhoneNumberId(index))("1234").asOpt.value

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private def answerSection(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = {
    Seq(AnswerSection(None,
      StringCYA[PartnershipEmailId](
        Some(messages("messages__enterEmail", partnershipDetails.name)),
        Some(messages("messages__visuallyhidden__dynamic_email_address", partnershipDetails.name))
      )().row(PartnershipEmailId(index))(
        routes.PartnershipEmailController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers) ++

        StringCYA[PartnershipPhoneNumberId](
          Some(messages("messages__enterPhoneNumber", partnershipDetails.name)),
          Some(messages("messages__visuallyhidden__dynamic_phone_number", partnershipDetails.name))
        )().row(PartnershipPhoneNumberId(index))(
          routes.PartnershipPhoneNumberController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false,
                   title:Message, h1:Message): String =
    view(
      CYAViewModel(
        answerSections = answerSections,
        href = postUrl,
        schemeName = None,
        returnOverview = false,
        hideEditLinks = false,
        srn = srn,
        hideSaveAndContinueButton = hideButton,
        title = title,
        h1 = h1
      )
    )(fakeRequest, messages).toString

  "CheckYourAnswersPartnershipContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          running(_.overrides(modules(fullAnswers.dataRetrievalAction): _*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
              val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
                title = Message("checkYourAnswers.hs.heading"),
                h1 = Message("checkYourAnswers.hs.heading"))
          }
        }

        "Update Mode" in {
          running(_.overrides(
            bind[AuthAction].toInstance(FakeAuthAction),
            bind(classOf[AllowAccessActionProvider]).qualifiedWith(classOf[NoSuspendedCheck]).toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].toInstance(fullAnswers.dataRetrievalAction))) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, submitUrl(UpdateMode, srn), hideButton = true,
                title = Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve),
                h1 = Message("messages__contactDetailsFor", partnershipDetails.name))
          }
        }
      }
    }
  }
}


