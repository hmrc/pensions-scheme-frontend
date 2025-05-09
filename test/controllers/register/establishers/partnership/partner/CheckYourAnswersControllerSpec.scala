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

package controllers.register.establishers.partnership.partner

import base.SpecBase
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.partnership.partner.*
import models.*
import models.Mode.checkMode
import models.address.Address
import models.person.PersonName
import play.api.test.Helpers.{contentAsString, status, *}
import utils.{AllowChangeHelper, FakeCountryOptions, FakeDataRequest, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import java.time.LocalDate

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec.*

  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()
  implicit val request: FakeDataRequest = FakeDataRequest(partnerAnswers)

  private val view = injector.instanceOf[checkYourAnswers]

  private def controller(dataRetrievalAction: DataRetrievalAction,
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
    new CheckYourAnswersController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      countryOptions,
      allowChangeHelper,
      controllerComponents,
      view
    )

  private def viewAsString(mode: Mode, answerSection: Seq[AnswerSection],
                           srn: OptionalSchemeReferenceNumber, title:Message, h1:Message) = view(
    CYAViewModel(
      answerSections = answerSection,
      href = controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, firstIndex, OptionalSchemeReferenceNumber(srn)),
      schemeName = None,
      returnOverview = false,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false,
      title = title,
      h1 = h1
    )
  )(fakeRequest, messages).toString

  "Check Your Answers Individual Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request: FakeDataRequest = FakeDataRequest(partnerAnswersYes)
        val result = controller(partnerAnswersYes.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode, answerRowsYes(NormalMode, EmptyOptionalSchemeReferenceNumber), EmptyOptionalSchemeReferenceNumber,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(partnerAnswersNo)
        val result = controller(partnerAnswersNo.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode, answerRowsNo(NormalMode, EmptyOptionalSchemeReferenceNumber), EmptyOptionalSchemeReferenceNumber,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val answers = partnerAnswersYes
          .set(IsNewPartnerId(firstIndex, firstIndex))(true).asOpt.value
        val request = FakeDataRequest(answers)
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn))(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, answerRowsYes(UpdateMode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn),
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(partnerAnswersNo)
        val result = controller(partnerAnswersNo.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn))(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, answerRowsAddLinks(UpdateMode, OptionalSchemeReferenceNumber(srn)), OptionalSchemeReferenceNumber(srn),
          title = Message("messages__detailsFor", Message("messages__thePartner")),
          h1 = Message("messages__detailsFor", personName.fullName))
      }
    }

    behave like changeableController(
      controller(partnerAnswersNo.dataRetrievalAction, _: AllowChangeHelper)
        .onPageLoad(NormalMode, firstIndex, firstIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
    )
  }

}

object CheckYourAnswersControllerSpec extends SpecBase {
  private val firstIndex = Index(0)
  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()
  private val srn = Some(SchemeReferenceNumber("srn"))
  private val personName = PersonName("first name", "last name")
  private val address = Address("Address 1", "Address 2", None, None, None, "GB")

  private val partnerAnswers = UserAnswers()
    .set(PartnerNameId(firstIndex, firstIndex))(personName)
    .flatMap(_.set(PartnerDOBId(firstIndex, firstIndex))(LocalDate.of(1962, 6, 9)))
    .flatMap(_.set(PartnerAddressId(firstIndex, firstIndex))(address))
    .flatMap(_.set(PartnerAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnerPreviousAddressId(firstIndex, firstIndex))(address))
    .flatMap(_.set(PartnerEmailId(firstIndex, firstIndex))("test@test.com"))
    .flatMap(_.set(PartnerPhoneId(firstIndex, firstIndex))("123456789"))
    .asOpt.value

  private val partnerAnswersYes = partnerAnswers
    .set(PartnerHasUTRId(firstIndex, firstIndex))(true)
    .flatMap(_.set(PartnerEnterUTRId(firstIndex, firstIndex))(ReferenceValue("utr")))
    .flatMap(_.set(PartnerHasNINOId(firstIndex, firstIndex))(true))
    .flatMap(_.set(PartnerEnterNINOId(firstIndex, firstIndex))(ReferenceValue("nino")))
    .asOpt.value

  private val partnerAnswersNo = partnerAnswers
    .set(PartnerHasUTRId(firstIndex, firstIndex))(false)
    .flatMap(_.set(PartnerNoUTRReasonId(firstIndex, firstIndex))("reason"))
    .flatMap(_.set(PartnerHasNINOId(firstIndex, firstIndex))(false))
    .flatMap(_.set(PartnerNoNINOReasonId(firstIndex, firstIndex))("reason"))
    .asOpt.value

  private def answerRowWithAddNino: AnswerRow = AnswerRow(Message("messages__enterNINO", personName.fullName), Seq("site.not_entered"), answerIsMessageKey = true,
    Some(Link("site.add",
      routes.PartnerEnterNINOController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn")))).url,
      Some(Message("messages__visuallyhidden__dynamic_national_insurance_number", personName.fullName)))))

