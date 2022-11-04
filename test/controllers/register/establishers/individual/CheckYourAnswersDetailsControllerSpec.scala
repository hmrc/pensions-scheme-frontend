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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.individual._
import models.FeatureToggleName.SchemeRegistration
import models.Mode._
import models._
import models.person.PersonName
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, FeatureToggleService}
import utils._
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach with MockitoSugar {

  import CheckYourAnswersDetailsControllerSpec._

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "Check Your Answers Individual Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allValuesYes(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allValuesNo(NormalMode, None),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allChangeLinksVariations, UpdateMode, srn, postUrlUpdateMode,
          title = Message("messages__detailsFor", Message("messages__thePerson")),
          h1 = Message("messages__detailsFor", establisherName.fullName))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allAddLinksVariations, UpdateMode, srn, postUrlUpdateMode,
          title = Message("messages__detailsFor", Message("messages__thePerson").resolve),
          h1 = Message("messages__detailsFor", establisherName.fullName))
      }
    }
  }

}

object CheckYourAnswersDetailsControllerSpec extends ControllerSpecBase with Enumerable.Implicits
  with ControllerAllowChangeBehaviour with OptionValues {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)

  private val index = Index(0)
  private val srn = Some("S123")
  private val name = "test name"
  private val establisherName = PersonName("test", "name")
  private val establisherDob: LocalDate = LocalDate.now()
  private val nino = "nino"
  private val utr = "utr"
  private val reason = "reason"

  private val emptyAnswers = UserAnswers()

  private def dob(mode: Mode, srn: Option[String]) =
    routes.EstablisherDOBController.onPageLoad(checkMode(mode), index, srn).url

  private def hasNino(mode: Mode, srn: Option[String]) =
    routes.EstablisherHasNINOController.onPageLoad(checkMode(mode), index, srn).url

  private def nino(mode: Mode, srn: Option[String]) =
    routes.EstablisherEnterNINOController.onPageLoad(checkMode(mode), index, srn).url

  private def noNinoReason(mode: Mode, srn: Option[String]) =
    routes.EstablisherNoNINOReasonController.onPageLoad(checkMode(mode), index, srn).url

  private def hasUtr(mode: Mode, srn: Option[String]) =
    routes.EstablisherHasUTRController.onPageLoad(checkMode(mode), 0, srn).url

  private def utr(mode: Mode, srn: Option[String]) =
    routes.EstablisherEnterUTRController.onPageLoad(checkMode(mode), 0, srn).url

  private def noUtrReason(mode: Mode, srn: Option[String]) =
    routes.EstablisherNoUTRReasonController.onPageLoad(checkMode(mode), 0, srn).url

  private val fullAnswers = emptyAnswers
    .set(EstablisherNameId(0))(establisherName).flatMap(
    _.set(EstablisherDOBId(0))(establisherDob).flatMap(
      _.set(EstablisherHasNINOId(0))(true).flatMap(
        _.set(EstablisherEnterNINOId(0))(ReferenceValue(nino)).flatMap(
          _.set(EstablisherHasUTRId(0))(true).flatMap(
            _.set(EstablisherUTRId(0))(ReferenceValue(utr))
          ))))).asOpt.value

  private val fullAnswersNo = emptyAnswers
    .set(EstablisherNameId(0))(establisherName).flatMap(
    _.set(EstablisherDOBId(0))(establisherDob).flatMap(
      _.set(EstablisherHasNINOId(0))(false).flatMap(
        _.set(EstablisherNoNINOReasonId(0))(reason).flatMap(
          _.set(EstablisherHasUTRId(0))(false).flatMap(
            _.set(EstablisherNoUTRReasonId(0))(reason)
          ))))).asOpt.value

  def postUrl: Call = controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)

  def postUrlUpdateMode: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn)


  private def allAddLinksVariations: Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        stringLink(messages("messages__DOB__heading", name), DateHelper.formatDate(establisherDob)),
        addLink(messages("messages__enterNINO", name), nino(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_national_insurance_number", name)),
        addLink(messages("messages__enterUTR", name), utr(UpdateMode, srn),
          messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name))
      )
    ))

  private def allChangeLinksVariations: Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        stringLink(messages("messages__DOB__heading", name), DateHelper.formatDate(establisherDob)),
        stringLink(messages("messages__enterNINO", name), nino),
        stringLink(messages("messages__enterUTR", name), utr)
      )
    ))

  private def allValuesYes(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        stringChangeLink(messages("messages__DOB__heading", name), dob(mode, srn), DateHelper.formatDate(establisherDob),
          messages("messages__visuallyhidden__dynamic_date_of_birth", name)),
        booleanChangeLink(messages("messages__hasNINO", name), hasNino(mode, srn), value = true,
          messages("messages__visuallyhidden__dynamic_hasNino", name)),
        stringChangeLink(messages("messages__enterNINO", name), nino(mode, srn), nino,
          messages("messages__visuallyhidden__dynamic_national_insurance_number", name)),
        booleanChangeLink(messages("messages__hasUTR", name), hasUtr(mode, srn), value = true,
          messages("messages__visuallyhidden__dynamic_hasUtr", name)),
        stringChangeLink(messages("messages__enterUTR", name), utr(mode, srn), utr,
          messages("messages__visuallyhidden__dynamic_unique_taxpayer_reference", name))
      )
    ))


  private def allValuesNo(mode: Mode, srn: Option[String]): Seq[AnswerSection] =
    Seq(AnswerSection(
      None,
      Seq(
        stringChangeLink(
          label = messages("messages__DOB__heading", name),
          changeUrl = dob(mode, srn),
          ansOrReason = DateHelper.formatDate(establisherDob),
          hiddenLabel = messages("messages__visuallyhidden__dynamic_date_of_birth", name)
        ),
        booleanChangeLink(
          label = messages("messages__hasNINO", name),
          changeUrl = hasNino(mode, srn),
          value = false,
          hiddenLabel = messages("messages__visuallyhidden__dynamic_hasNino", name)
        ),
        stringChangeLink(
          label = messages("messages__whyNoNINO", name),
          changeUrl = noNinoReason(mode, srn), ansOrReason = reason,
          hiddenLabel = messages("messages__visuallyhidden__dynamic_noNinoReason", name)
        ),
        booleanChangeLink(
          label = messages("messages__hasUTR", name),
          changeUrl = hasUtr(mode, srn),
          value = false,
          hiddenLabel = messages("messages__visuallyhidden__dynamic_hasUtr", name)
        ),
        stringChangeLink(
          label = messages("messages__whyNoUTR", name),
          changeUrl = noUtrReason(mode, srn),
          ansOrReason = reason,
          hiddenLabel = messages("messages__visuallyhidden__dynamic_noUtrReason", name)
        )
      )
    ))

  private def booleanChangeLink(label: String, changeUrl: String, value: Boolean, hiddenLabel: String) =
    AnswerRow(
      label,
      Seq(if (value) "site.yes" else "site.no"),
      answerIsMessageKey = true,
      Some(Link("site.change", changeUrl, Some(hiddenLabel)))
    )

  private def stringChangeLink(label: String, changeUrl: String, ansOrReason: String, hiddenLabel: String) =
    AnswerRow(
      label,
      Seq(ansOrReason),
      answerIsMessageKey = false,
      Some(Link("site.change", changeUrl, Some(hiddenLabel)))
    )

  private def stringLink(label: String, ansOrReason: String) =
    AnswerRow(
      label,
      Seq(ansOrReason),
      answerIsMessageKey = false,
      None
    )


  private def addLink(label: String, changeUrl: String, hiddenLabel: String) =
    AnswerRow(
      label,
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", changeUrl, Some(hiddenLabel)))
    )

  private val view = injector.instanceOf[checkYourAnswers]
  private val mockFeatureToggleService = mock[FeatureToggleService]
  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersDetailsController =
    new CheckYourAnswersDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      allowChangeHelper,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  def viewAsString(answerSections: Seq[AnswerSection],
                   mode: Mode = NormalMode,
                   srn: Option[String] = None,
                   postUrl: Call = postUrl,
                   title:Message, h1:Message): String =
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
