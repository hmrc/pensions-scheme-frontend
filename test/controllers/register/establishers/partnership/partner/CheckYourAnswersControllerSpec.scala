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
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
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
      allowChangeHelper
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

      "return OK and display all given answers for UpdateMode with add/change link when user enters nino" in {

        val result = controller(partnerAnswersUpdateWithNewNino(true).dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNino(answerRowWithChange), Some("srn"))
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
  private val firstIndex = Index(0)
  private val schemeName = "test scheme name"
  private val desiredRoute = controllers.routes.IndexController.onPageLoad()

  private val partnerDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(PartnerDetailsId(firstIndex, firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .asOpt.value

  private val partnerAnswersUpdate = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value

  private def partnerAnswersUpdateWithNewNino(isEditable: Boolean): UserAnswers = partnerDetailsAnswersUpdateWithoutNino
    .set(PartnerNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A", isEditable)).asOpt.value

  private val partnerAnswers = partnerAnswersUpdate
    .set(PartnerUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(PartnerAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnerPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  private val newPartnerAnswers = partnerAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value

  private def answerRowWithAdd: AnswerRow = AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
    Some(Link("site.add",
      routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
      Some(s"messages__visuallyhidden__partner__nino"))))

  private def answerRowWithChange: AnswerRow = AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, Some(
    Link("site.change", routes.PartnerNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
      Some("messages__visuallyhidden__partner__nino")
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
}
