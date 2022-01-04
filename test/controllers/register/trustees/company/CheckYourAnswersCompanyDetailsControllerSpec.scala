/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.{DataRetrievalAction, FakeAuthAction, _}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.company._
import models.Mode.checkMode
import models.{Index, NormalMode, _}
import org.scalatest.OptionValues
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersCompanyDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersCompanyDetailsControllerSpec._

  "Check Your Answers Company Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(companyDetailsAllValues(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyDetailsAllReasons(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe
          viewAsString(companyDetailsAllValues(UpdateMode, srn), UpdateMode, srn, postUrlUpdateMode,
            title = Message("messages__detailsFor", Message("messages__theCompany")),
            h1 = Message("messages__detailsFor", companyName))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe
          viewAsString(companyDetailsAddLinksValues, UpdateMode, srn, postUrlUpdateMode,
            title = Message("messages__detailsFor", Message("messages__theCompany")),
            h1 = Message("messages__detailsFor", companyName))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe
          viewAsString(companyDetailsAddLinksValues, UpdateMode, srn, postUrlUpdateMode,
            title = Message("messages__detailsFor", Message("messages__theCompany")),
            h1 = Message("messages__detailsFor", companyName))
      }
    }

    "when rendering view" must {

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, index, None)(FakeDataRequest(fullAnswers))
      )
    }
  }

}

object CheckYourAnswersCompanyDetailsControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour with OptionValues {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index: Index = Index(0)
  val testSchemeName = "Test Scheme Name"
  val srn: Option[String] = Some("S123")
  val companyName = "test company name"

  private val crn = "crn"
  private val utr = "utr"
  private val vat = "vat"
  private val paye = "paye"
  private val reason = "reason"

  private val emptyAnswers = UserAnswers().set(CompanyDetailsId(0))(CompanyDetails(companyName)).asOpt.value

  private def hasCompanyNumberRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyCRNController.onPageLoad(checkMode(mode), 0, srn).url

  private def companyRegistrationNumberVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyEnterCRNController.onPageLoad(checkMode(mode), srn, index).url

  private def noCompanyNumberReasonRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyNoCRNReasonController.onPageLoad(checkMode(mode), index, srn).url

  private def hasCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyUTRController.onPageLoad(checkMode(mode), index, srn).url

  private def companyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyEnterUTRController.onPageLoad(checkMode(mode), srn, index).url

  private def noCompanyUTRRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyNoUTRReasonController.onPageLoad(checkMode(mode), 0, srn).url

  private def hasCompanyVatRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyVATController.onPageLoad(checkMode(mode), 0, srn).url

  private def companyEnterVATRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyEnterVATController.onPageLoad(checkMode(mode), 0, srn).url

  private def hasCompanyPayeRoute(mode: Mode, srn: Option[String]) =
    routes.HasCompanyPAYEController.onPageLoad(checkMode(mode), 0, srn).url

  private def companyPayeVariationsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyEnterPAYEController.onPageLoad(checkMode(mode), 0, srn).url

  private val fullAnswers = emptyAnswers
    .set(HasCompanyCRNId(0))(true).flatMap(
    _.set(CompanyEnterCRNId(0))(ReferenceValue(crn, isEditable = true)).flatMap(
      _.set(HasCompanyUTRId(0))(true).flatMap(
        _.set(CompanyEnterUTRId(0))(ReferenceValue(utr, isEditable = true)).flatMap(
          _.set(HasCompanyVATId(0))(true).flatMap(
            _.set(CompanyEnterVATId(0))(ReferenceValue(vat, isEditable = true)).flatMap(
              _.set(HasCompanyPAYEId(0))(true).flatMap(
                _.set(CompanyEnterPAYEId(0))(ReferenceValue(paye, isEditable = true))
              ))))))).asOpt.value

  private val fullAnswersNo = emptyAnswers
    .set(HasCompanyCRNId(0))(false).flatMap(
    _.set(CompanyNoCRNReasonId(0))(reason).flatMap(
      _.set(HasCompanyUTRId(0))(false).flatMap(
        _.set(CompanyNoUTRReasonId(0))(reason).flatMap(
          _.set(HasCompanyVATId(0))(false).flatMap(
            _.set(HasCompanyPAYEId(0))(false)
          ))))).asOpt.value

