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

  "Check Your Answers Company Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with add links for reasons" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

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
    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with add links for reasons" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

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
  private val crn = "crn"
  private val utr = "utr"
  private val vat = "vat"
  private val paye = "paye"
  private val reason = "reason"

  private def estCompanyDetailsSection(rows: Seq[AnswerRow]) =
    AnswerSection(None, rows)

  private val emptyAnswers = UserAnswers()
  private def hasCompanyNumberRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyNumberController.onPageLoad(mode, srn, 0).url
  private def companyRegistrationNumberVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index).url
  private def noCompanyNumberReasonRoute(mode: Mode, srn: Option[String]) =
    routes.NoCompanyNumberController.onPageLoad(mode, srn, index).url
  private def hasCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyUTRController.onPageLoad(mode, srn, index).url
  private def hasCompanyUTR1Route(mode: Mode, srn: Option[String]) =
    routes.HasCompanyUTRController.onPageLoad(mode, srn, index).url
  private def noCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.NoCompanyUTRController.onPageLoad(mode, srn, 0).url
  private def hasCompanyVatRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyVATController.onPageLoad(mode, srn, 0).url
  private def companyVatVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyVatVariationsController.onPageLoad(mode, 0, srn).url
  private def hasCompanyPayeRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyPAYEController.onPageLoad(mode, srn, 0).url
  private def companyPayeRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyPayeVariationsController.onPageLoad(mode, srn, 0).url
  private def isCompanyDormantRoute(mode: Mode, srn: Option[String]) =
  routes.IsCompanyDormantController.onPageLoad(mode, srn, 0).url

  private val fullAnswers = emptyAnswers

  def postUrl: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(NormalMode, None, index)

  def postUrlUpdateMode: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(UpdateMode, srn, index)

  private def emptyCompanyDetailsSection(mode: Mode, srn: Option[String]
                                                    )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        addLink("", hasCompanyNumberRoute(mode, srn)),
        addLink("", hasCompanyUTRRoute(mode, srn)),
        addLink("", hasCompanyVatRoute(mode, srn)),
        addLink("", hasCompanyPayeRoute(mode, srn)),
        addLink("", isCompanyDormantRoute(mode, srn))
      )
    ))

  private def companyDetailsAddLinksValues(mode: Mode, srn: Option[String]
                                                    )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink("", hasCompanyNumberRoute(mode, srn), true),
        addLink("", companyRegistrationNumberVariationsRoute(mode, srn)),
        booleanChangeLink("", hasCompanyUTRRoute(mode, srn), true),
        addLink("", hasCompanyUTR1Route(mode, srn)),
        booleanChangeLink("", hasCompanyVatRoute(mode, srn), true),
        addLink("", companyVatVariationsRoute(mode, srn)),
        booleanChangeLink("", hasCompanyPayeRoute(mode, srn), true),
        addLink("", companyPayeRoute(mode, srn)),
        booleanChangeLink("", isCompanyDormantRoute(mode, srn), true)
      )
    ))

  private def companyDetailsAddLinksReasons(mode: Mode, srn: Option[String]
                                          )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink("", hasCompanyNumberRoute(mode, srn), false),
        addLink("", noCompanyNumberReasonRoute(mode, srn)),
        booleanChangeLink("", hasCompanyUTRRoute(mode, srn), false),
        addLink("", noCompanyUTRRoute(mode, srn)),
        booleanChangeLink("", hasCompanyVatRoute(mode, srn), true),
        addLink("", companyVatVariationsRoute(mode, srn)),
        booleanChangeLink("", hasCompanyPayeRoute(mode, srn), true),
        addLink("", companyPayeRoute(mode, srn)),
        booleanChangeLink("", isCompanyDormantRoute(mode, srn), true)
      )
    ))

  private def companyDetailsAllValues(mode: Mode, srn: Option[String]
                                          )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink("", hasCompanyNumberRoute(mode, srn), true),
        stringChangeLink("", companyRegistrationNumberVariationsRoute(mode, srn), crn),
        booleanChangeLink("", hasCompanyUTRRoute(mode, srn), true),
        stringChangeLink("", hasCompanyUTR1Route(mode, srn), utr),
        booleanChangeLink("", hasCompanyVatRoute(mode, srn), true),
        stringChangeLink("", companyVatVariationsRoute(mode, srn), vat),
        booleanChangeLink("", hasCompanyPayeRoute(mode, srn), true),
        stringChangeLink("", companyPayeRoute(mode, srn), paye),
        booleanChangeLink("", isCompanyDormantRoute(mode, srn), true)
      )
    ))

  private def companyDetailsAllReasons(mode: Mode, srn: Option[String]
                                           )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink("", hasCompanyNumberRoute(mode, srn), false),
        stringChangeLink("", noCompanyNumberReasonRoute(mode, srn), reason),
        booleanChangeLink("", hasCompanyUTRRoute(mode, srn), false),
        stringChangeLink("", noCompanyUTRRoute(mode, srn), reason),
        booleanChangeLink("", hasCompanyVatRoute(mode, srn), true),
        stringChangeLink("", companyVatVariationsRoute(mode, srn), reason),
        booleanChangeLink("", hasCompanyPayeRoute(mode, srn), true),
        stringChangeLink("", companyPayeRoute(mode, srn), reason),
        booleanChangeLink("", isCompanyDormantRoute(mode, srn), true)
      )
    ))

  private def booleanChangeLink(label: String, changeUrl: String, value: Boolean) =
    AnswerRow(label, Seq(if (value) "site.yes" else "site.no"),
    answerIsMessageKey = false,
    Some(Link("site.change", changeUrl, Some(label))))

  private def stringChangeLink(label: String, changeUrl: String, ansOrReason: String) =
    AnswerRow(
      label,
      Seq(ansOrReason),
      answerIsMessageKey = false,
      Some(Link("site.change", changeUrl,
        Some(label)
    )))


  private def addLink(label: String, changeUrl: String) =
    AnswerRow(label, Seq.empty, answerIsMessageKey = true, Some(Link("site.add", changeUrl, Some(label))))


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



