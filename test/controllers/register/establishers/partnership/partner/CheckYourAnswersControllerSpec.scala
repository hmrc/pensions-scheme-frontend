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

package controllers.register.establishers.partnership.partner

import base.SpecBase
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.partner._
import models.Mode.checkMode
import models.address.Address
import models.person.{PersonDetails, PersonName}
import models.{Index, _}
import org.joda.time.LocalDate
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import utils.checkyouranswers.Ops._
import utils.{FakeCountryOptions, FakeDataRequest, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers_old

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(partnerAnswersToggleOff)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach,
                         isHnSEnabled: Boolean = false): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute),
      countryOptions,
      allowChangeHelper,
      new FakeFeatureSwitchManagementService(isHnSEnabled)
    )


  private def partnerDetails(mode: Mode, srn: Option[String] = None) = AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      PartnerDetailsId(firstIndex, firstIndex).
        row(routes.PartnerDetailsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      PartnerNinoId(firstIndex, firstIndex).
        row(routes.PartnerNinoController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      PartnerUniqueTaxReferenceId(firstIndex, firstIndex).
        row(routes.PartnerUniqueTaxReferenceController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url)
    ).flatten
  )

  private def partnerContactDetails(mode: Mode, srn: Option[String] = None) = AnswerSection(
    Some("messages__partner__cya__contact__details_heading"),
    Seq(
      PartnerAddressId(firstIndex, firstIndex).
        row(routes.PartnerAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      PartnerAddressYearsId(firstIndex, firstIndex).
        row(routes.PartnerAddressYearsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      PartnerPreviousAddressId(firstIndex, firstIndex).
        row(routes.PartnerPreviousAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      PartnerContactDetailsId(firstIndex, firstIndex).
        row(routes.PartnerContactDetailsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url)
    ).flatten
  )

  private def viewAsString(mode: Mode = NormalMode,
                           answerSection: Seq[AnswerSection] = Seq(partnerDetails(NormalMode), partnerContactDetails(NormalMode)),
                           srn: Option[String] = None) = check_your_answers_old(
    frontendAppConfig,
    answerSection,
    routes.CheckYourAnswersController.onSubmit(mode, firstIndex, firstIndex, srn),
    None,
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    srn = srn
  )(fakeRequest, messages).toString

  /*"CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {
        val result = controller(partnerAnswersToggleOff.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and display all given answers for UpdateMode with add/change link when user enters nino" in {

        val result = controller(partnerAnswersUpdateWithNewNino(true).dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNino(answerRowWithChangeToggleOff), Some("srn"))
      }

      "return OK and display Nino with Add link for UpdateMode when no nino returned from ETMP" in {

        val result = controller(partnerDetailsAnswersUpdateWithoutNino.dataRetrievalAction).
          onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNino(answerRowWithAdd), Some("srn"))
      }

      "return OK and display Nino with no link for UpdateMode when nino returned from ETMP" in {

        val result = controller(partnerAnswersUpdateWithNewNino(false).dataRetrievalAction).
          onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNino(answerRowWithNoLink), Some("srn"))
      }

      "return OK and display all the answers with yes when HnS toggle is on" in {

        val result = controller(partnerAnswersYes.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, answerRowsYes, Some("srn"))
      }

    "return OK and display all the answers with no when HnS toggle is on" in {

      val result = controller(partnerAnswersNo.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(NormalMode, answerRowsNo)
    }

    behave like changeableController(
      controller(partnerAnswersNo.dataRetrievalAction, _: AllowChangeHelper)
        .onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)
    )
  }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(NormalMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)
      }
    }
  }*/

  "Check Your Answers Individual Details Controller " when {
    "when in registration journey" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request: FakeDataRequest = FakeDataRequest(partnerAnswersYes)
        val result = controller(partnerAnswersYes.dataRetrievalAction, isHnSEnabled = true).onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode, answerRowWithChange(NormalMode, None), None)
      }

      "return OK and the correct view with full answers when user has answered no to all questions" in {
        val request = FakeDataRequest(partnerAnswersNo)
        val result = controller(partnerAnswersNo.dataRetrievalAction, isHnSEnabled = true).onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode, answerRowWithChangeForNo(NormalMode, None), None)
      }
    }
/*
    "when in variations journey with existing establisher" must {
      "return OK and the correct view with full answers when user has answered yes to all questions" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allChangeLinksVariations(request), UpdateMode, srn, postUrlUpdateMode)
      }

      "return OK and the correct view with add links for values" in {
        val request = FakeDataRequest(fullAnswersNo)
        val result = controller(fullAnswersNo.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(allAddLinksVariations(request), UpdateMode, srn, postUrlUpdateMode)
      }
    }*/
  }

}

object CheckYourAnswersControllerSpec extends SpecBase {
  private val firstIndex = Index(0)
  implicit val countryOptions = new FakeCountryOptions()
  private val schemeName = "test scheme name"
  private val personName = PersonName("first name", "last name")
  private val address = Address("Address 1", "Address 2", None, None, None, "GB")
  private val desiredRoute = controllers.routes.IndexController.onPageLoad()