  def postUrl: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)

  def postUrlUpdateMode: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn)


  private def companyDetailsAddLinksValues: Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        addLink(messages("messages__checkYourAnswers__establishers__company__number"), companyRegistrationNumberVariationsRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_crn", companyName)),
        addLink(messages("messages__utr__checkyouranswerslabel"), companyUTRRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", companyName)),
        addLink(messages("messages__common__cya__vat"), companyEnterVATRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_vat_number", companyName)),
        addLink(messages("messages__common__cya__paye"), companyPayeVariationsRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_paye", companyName))
      )
    ))

  private def companyDetailsAllValues(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      hasCompanyNumberYesRow(mode, srn) ++
        companyNumberRow(mode, srn) ++
        hasCompanyUTRYesRow(mode, srn) ++
        utrRow(mode, srn) ++
        hasCompanyVatYesRow(mode, srn) ++
        vatRow(mode, srn) ++
        hasCompanyPayeYesRow(mode, srn) ++
        payeRow(mode, srn)
    ))

  private def hasCompanyNumberYesRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    if (mode == NormalMode)
      Seq(booleanChangeLink(messages("messages__hasCRN", companyName), hasCompanyNumberRoute(mode, srn), value = true,
        messages("messages__visuallyhidden__dynamic_hasCrn", companyName))) else Nil

  private def companyNumberRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    Seq(stringChangeLink(messages("messages__checkYourAnswers__establishers__company__number"), companyRegistrationNumberVariationsRoute(mode, srn), crn,
      messages("messages__visuallyhidden__dynamic_crn", companyName)))

  private def utrRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    Seq(stringChangeLink(messages("messages__utr__checkyouranswerslabel"), companyUTRRoute(mode, srn), utr,
      messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", companyName)))

  private def vatRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    Seq(stringChangeLink(messages("messages__common__cya__vat"), companyEnterVATRoute(mode, srn), vat,
      messages("messages__visuallyhidden__dynamic_vat_number", companyName)))

  private def payeRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    Seq(stringChangeLink(messages("messages__common__cya__paye"), companyPayeVariationsRoute(mode, srn), paye,
      messages("messages__visuallyhidden__dynamic_paye", companyName)))

  private def hasCompanyUTRYesRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    if (mode == NormalMode)
      Seq(booleanChangeLink(messages("messages__hasUTR", companyName), hasCompanyUTRRoute(mode, srn), value = true,
        messages("messages__visuallyhidden__dynamic_hasUtr", companyName))) else Nil

  private def hasCompanyVatYesRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    if (mode == NormalMode)
      Seq(booleanChangeLink(messages("messages__hasVAT", companyName), hasCompanyVatRoute(mode, srn), value = true,
        messages("messages__visuallyhidden__dynamic_hasVat", companyName))) else Nil

  private def hasCompanyPayeYesRow(mode: Mode, srn: Option[String]): Seq[AnswerRow] =
    if (mode == NormalMode)
      Seq(booleanChangeLink(messages("messages__hasPAYE", companyName), hasCompanyPayeRoute(mode, srn), value = true,
        messages("messages__visuallyhidden__dynamic_hasPaye", companyName))) else Nil

  private def companyDetailsAllReasons(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink(messages("messages__hasCRN", companyName), hasCompanyNumberRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasCrn", companyName)),
        stringChangeLink(messages("messages__whyNoCRN", companyName), noCompanyNumberReasonRoute(mode, srn), reason,
          messages("messages__visuallyhidden__dynamic_noCrnReason", companyName)),
        booleanChangeLink(messages("messages__hasUTR", companyName), hasCompanyUTRRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasUtr", companyName)),
        stringChangeLink(messages("messages__whyNoUTR", companyName), noCompanyUTRRoute(mode, srn), reason,
          messages("messages__visuallyhidden__dynamic_noUtrReason", companyName)),
        booleanChangeLink(messages("messages__hasVAT", companyName), hasCompanyVatRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasVat", companyName)),
        booleanChangeLink(messages("messages__hasPAYE", companyName), hasCompanyPayeRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasPaye", companyName))
      )
    ))

  private def booleanChangeLink(label: String, changeUrl: String, value: Boolean, hiddenLabel: String) =
    AnswerRow(label, Seq(if (value) "site.yes" else "site.no"),
      answerIsMessageKey = true,
      Some(Link("site.change", changeUrl, Some(hiddenLabel))))

  private def stringChangeLink(label: String, changeUrl: String, ansOrReason: String, hiddenLabel: String) =
    AnswerRow(
      label,
      Seq(ansOrReason),
      answerIsMessageKey = false,
      Some(Link("site.change", changeUrl,
        Some(hiddenLabel)
      )))


  private def addLink(label: String, changeUrl: String, hiddenLabel: String) =
    AnswerRow(label, Seq("site.not_entered"), answerIsMessageKey = true, Some(Link("site.add", changeUrl, Some(hiddenLabel))))

  private val view = injector.instanceOf[checkYourAnswers]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersCompanyDetailsController =
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
      controllerComponents,
      view
    )

  def viewAsString(answerSections: Seq[AnswerSection], mode: Mode = NormalMode,
                   srn: Option[String] = None, postUrl: Call = postUrl, title:Message, h1:Message): String =
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

}
