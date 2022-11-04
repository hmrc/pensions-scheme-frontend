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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils._
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers
import org.mockito.Mockito._

import scala.concurrent.Future

class CheckYourAnswersPartnershipDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach{

  import CheckYourAnswersPartnershipDetailsControllerSpec._

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "Check Your Answers Partnership Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswersYes())
        val result = controller(fullAnswersYes().dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipDetailsAllValues(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipDetailsAllReasons(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswersYes(false))
        val result = controller(fullAnswersYes(false).dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe
          viewAsString(partnershipDetailsAllExistingAnswers, UpdateMode, srn,
            title = Message("messages__detailsFor", Message("messages__thePartnership").resolve),
            h1 = Message("messages__detailsFor", partnershipName))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe
          viewAsString(partnershipDetailsAddLinksValues, UpdateMode, srn,
            title = Message("messages__detailsFor", Message("messages__thePartnership").resolve),
            h1 = Message("messages__detailsFor", partnershipName))
      }
    }

    "when in variations journey with new establisher" must {

      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val answers = fullAnswersYes().set(IsEstablisherNewId(0))(true).asOpt.value
        val request = FakeDataRequest(answers)
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipDetailsAllValues(UpdateMode, srn), mode = UpdateMode, srn = srn,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val answers = fullAnswersNo.set(IsEstablisherNewId(0))(true).asOpt.value
        val request = FakeDataRequest(answers)
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipDetailsAllReasons(UpdateMode, srn), mode = UpdateMode, srn = srn,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }
  }

}

