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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.individual.{TrusteeEmailId, TrusteeNameId, TrusteePhoneId}
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.person.PersonName
import navigators.Navigator
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils.annotations.NoSuspendedCheck
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersIndividualContactDetailsControllerToggleOffSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach {
  private val index = Index(0)
  private val srn = Some(SchemeReferenceNumber(SchemeReferenceNumber("test-srn")))
  private val trusteeName = "test name"
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  private val mockFeatureToggleService = mock[FeatureToggleService]

  private def submitUrl(mode: Mode = NormalMode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn))

  private val fullAnswers = UserAnswers().set(TrusteeEmailId(0))(value = "test@test.com").flatMap(_.set(TrusteePhoneId(0))(value = "12345"))
    .flatMap(_.set(TrusteeNameId(0))(PersonName("test", "name"))).asOpt.value

  private def answerSection(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Seq[AnswerSection] = {
    Seq(AnswerSection(None, StringCYA[TrusteeEmailId](
      Some(messages("messages__enterEmail", trusteeName)),
      Some(messages("messages__visuallyhidden__dynamic_email_address", trusteeName))
    )().row(TrusteeEmailId(index))(routes.TrusteeEmailController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, fullAnswers) ++
      StringCYA[TrusteePhoneId](
        Some(messages("messages__enterPhoneNumber", trusteeName)),
        Some(messages("messages__visuallyhidden__dynamic_phone_number", trusteeName))
      )().row(TrusteePhoneId(index))(
        routes.TrusteePhoneController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, fullAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = submitUrl(), hideButton: Boolean = false,
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

  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, false)))
  }

  "CheckYourAnswersIndividualContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction).overrides(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService)
          ).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
          val result = controller.onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
            title = Message("checkYourAnswers.hs.heading"),
            h1 = Message("checkYourAnswers.hs.heading"))
          app.stop()
        }

        "Update Mode" in {
          val ftBinding: Seq[GuiceableModule] = Seq(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService),
            bind[Navigator].toInstance(FakeNavigator),
            bind[AuthAction].toInstance(FakeAuthAction),
            bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].to(fullAnswers.dataRetrievalAction),
            bind[AllowChangeHelper].toInstance(allowChangeHelper(saveAndContinueButton = true)),
            bind[AllowAccessActionProvider].qualifiedWith(classOf[NoSuspendedCheck]).to(FakeAllowAccessProvider())
          )
          running(_.overrides(ftBinding: _*)) {
            app =>

              val controller = app.injector.instanceOf[CheckYourAnswersIndividualContactDetailsController]
              val result = controller.onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn))(fakeRequest)

              status(result) mustBe OK
              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn), postUrl = submitUrl(UpdateMode, OptionalSchemeReferenceNumber(srn)), hideButton = true,
                title = Message("messages__contactDetailsFor", Message("messages__thePerson")),
                h1 = Message("messages__contactDetailsFor", trusteeName))
              app.stop()
          }
        }
      }
    }
  }
}

