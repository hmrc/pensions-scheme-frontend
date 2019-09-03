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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.routes.SchemeTaskListController
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyEmailId, CompanyPhoneId}
import models.Mode.checkMode
import models.requests.DataRequest
import models._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeDataRequest, UserAnswers}
import viewmodels.AnswerSection
import views.html.checkYourAnswers

class CheckYourAnswersCompanyContactDetailsControllerSpec extends ControllerSpecBase with MockitoSugar
  with BeforeAndAfterEach with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some("test-srn")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    SchemeTaskListController.onPageLoad(mode, srn)

  private def answerSection(mode: Mode, srn: Option[String] = None)(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {
    val userAnswers = request.userAnswers
    Seq(AnswerSection(None,
      StringCYA[CompanyEmailId](userAnswers.get(CompanyDetailsId(index)).map(companyDetails =>
        messages("messages__common_email__heading", companyDetails.companyName)),
        Some(messages("messages__common_company_email__visually_hidden_change_label")))().row(CompanyEmailId(index))(
        routes.CompanyEmailController.onPageLoad(checkMode(mode), srn, Index(index)).url, userAnswers) ++

        StringCYA[CompanyPhoneId](userAnswers.get(CompanyDetailsId(index)).map(companyDetails =>
          messages("messages__common_phone__heading", companyDetails.companyName)),
          Some(messages("messages__common_company_phone__visually_hidden_change_label")))().row(CompanyPhoneId(index))(
          routes.CompanyPhoneController.onPageLoad(checkMode(mode), srn, Index(index)).url, userAnswers)
    ))
  }

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
      FakeUserAnswersService
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl()): String =
    checkYourAnswers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false
    )(fakeRequest, messages).toString

  private val fullAnswers = UserAnswers().set(CompanyEmailId(0))("test@test.com").flatMap(_.set(CompanyPhoneId(0))("12345"))
    .flatMap(_.set(CompanyDetailsId(0))(CompanyDetails("test company"))).asOpt.value

  "CheckYourAnswersCompanyContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode))
        }
        "Update Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), postUrl = submitUrl(UpdateMode, srn), srn = srn)
        }
      }
    }
  }
}

