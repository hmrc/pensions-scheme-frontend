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
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import models._
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.checkyouranswers.CheckYourAnswers._
import utils.checkyouranswers.Ops._
import utils.checkyouranswers._
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersCompanyDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersCompanyDetailsControllerSpec._

  "Check Your Answers Copany Details Controller " when {
    "on Page load if toggle off/toggle on in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with empty answers" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }
    }

    "on Page load if toggle on in UpdateMode" must {
      "return OK and the correct view for vat, paye and crn if not new establisher" in {
        val answers = UserAnswers().set(CompanyVatVariationsId(index))("098765432").flatMap(
          _.set(CompanyPayeVariationsId(index))("12345678")).asOpt.value
        implicit val request = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = estCompanyDetailsSection(
          CompanyVatVariationsId(index).row(companyVatVariationsRoute, UpdateMode) ++
            CompanyPayeVariationsId(index).row(companyPayeRoute, UpdateMode) ++
            CompanyRegistrationNumberVariationsId(index).row(companyRegistrationNumberVariationsRoute, UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection), srn, postUrlUpdateMode)
      }

      "return OK and the correct view for vat and paye if new establisher" in {
        val answers = UserAnswers().set(CompanyVatId(index))(Vat.Yes("098765432")).flatMap(
          _.set(CompanyPayeId(index))(Paye.Yes("12345678"))).flatMap(_.set(IsEstablisherNewId(index))(true)).asOpt.value
        implicit val request = FakeDataRequest(answers)

        val expectedCompanyDetailsSection = estCompanyDetailsSection(
          Seq.empty[AnswerRow]
        )
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection), srn, postUrlUpdateMode)
      }
    }

    "on Submit" must {
      "redirect to next page " in {
        val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "mark establisher company as complete" in {
        val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        FakeUserAnswersService.verify(IsCompanyCompleteId(index), true)
      }

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, None, index)(FakeDataRequest(fullAnswers))
      )
    }
  }

}

object CheckYourAnswersCompanyDetailsControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index = Index(0)
  val testSchemeName = "Test Scheme Name"
  val srn = Some("S123")
  private val companyDetails = CompanyDetails("test company")

  private def estCompanyDetailsSection(rows: Seq[AnswerRow]) =
    AnswerSection(None, rows)

  private val emptyAnswers = UserAnswers()
  private val hasCompanyNumberRoute = routes.HasCompanyNumberController.onPageLoad(CheckMode, None, 0).url
  private val companyRegistrationNumberVariationsRoute = routes.CompanyRegistrationNumberVariationsController.onPageLoad(CheckUpdateMode, srn, index).url
  private val noCompanyNumberReasonRoute = routes.NoCompanyNumberController.onPageLoad(CheckUpdateMode, srn, index).url
  private val hasCompanyUTRRoute = routes.HasCompanyUTRController.onPageLoad(CheckUpdateMode, srn, index).url
  private val hasCompanyUTR1Route = routes.HasCompanyUTRController.onPageLoad(CheckUpdateMode, srn, index).url
  private val noCompanyUTRRoute = routes.NoCompanyUTRController.onPageLoad(CheckMode, None, 0).url
  private def hasCompanyVatRoute = routes.HasCompanyVATController.onPageLoad(CheckMode, srn, 0).url
  private val companyVatVariationsRoute = routes.CompanyVatVariationsController.onPageLoad(CheckUpdateMode, 0, srn).url
  private val hasCompanyPayeRoute = routes.HasCompanyPAYEController.onPageLoad(CheckMode, None, 0).url
  private val companyPayeRoute = routes.HasCompanyPAYEController.onPageLoad(CheckMode, None, 0).url
  private val isCompanyDormantRoute = routes.IsCompanyDormantController.onPageLoad(CheckMode, None, 0).url

  private val fullAnswers = emptyAnswers

  def postUrl: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(NormalMode, None, index)

  def postUrlUpdateMode: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(UpdateMode, srn, index)

  private def companyDetailsSection(implicit request: DataRequest[AnyContent]): AnswerSection = {
    val crnRows = DoYouHaveBoolCYA[HasCompanyNumberId](
      label = "messages__company__cya__crn_yes_no"
    )().row(HasCompanyNumberId(index))(hasCompanyNumberRoute, request.userAnswers)

    val payeRows = DoYouHaveBoolCYA[HasCompanyPAYEId](
     "messages__company__cya__paye_yes_no"
    )().row(HasCompanyPAYEId(index))(hasCompanyPayeRoute, request.userAnswers)

    val vatRows = DoYouHaveBoolCYA("messages__company__cya__vat_yes_no")().
      row(HasCompanyVATId(index))(hasCompanyVatRoute, request.userAnswers)

    val utrRows = DoYouHaveBoolCYA(
      label = "messages__company__cya__utr_yes_no"
    )().row(HasCompanyUTRId(index))(hasCompanyUTRRoute, request.userAnswers)

    val isDormantRows = IsDormantCYA()().row(IsCompanyDormantId(index))(isCompanyDormantRoute, request.userAnswers)

    AnswerSection(
      None,
      crnRows ++ utrRows ++ vatRows ++ payeRows ++ isDormantRows)
  }

  private def answerSections(implicit request: DataRequest[AnyContent]) = Seq(companyDetailsSection)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach,
                 isToggleOn: Boolean = false): CheckYourAnswersCompanyDetailsController =
    new CheckYourAnswersCompanyDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      new FakeNavigator(onwardRoute),
      FakeUserAnswersService,
      allowChangeHelper,
      new FakeFeatureSwitchManagementService(isToggleOn)
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = postUrl): String =
    check_your_answers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false
    )(fakeRequest, messages).toString

}



