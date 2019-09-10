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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.partnership.{PartnershipEmailId, PartnershipPhoneId}
import models.Mode.checkMode
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, FakeCountryOptions, UserAnswers}
import viewmodels.AnswerSection
import views.html.checkYourAnswers

class CheckYourAnswersPartnershipContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some("test-srn")
  private val partnershipDetails = PartnershipDetails("Test partnership")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val fullAnswers = UserAnswers().trusteePartnershipDetails(index, partnershipDetails).
    trusteePartnershipEmail(index, email = "test@test.com").trusteePartnershipPhone(index, phone = "1234")

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private def answerSection(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = {
    Seq(AnswerSection(None,
      StringCYA[PartnershipEmailId](
        Some(messages("messages__common_email__heading", partnershipDetails.name)),
        Some(messages("messages__visuallyhidden__dynamic_email", partnershipDetails.name))
      )().row(PartnershipEmailId(index))(
        routes.PartnershipEmailController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers) ++

        StringCYA[PartnershipPhoneId](
          Some(messages("messages__common_phone__heading", partnershipDetails.name)),
          Some(messages("messages__visuallyhidden__dynamic_phone", partnershipDetails.name))
        )().row(PartnershipPhoneId(index))(
          routes.PartnershipPhoneNumberController.onPageLoad(checkMode(mode), Index(index), srn).url, fullAnswers)
    ))
  }

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false): String =
    checkYourAnswers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = hideButton
    )(fakeRequest, messages).toString

  "CheckYourAnswersPartnershipContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          running(_.overrides(modules(fullAnswers.dataRetrievalAction, featureSwitchEnabled = true): _*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
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
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, submitUrl(UpdateMode, srn), hideButton = true)
          }
        }
      }
    }
  }
}


