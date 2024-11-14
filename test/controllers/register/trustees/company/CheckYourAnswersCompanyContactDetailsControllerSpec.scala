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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyEmailId, CompanyPhoneId}
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.{FakeUserAnswersService, FeatureToggleService}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeDataRequest, UserAnswers}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersCompanyContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some(SchemeReferenceNumber(SchemeReferenceNumber("test-srn")))
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val mockFeatureToggleService = mock[FeatureToggleService]

  private def submitUrlUpdateMode(mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn))

  private def submitUrl(index: Int): Call =
    PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index)

  private def answerSection(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber)(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {
    val userAnswers = request.userAnswers
    val cn = userAnswers.get(CompanyDetailsId(index)).map(_.companyName).value

    Seq(AnswerSection(None,
      StringCYA[CompanyEmailId](
        Some(messages("messages__enterEmail", cn)),
        Some(messages("messages__visuallyhidden__dynamic_email_address", cn))
      )().row(CompanyEmailId(index))(
        routes.CompanyEmailController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, userAnswers) ++

        StringCYA[CompanyPhoneId](
          Some(messages("messages__enterPhoneNumber", cn)),
          Some(messages("messages__visuallyhidden__dynamic_phone_number", cn))
        )().row(CompanyPhoneId(index))(
          routes.CompanyPhoneController.onPageLoad(checkMode(mode), Index(index), OptionalSchemeReferenceNumber(srn)).url, userAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersCompanyContactDetailsController =
    new CheckYourAnswersCompanyContactDetailsController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper,
      FakeUserAnswersService,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = submitUrl(index), title: Message, h1: Message): String =
    view(
      CYAViewModel(
        answerSections = answerSections,
        href = postUrl,
        schemeName = None,
        returnOverview = false,
        hideEditLinks = false,
        srn = srn,
        hideSaveAndContinueButton = false,
        title = title,
        h1 = h1
      )
    )(fakeRequest, messages).toString

  private val fullAnswers = UserAnswers().set(CompanyEmailId(0))("test@test.com").flatMap(_.set(CompanyPhoneId(0))("12345"))
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("test company"))).asOpt.value


  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "CheckYourAnswersCompanyContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
            title = Message("checkYourAnswers.hs.heading"),
            h1 = Message("checkYourAnswers.hs.heading"))
        }
        "Update Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, Index(0), OptionalSchemeReferenceNumber(srn))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, OptionalSchemeReferenceNumber(srn)), postUrl = submitUrlUpdateMode(UpdateMode, OptionalSchemeReferenceNumber(srn)), srn = OptionalSchemeReferenceNumber(srn),
            title = Message("messages__contactDetailsFor", Message("messages__theCompany").resolve),
            h1 = Message("messages__contactDetailsFor", "test company"))
        }
      }
    }
  }
}

