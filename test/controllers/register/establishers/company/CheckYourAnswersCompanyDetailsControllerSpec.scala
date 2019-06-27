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
import identifiers.register.establishers.company._
import models._
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
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
        contentAsString(result) mustBe viewAsString(companyDetailsAllValues(NormalMode, None)(request))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyDetailsAllReasons(NormalMode, None)(request))
      }
    }

    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyDetailsAllValues(UpdateMode, srn)(request))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyDetailsAllReasons(UpdateMode, srn)(request))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyDetailsAddLinksValues(request))
      }
    }

    "on Submit" must {
      "redirect to next page " in {
        val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "mark establisher company details as complete" in {
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
  val companyName = "test company name"

  private val crn = "crn"
  private val utr = "utr"
  private val vat = "vat"
  private val paye = "paye"
  private val reason = "reason"

  private val emptyAnswers = UserAnswers()
  private def hasCompanyNumberRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyNumberController.onPageLoad(mode, srn, 0).url
  private def companyRegistrationNumberVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index).url
  private def noCompanyNumberReasonRoute(mode: Mode, srn: Option[String]) =
    routes.NoCompanyNumberController.onPageLoad(mode, srn, index).url
  private def hasCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyUTRController.onPageLoad(mode, srn, index).url
  private def companyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyUTRController.onPageLoad(mode, srn, index).url
  private def noCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.NoCompanyUTRController.onPageLoad(mode, srn, 0).url
  private def hasCompanyVatRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyVATController.onPageLoad(mode, srn, 0).url
  private def companyVatVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyVatVariationsController.onPageLoad(mode, 0, srn).url
  private def hasCompanyPayeRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyPAYEController.onPageLoad(mode, srn, 0).url
  private def companyPayeVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyPayeVariationsController.onPageLoad(mode, 0, srn).url
  private def isCompanyDormantRoute(mode: Mode, srn: Option[String]) =
  routes.IsCompanyDormantController.onPageLoad(mode, srn, 0).url

  private val fullAnswers = emptyAnswers

  def postUrl: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(NormalMode, None, index)

  def postUrlUpdateMode: Call = routes.CheckYourAnswersCompanyDetailsController.onSubmit(UpdateMode, srn, index)


  private def companyDetailsAddLinksValues(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        addLink(messages("messages__companyNumber__establisher__heading", companyName), companyRegistrationNumberVariationsRoute(UpdateMode, srn)),
        addLink(messages("messages__vatVariations__heading", companyName), companyVatVariationsRoute(UpdateMode, srn)),
        addLink(messages("messages__payeVariations__heading", companyName), companyPayeVariationsRoute(UpdateMode, srn))
      )
    ))

  private def companyDetailsAllValues(mode: Mode, srn: Option[String]
                                          )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink(messages("messages__hasCompanyNumber__h1", companyName), hasCompanyNumberRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__companyNumber__establisher__heading", companyName), companyRegistrationNumberVariationsRoute(mode, srn), crn),
        booleanChangeLink(messages("messages__hasCompanyUtr__h1", companyName), hasCompanyUTRRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__companyUtr__heading", companyName), companyUTRRoute(mode, srn), utr),
        booleanChangeLink(messages("messages__hasCompanyVat__h1", companyName), hasCompanyVatRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__vatVariations__heading", companyName), companyVatVariationsRoute(mode, srn), vat),
        booleanChangeLink(messages("", companyName), hasCompanyPayeRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__payeVariations__heading", companyName), companyPayeVariationsRoute(mode, srn), paye),
        booleanChangeLink(messages("messages__company__cya__dormant", companyName), isCompanyDormantRoute(mode, srn), value = true)
      )
    ))

  private def companyDetailsAllReasons(mode: Mode, srn: Option[String]
                                           )(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink(messages("messages__hasCompanyNumber__h1", companyName), hasCompanyNumberRoute(mode, srn), value = false),
        stringChangeLink(messages("", companyName), noCompanyNumberReasonRoute(mode, srn), reason),
        booleanChangeLink(messages("messages__hasCompanyUtr__h1", companyName), hasCompanyUTRRoute(mode, srn), value = false),
        stringChangeLink(messages("messages__noCompanyUtr__heading", companyName), noCompanyUTRRoute(mode, srn), reason),
        booleanChangeLink(messages("messages__hasCompanyVat__h1", companyName), hasCompanyVatRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__vatVariations__heading", companyName), companyVatVariationsRoute(mode, srn), reason),
        booleanChangeLink(messages("", companyName), hasCompanyPayeRoute(mode, srn), value = true),
        stringChangeLink(messages("messages__payeVariations__heading", companyName), companyPayeVariationsRoute(mode, srn), reason),
        booleanChangeLink(messages("messages__company__cya__dormant", companyName), isCompanyDormantRoute(mode, srn), value = true)
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