  private def answerRowWithAddUtr: AnswerRow = AnswerRow(Message("messages__enterUTR", personName.fullName), Seq("site.not_entered"), answerIsMessageKey = true,
    Some(Link("site.add",
      routes.PartnerEnterUTRController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("srn")))).url,
      Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", personName.fullName)))))

  private def answerRowsYes(mode: Mode, srn: OptionalSchemeReferenceNumber): Seq[AnswerSection] = Seq(AnswerSection(
    None,
    Seq(
    nameAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    dobAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    hasNinoAnswerRow(mode, OptionalSchemeReferenceNumber(srn), "site.yes"),
    ninoAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    hasUtrAnswerRow(mode, OptionalSchemeReferenceNumber(srn), "site.yes"),
    utrAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    addressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    addressYearsAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    previousAddressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    emailAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
    phoneAnswerRow(mode, OptionalSchemeReferenceNumber(srn))
    )
  ))

  private def answerRowsNo(mode: Mode, srn: OptionalSchemeReferenceNumber): Seq[AnswerSection] = Seq(AnswerSection(
    None,
    Seq(
      nameAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      dobAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      hasNinoAnswerRow(mode, OptionalSchemeReferenceNumber(srn), "site.no"),
      noNinoReasonAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      hasUtrAnswerRow(mode, OptionalSchemeReferenceNumber(srn), "site.no"),
      noUtrReasonAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      addressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      addressYearsAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      previousAddressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      emailAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      phoneAnswerRow(mode, OptionalSchemeReferenceNumber(srn))
    )
  ))

  private def answerRowsAddLinks(mode: Mode, srn: OptionalSchemeReferenceNumber): Seq[AnswerSection] = Seq(AnswerSection(
    None,
    Seq(
      nameRow(mode, OptionalSchemeReferenceNumber(srn)),
      dobRow(mode, OptionalSchemeReferenceNumber(srn)),
      answerRowWithAddNino,
      answerRowWithAddUtr,
      addressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      previousAddressAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      emailAnswerRow(mode, OptionalSchemeReferenceNumber(srn)),
      phoneAnswerRow(mode, OptionalSchemeReferenceNumber(srn))
    )
  ))

  def addressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__addressFor", personName.fullName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", routes.PartnerAddressController.onPageLoad(checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_address", personName.fullName)))
    ))

  def addressYearsAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__hasBeen1Year", personName.fullName),
    Seq(s"messages__common__${AddressYears.UnderAYear.toString}"),
    answerIsMessageKey = true,
    Some(Link("site.change", routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_addressYears", personName.fullName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__previousAddress__cya", personName.fullName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", routes.PartnerPreviousAddressController.onPageLoad(checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_previousAddress", personName.fullName))))
  )

  def hasNinoAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber, value: String): AnswerRow = AnswerRow(Message("messages__hasNINO", personName.fullName), Seq(value), answerIsMessageKey = true, Some(
    Link("site.change", routes.PartnerHasNINOController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_hasNino", personName.fullName))
    )))

  def hasUtrAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber, value: String): AnswerRow =
    AnswerRow(Message("messages__hasUTR", personName.fullName), Seq(value), answerIsMessageKey = true, Some(
    Link("site.change", routes.PartnerHasUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_hasUtr", personName.fullName))
    )))

  def ninoAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__enterNINO", personName.fullName), Seq("nino"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerEnterNINOController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_national_insurance_number", personName.fullName))
    )))

  def nameAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = nameRow(mode, OptionalSchemeReferenceNumber(srn), Some(
    Link("site.change", routes.PartnerNameController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_name", personName.fullName))
    )))

  def nameRow(mode: Mode, srn: OptionalSchemeReferenceNumber, changeLink: Option[Link] = None): AnswerRow =
    AnswerRow("messages__partnerName__cya", Seq(personName.fullName), answerIsMessageKey = false, changeLink)

  def noUtrReasonAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__whyNoUTR", personName.fullName), Seq("reason"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNoUTRReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_noUtrReason", personName.fullName))
    )))

  def noNinoReasonAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__whyNoNINO", personName.fullName), Seq("reason"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNoNINOReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_noNinoReason", personName.fullName))
    )))

  def dobAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    dobRow(mode, OptionalSchemeReferenceNumber(srn), Some(Link("site.change", routes.PartnerDOBController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_date_of_birth", personName.fullName))
    )))

  def dobRow(mode: Mode, srn: OptionalSchemeReferenceNumber, changeLink: Option[Link] = None): AnswerRow =
    AnswerRow(Message("messages__DOB__heading", personName.fullName), Seq("9 June 1962"), answerIsMessageKey = false, changeLink)

  def emailAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__enterEmail", personName.fullName), Seq("test@test.com"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerEmailController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_email_address", personName.fullName))
    )))

  def phoneAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__enterPhoneNumber", personName.fullName), Seq("123456789"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerPhoneController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_phone_number", personName.fullName))
    )))

  private def utrAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
  AnswerRow(Message("messages__enterUTR", personName.fullName), Seq("utr"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerEnterUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, OptionalSchemeReferenceNumber(srn)).url,
      Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", personName.fullName))
    )))
}
