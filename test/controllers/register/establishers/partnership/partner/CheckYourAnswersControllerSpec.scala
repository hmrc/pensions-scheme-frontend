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
import models.address.Address
import models.person.PersonDetails
import models.{Index, _}
import org.joda.time.LocalDate
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import utils.checkyouranswers.Ops._
import utils.{FakeCountryOptions, FakeDataRequest, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(partnerAnswers)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach, toggle: Boolean = false): CheckYourAnswersController =
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
      new FakeFeatureSwitchManagementService(toggle)
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
                           srn: Option[String] = None) = check_your_answers(
    frontendAppConfig,
    answerSection,
    routes.CheckYourAnswersController.onSubmit(mode, firstIndex, firstIndex, srn),
    None,
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    srn = srn
  )(fakeRequest, messages).toString

  "CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {


        val result = controller(partnerAnswers.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and display all given answers for UpdateMode" in {

        val result = controller(partnerAnswersExistingNino.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, updateAnswerRows, Some("srn"))
      }

      "return OK and display new Nino with Add link for UpdateMode and separateRefCollectionEnabled is true" in {

        val result = controller(partnerDetailsAnswersUpdateWithoutNino.dataRetrievalAction, toggle = true).
          onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, Some("srn"))
      }

      "return OK and display new Nino with no link for UpdateMode and separateRefCollectionEnabled is true" in {

        val result = controller(partnerAnswersUpdate.dataRetrievalAction, toggle = true).onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithNoLink, Some("srn"))
      }

      "return OK and display old Nino links for UpdateMode, New Partner and separateRefCollectionEnabled is true" in {

        val result = controller(newPartnerAnswersUpdateWithOldNino.dataRetrievalAction, toggle = true).
          onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, updateAnswerRowsWithChange, Some("srn"))
      }

      behave like changeableController(
        controller(partnerAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, firstIndex, firstIndex, None)(request)
      )
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page in NormalMode" in {
        val result = controller().onSubmit(NormalMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsPartnerCompleteId(firstIndex, firstIndex), true)
      }

      "mark the section as complete and redirect to the next page in UpdateMode if Establisher is new" in {
        val result = controller(newPartnerAnswers.dataRetrievalAction).onSubmit(UpdateMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsPartnerCompleteId(firstIndex, firstIndex), true)
      }

      "mark the section as complete and redirect to the next page in UpdateMode if Establisher is not new" in {
        val result = controller(partnerAnswers.dataRetrievalAction).onSubmit(UpdateMode, firstIndex, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsPartnerCompleteId(firstIndex, firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends SpecBase {
  val firstIndex = Index(0)
  val schemeName = "test scheme name"
  val desiredRoute = controllers.routes.IndexController.onPageLoad()

  implicit val partnerDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(PartnerDetailsId(firstIndex, firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .asOpt.value

  implicit val partnerAnswersUpdate = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value

  implicit val partnerAnswersExistingNino = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")).asOpt.value

  val partnerAnswers = partnerAnswersUpdate
    .set(PartnerUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(PartnerAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnerPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  val newPartnerAnswers = partnerAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value

  implicit val newPartnerAnswersUpdateWithOldNino = partnerAnswersUpdate
    .set(IsNewPartnerId(firstIndex, firstIndex))(true)
    .flatMap(_.set(PartnerNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")))
    .asOpt.value

  private def updateAnswerRows = Seq(AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
      AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, None)
    )
  ),
    AnswerSection(Some("messages__partner__cya__contact__details_heading"), Seq())
  )

  private def updateAnswerRowsWithChange = Seq(AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false,
        Some(Link("site.change", routes.PartnerDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some(Message("messages__visuallyhidden__common__name", "first name last name").resolve)))),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false,
        Some(Link("site.change", routes.PartnerDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some(Message("messages__visuallyhidden__common__dob", "first name last name").resolve)))),
      AnswerRow("messages__partner_nino_question_cya_label", Seq(s"${Nino.Yes}"), answerIsMessageKey = false,
        Some(Link("site.change", routes.PartnerNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some("messages__visuallyhidden__partner__nino_yes_no")))),
      AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false,
        Some(Link("site.change", routes.PartnerNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some("messages__visuallyhidden__partner__nino"))))
    )
  ),
    AnswerSection(Some("messages__partner__cya__contact__details_heading"), Seq())
  )

  private def displayNewNinoAnswerRowWithAdd = Seq(AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
      AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
        Some(Link("site.add",
          routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some(s"messages__visuallyhidden__partner__nino_add"))))
    )
  ),
    AnswerSection(Some("messages__partner__cya__contact__details_heading"), Seq())
  )

  private def displayNewNinoAnswerRowWithNoLink = Seq(AnswerSection(
    Some("messages__partner__cya__details_heading"),
    Seq(
      AnswerRow("messages__common__cya__name", Seq("first name last name"), false, None),
      AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
      AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, None)
    )
  ),
    AnswerSection(Some("messages__partner__cya__contact__details_heading"), Seq())
  )
}