object CheckYourAnswersPartnershipDetailsControllerSpec extends ControllerSpecBase with Enumerable.Implicits
  with ControllerAllowChangeBehaviour with OptionValues {

  def onwardRoute(mode: Mode = NormalMode, srn: Option[String] = None): Call = {
    if (mode == NormalMode) {
      controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)
    } else {
      controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)
    }
  }

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index: Index = Index(0)
  val testSchemeName = "Test Scheme Name"
  val srn: Option[String] = Some("S123")
  val partnershipName = "test partnership name"

  private val utr = "utr"
  private val vat = "vat"
  private val paye = "paye"
  private val reason = "reason"

  private val emptyAnswers = UserAnswers().set(PartnershipDetailsId(0))(PartnershipDetails(partnershipName)).asOpt.value

  private def hasPartnershipUTRRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipHasUTRController.onPageLoad(checkMode(mode), index, srn).url

  private def partnershipUTRRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipEnterUTRController.onPageLoad(checkMode(mode), index, srn).url

  private def noPartnershipUTRRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipNoUTRReasonController.onPageLoad(checkMode(mode), 0, srn).url

  private def hasPartnershipVatRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipHasVATController.onPageLoad(checkMode(mode), 0, srn).url

  private def partnershipEnterVATRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipEnterVATController.onPageLoad(checkMode(mode), 0, srn).url

  private def hasPartnershipPayeRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipHasPAYEController.onPageLoad(checkMode(mode), 0, srn).url

  private def partnershipPayeVariationsRoute(mode: Mode, srn: Option[String]): String =
    routes.PartnershipEnterPAYEController.onPageLoad(checkMode(mode), 0, srn).url

  private def fullAnswersYes(isEditable: Boolean = true) = emptyAnswers
    .set(PartnershipHasUTRId(0))(value = true).flatMap(
    _.set(PartnershipEnterUTRId(0))(ReferenceValue(utr, isEditable)).flatMap(
      _.set(PartnershipHasVATId(0))(value = true).flatMap(
        _.set(PartnershipEnterVATId(0))(ReferenceValue(vat, isEditable)).flatMap(
          _.set(PartnershipHasPAYEId(0))(value = true).flatMap(
            _.set(PartnershipEnterPAYEId(0))(ReferenceValue(paye, isEditable))
          ))))).asOpt.value

  private val fullAnswersNo = emptyAnswers
    .set(PartnershipHasUTRId(0))(value = false).flatMap(
    _.set(PartnershipNoUTRReasonId(0))(reason).flatMap(
      _.set(PartnershipHasVATId(0))(value = false).flatMap(
        _.set(PartnershipHasPAYEId(0))(value = false)
      ))).asOpt.value


  private def partnershipDetailsAddLinksValues: Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        addLink(messages("messages__enterUTR", partnershipName), partnershipUTRRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", partnershipName)),
        addLink(messages("messages__enterVAT", partnershipName), partnershipEnterVATRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_vat_number", partnershipName)),
        addLink(messages("messages__enterPAYE", partnershipName), partnershipPayeVariationsRoute(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_paye_reference", partnershipName))
      )
    ))

  private def partnershipDetailsAllValues(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink(messages("messages__hasUTR", partnershipName), hasPartnershipUTRRoute(mode, srn), value = true,
          messages("messages__visuallyhidden__dynamic_hasUtr", partnershipName)),
        stringChangeLink(messages("messages__enterUTR", partnershipName), partnershipUTRRoute(mode, srn), utr,
          messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", partnershipName)),
        booleanChangeLink(messages("messages__hasVAT", partnershipName), hasPartnershipVatRoute(mode, srn), value = true,
          messages("messages__visuallyhidden__dynamic_hasVat", partnershipName)),
        stringChangeLink(messages("messages__enterVAT", partnershipName), partnershipEnterVATRoute(mode, srn), vat,
          messages("messages__visuallyhidden__dynamic_vat_number", partnershipName)),
        booleanChangeLink(messages("messages__hasPAYE", partnershipName), hasPartnershipPayeRoute(mode, srn), value = true,
          messages("messages__visuallyhidden__dynamic_hasPaye", partnershipName)),
        stringChangeLink(messages("messages__enterPAYE", partnershipName), partnershipPayeVariationsRoute(mode, srn), paye,
          messages("messages__visuallyhidden__dynamic_paye_reference", partnershipName))
      )
    ))

  private def partnershipDetailsAllExistingAnswers: Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        stringNoLink(messages("messages__enterUTR", partnershipName), utr),
        stringNoLink(messages("messages__enterVAT", partnershipName), vat),
        stringNoLink(messages("messages__enterPAYE", partnershipName), paye)
      )
    ))


  private def partnershipDetailsAllReasons(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        booleanChangeLink(messages("messages__hasUTR", partnershipName), hasPartnershipUTRRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasUtr", partnershipName)),
        stringChangeLink(messages("messages__whyNoUTR", partnershipName), noPartnershipUTRRoute(mode, srn), reason,
          messages("messages__visuallyhidden__dynamic_noUtrReason", partnershipName)),
        booleanChangeLink(messages("messages__hasVAT", partnershipName), hasPartnershipVatRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasVat", partnershipName)),
        booleanChangeLink(messages("messages__hasPAYE", partnershipName), hasPartnershipPayeRoute(mode, srn), value = false,
          messages("messages__visuallyhidden__dynamic_hasPaye", partnershipName))
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

  private def stringNoLink(label: String, ansOrReason: String) =
    AnswerRow(
      label,
      Seq(ansOrReason),
      answerIsMessageKey = false,
      None)


  private def addLink(label: String, changeUrl: String, hiddenLabel: String) =
    AnswerRow(label, Seq("site.not_entered"), answerIsMessageKey = true, Some(Link("site.add", changeUrl, Some(hiddenLabel))))

  private val view = injector.instanceOf[checkYourAnswers]
  private val mockFeatureToggleService = mock[FeatureToggleService]
  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach,
                 isToggleOn: Boolean = false): CheckYourAnswersPartnershipDetailsController =
    new CheckYourAnswersPartnershipDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  def viewAsString(answerSections: Seq[AnswerSection], mode: Mode = NormalMode,
                   srn: Option[String] = None, title:Message, h1:Message): String =
    view(
      CYAViewModel(
        answerSections = answerSections,
        href =  onwardRoute(mode, srn),
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
