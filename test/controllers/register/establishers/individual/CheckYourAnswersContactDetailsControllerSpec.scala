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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.establishers.individual.routes.{EstablisherEmailController, EstablisherPhoneController}
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.person.PersonName
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils.UserAnswers
import utils.annotations.NoSuspendedCheck
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersContactDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach {

  private val index = Index(0)
  private val srn = Some(SchemeReferenceNumber(SchemeReferenceNumber("test-srn")))
  private val establisherName = PersonName("test", "name")
  private val email = "test@test.com"
  private val phone = "1234"

  private val fullAnswers = UserAnswers().establishersIndividualName(Index(0), establisherName).
    establishersIndividualEmail(Index(0), email = email).establishersIndividualPhone(Index(0), phone = "1234")

  private def submitUrl(mode: Mode = NormalMode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Call =
    controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)

  private def answerSection(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Seq[AnswerSection] = {
    val emailAnswerRow = AnswerRow(
      messages("messages__enterEmail", establisherName.fullName),
      Seq(email),
      answerIsMessageKey = false,
      Some(Link("site.change", EstablisherEmailController.onPageLoad(checkMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)).url,
        Some(messages("messages__visuallyhidden__dynamic_email_address", establisherName.fullName))))
    )

    val phoneAnswerRow = AnswerRow(
      messages("messages__enterPhoneNumber", establisherName.fullName),
      Seq(phone),
      answerIsMessageKey = false,
      Some(Link("site.change", EstablisherPhoneController.onPageLoad(checkMode(mode), Index(0), OptionalSchemeReferenceNumber(srn)).url,
        Some(messages("messages__visuallyhidden__dynamic_phone_number", establisherName.fullName))))
    )

    Seq(AnswerSection(None, Seq(emailAnswerRow, phoneAnswerRow)))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = submitUrl(), hideButton: Boolean = false,
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

  private val mockFeatureToggleService = mock[FeatureToggleService]

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "CheckYourAnswersContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {

          val bindings = modules(fullAnswers.dataRetrievalAction)
          val ftBinding: Seq[GuiceableModule] = Seq(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService)
          )

          running(_.overrides((bindings ++ ftBinding): _*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersContactDetailsController]
              val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
                title = Message("checkYourAnswers.hs.heading"),
                h1 = Message("checkYourAnswers.hs.heading"))
          }
        }

        "Update Mode" in {

          val ftBinding: Seq[GuiceableModule] = Seq(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService),
            bind[AuthAction].toInstance(FakeAuthAction),
            bind(classOf[AllowAccessActionProvider]).qualifiedWith(classOf[NoSuspendedCheck]).toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].toInstance(fullAnswers.dataRetrievalAction)
          )
          running(_.overrides(ftBinding: _*)) {
            app =>
              val controller = app.injector.instanceOf[CheckYourAnswersContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(fakeRequest)
              status(result) mustBe OK

              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn), submitUrl(UpdateMode, OptionalSchemeReferenceNumber(srn)), hideButton = true,
                title = Message("messages__contactDetailsFor", Message("messages__thePerson")),
                h1 = Message("messages__contactDetailsFor", establisherName.fullName))
          }
        }
      }
    }
  }
}