  private val partnerDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(PartnerDetailsId(firstIndex, firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .asOpt.value

  private val partnerAnswersUpdate = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value

  private def partnerAnswersUpdateWithNewNino(isEditable: Boolean): UserAnswers = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A", isEditable)).asOpt.value

  private val partnerAnswersToggleOff = partnerAnswersUpdate
    .set(PartnerUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(PartnerAddressId(firstIndex, firstIndex))(address))
    .flatMap(_.set(PartnerAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnerPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  private val partnerAnswers = UserAnswers()
    .set(PartnerNameId(firstIndex, firstIndex))(personName)
    .flatMap(_.set(PartnerDOBId(firstIndex, firstIndex))(new LocalDate(1962, 6, 9)))
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
    .flatMap(_.set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("nino")))
    .asOpt.value

  private val partnerAnswersNo = partnerAnswers
    .set(PartnerHasUTRId(firstIndex, firstIndex))(false)
    .flatMap(_.set(PartnerNoUTRReasonId(firstIndex, firstIndex))("reason"))
    .flatMap(_.set(PartnerHasNINOId(firstIndex, firstIndex))(false))
    .flatMap(_.set(PartnerNoNINOReasonId(firstIndex, firstIndex))("reason"))
    .asOpt.value

  private def answerRowWithAdd: AnswerRow = AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
    Some(Link("site.add",
      routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
      Some(s"messages__visuallyhidden__partner__nino"))))

  private def answerRowWithChange(mode: Mode, srn: Option[String]): Seq[AnswerSection] = Seq(AnswerSection(
    None,
    Seq(
    nameAnswerRow(mode, srn),
    dobAnswerRow(mode, srn),
    hasNinoAnswerRow(mode, srn, "site.yes"),
    ninoAnswerRow(mode, srn),
    hasUtrAnswerRow(mode, srn, "site.yes"),
    utrAnswerRow(mode, srn),
    addressAnswerRow(mode, srn),
    addressYearsAnswerRow(mode, srn),
    previousAddressAnswerRow(mode, srn),
    emailAnswerRow(mode, srn),
    phoneAnswerRow(mode, srn)
    )
  ))

  private def answerRowWithChangeForNo(mode: Mode, srn: Option[String]): Seq[AnswerSection] = Seq(AnswerSection(
    None,
    Seq(
      nameAnswerRow(mode, srn),
      dobAnswerRow(mode, srn),
      hasNinoAnswerRow(mode, srn, "site.no"),
      noNinoReasonAnswerRow(mode, srn),
      hasUtrAnswerRow(mode, srn, "site.no"),
      noUtrReasonAnswerRow(mode, srn),
      addressAnswerRow(mode, srn),
      addressYearsAnswerRow(mode, srn),
      previousAddressAnswerRow(mode, srn),
      emailAnswerRow(mode, srn),
      phoneAnswerRow(mode, srn)
    )
  ))

  private def answerRowWithChangeToggleOff: AnswerRow = AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
      Some("messages__visuallyhidden__partner__nino")
    )))

  def addressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__addressFor", personName.fullName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", routes.PartnerAddressController.onPageLoad(checkMode(mode), firstIndex, firstIndex, None).url,
      Some(Message("messages__visuallyhidden__dynamic_address", personName.fullName)))
    ))

  def addressYearsAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__hasBeen1Year", personName.fullName),
    Seq(s"messages__common__${AddressYears.UnderAYear.toString}"),
    answerIsMessageKey = true,
    Some(Link("site.change", routes.PartnerAddressYearsController.onPageLoad(checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_addressYears", personName.fullName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__previousAddress__cya", personName.fullName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", routes.PartnerPreviousAddressController.onPageLoad(checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_previousAddress", personName.fullName))))
  )

  def hasNinoAnswerRow(mode: Mode, srn: Option[String], value: String) = AnswerRow(Message("messages__hasNINO", personName.fullName), Seq(value), answerIsMessageKey = true, Some(
    Link("site.change", routes.PartnerHasNINOController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_hasNino", personName.fullName))
    )))

  def hasUtrAnswerRow(mode: Mode, srn: Option[String], value: String) = AnswerRow(Message("messages__hasUTR", personName.fullName), Seq(value), answerIsMessageKey = true, Some(
    Link("site.change", routes.PartnerHasUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_hasUtr", personName.fullName))
    )))

  def ninoAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__enterNINO", personName.fullName), Seq("nino"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_national_insurance_number", personName.fullName))
    )))

  def nameAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow("messages__partnerName__cya", Seq(personName.fullName), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNameController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_name", personName.fullName))
    )))

  def noUtrReasonAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__whyNoUTR", personName.fullName), Seq("reason"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNoUTRReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_noUtrReason", personName.fullName))
    )))

  def noNinoReasonAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__whyNoNINO", personName.fullName), Seq("reason"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNoNINOReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_noNinoReason", personName.fullName))
    )))

  def dobAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__DOB__heading", personName.fullName), Seq("9 June 1962"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerDOBController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_date_of_birth", personName.fullName))
    )))

  def emailAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__enterEmail", personName.fullName), Seq("test@test.com"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerEmailController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_email_address", personName.fullName))
    )))

  def phoneAnswerRow(mode: Mode, srn: Option[String]) = AnswerRow(Message("messages__enterPhoneNumber", personName.fullName), Seq("123456789"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerPhoneController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_phone_number", personName.fullName))
    )))

  private def utrAnswerRow(mode: Mode, srn: Option[String]): AnswerRow =
  AnswerRow(Message("messages__enterUTR", personName.fullName), Seq("utr"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerEnterUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url,
      Some(Message("messages__visuallyhidden__dynamic_unique_taxpayer_reference", personName.fullName))
    )))

  private def answerRowWithNoLink: AnswerRow = AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, None)

  private def displayNino(answerRow: AnswerRow) = Seq(AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
      answerRow
    )
  ),
    AnswerSection(Some("messages__partner__cya__contact__details_heading"), Seq())
  )

  private def answerRowsYes = Seq(AnswerSection(
    None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None)
    )
  )
  )

  private def answerRowsNo = Seq(AnswerSection(
    None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None)
    )
  )
  )
}
