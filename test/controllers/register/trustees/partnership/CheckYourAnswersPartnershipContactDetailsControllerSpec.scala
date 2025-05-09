/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions.*
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController
import identifiers.register.trustees.partnership.{PartnershipEmailId, PartnershipPhoneId}
import models.Mode.checkMode
import models.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, FakeCountryOptions, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersPartnershipContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some(SchemeReferenceNumber(SchemeReferenceNumber("test-srn")))
  private val partnershipDetails = PartnershipDetails("Test partnership")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val fullAnswers = UserAnswers().trusteePartnershipDetails(Index(0), partnershipDetails).
    trusteePartnershipEmail(Index(0), email = "test@test.com").trusteePartnershipPhone(Index(0), phone = "1234")

  private def submitUrl(index: Int): Call =
    PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index)

  private def updateModeUrl(mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn))

  private def answerSection(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Seq[AnswerSection] = {
    Seq(AnswerSection(None,
      StringCYA[PartnershipEmailId](
        Some(messages("messages__enterEmail", partnershipDetails.name)),
        Some(messages("messages__visuallyhidden__dynamic_email_address", partnershipDetails.name))
      )().row(PartnershipEmailId(index))(
        routes.PartnershipEmailController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, fullAnswers) ++

        StringCYA[PartnershipPhoneId](
          Some(messages("messages__enterPhoneNumber", partnershipDetails.name)),
          Some(messages("messages__visuallyhidden__dynamic_phone_number", partnershipDetails.name))
        )().row(PartnershipPhoneId(index))(
          routes.PartnershipPhoneNumberController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, fullAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = submitUrl(index),
                   hideButton: Boolean = false, title: Message, h1: Message): String =
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
          running(_.overrides(modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule]()*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
              val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
                title = Message("checkYourAnswers.hs.heading"),
                h1 = Message("checkYourAnswers.hs.heading"))
          }
        }

        "Update Mode" in {
          val ftBinding: Seq[GuiceableModule] = Seq(
            bind[AuthAction].toInstance(FakeAuthAction),
            bind(classOf[AllowAccessActionProvider]).qualifiedWith(classOf[NoSuspendedCheck]).toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].toInstance(fullAnswers.dataRetrievalAction))
          running(_.overrides(ftBinding*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersPartnershipContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn), updateModeUrl(UpdateMode, OptionalSchemeReferenceNumber(srn)), hideButton = true,
                title = Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve),
                h1 = Message("messages__contactDetailsFor", partnershipDetails.name))
          }
        }
      }
    }
  }
}


