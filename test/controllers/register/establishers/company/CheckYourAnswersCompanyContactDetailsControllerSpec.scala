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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions.*
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.routes.PsaSchemeTaskListController
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEmailId, CompanyPhoneId}
import models.*
import models.Mode.checkMode
import models.requests.DataRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers.*
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeDataRequest, UserAnswers, UserAnswerOps}
import viewmodels.{AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersCompanyContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val name = "test company"

  private val index = Index(0)
  private val srn = Some(SchemeReferenceNumber(SchemeReferenceNumber("test-srn")))
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private def submitUrl: Call =
    controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)

  private def submitUrlUpdateMode(mode: Mode, srn: OptionalSchemeReferenceNumber): Call =
    PsaSchemeTaskListController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn))

  private def answerSection(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber)(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {
    val userAnswers = request.userAnswers
    Seq(AnswerSection(None,
      StringCYA[CompanyEmailId](userAnswers.get(CompanyDetailsId(index)).map(companyDetails =>
        messages("messages__enterEmail", companyDetails.companyName)),
        Some(messages("messages__visuallyhidden__dynamic_email_address", name)))().row(CompanyEmailId(index))(
        routes.CompanyEmailController.onPageLoad(checkMode(mode), OptionalSchemeReferenceNumber(srn), Index(index)).url, userAnswers) ++

        StringCYA[CompanyPhoneId](userAnswers.get(CompanyDetailsId(index)).map(companyDetails =>
          messages("messages__enterPhoneNumber", companyDetails.companyName)),
          Some(messages("messages__visuallyhidden__dynamic_phone_number", name)))().row(CompanyPhoneId(index))(
          routes.CompanyPhoneController.onPageLoad(checkMode(mode), OptionalSchemeReferenceNumber(srn), Index(index)).url, userAnswers)
    ))
  }

  private val view = injector.instanceOf[checkYourAnswers]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersCompanyContactDetailsController =
    new CheckYourAnswersCompanyContactDetailsController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper,
      controllerComponents,
      view
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = submitUrl,
                   title: Message, h1: Message): String =
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

  "CheckYourAnswersCompanyContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode),
            title = Message("checkYourAnswers.hs.heading"),
            h1 = Message("checkYourAnswers.hs.heading"))
        }
        "Update Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn), index)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, OptionalSchemeReferenceNumber(srn)), postUrl = submitUrlUpdateMode(UpdateMode, OptionalSchemeReferenceNumber(srn)),
            srn = OptionalSchemeReferenceNumber(srn),
            title = Message("messages__contactDetailsFor", Message("messages__theCompany")),
            h1 = Message("messages__contactDetailsFor", name))
        }
      }
    }
  }
}

