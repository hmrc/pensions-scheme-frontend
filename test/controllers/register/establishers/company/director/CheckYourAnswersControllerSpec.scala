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

package controllers.register.establishers.company.director

import base.SpecBase
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director._
import models._
import models.address.Address
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, DateHelper, FakeCountryOptions, FakeDataRequest, FakeFeatureSwitchManagementService, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(directorAnswers)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach, toggle:Boolean = false): CheckYourAnswersController =
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

  private def directorDetails(mode:Mode, srn : Option[String] = None) = AnswerSection(
    Some("messages__director__cya__details_heading"),
    Seq(
      DirectorDetailsId(firstIndex, firstIndex).
        row(routes.DirectorDetailsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      DirectorNinoId(firstIndex, firstIndex).
        row(routes.DirectorNinoController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      DirectorUniqueTaxReferenceId(firstIndex, firstIndex).
        row(routes.DirectorUniqueTaxReferenceController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url)
    ).flatten
  )

  private def directorContactDetails(mode:Mode, srn : Option[String] = None) = AnswerSection(
    Some("messages__director__cya__contact__details_heading"),
    Seq(
      DirectorAddressId(firstIndex, firstIndex).
        row(routes.DirectorAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      DirectorAddressYearsId(firstIndex, firstIndex).
        row(routes.DirectorAddressYearsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      DirectorPreviousAddressId(firstIndex, firstIndex).
        row(routes.DirectorPreviousAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
      DirectorContactDetailsId(firstIndex, firstIndex).
        row(routes.DirectorContactDetailsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url)
    ).flatten
  )

  private def viewAsString(mode:Mode = NormalMode,
                   answerSection : Seq[AnswerSection] = Seq(directorDetails(NormalMode), directorContactDetails(NormalMode)),
                   srn : Option[String] =  None) = check_your_answers(
    frontendAppConfig,
    answerSection,
    routes.CheckYourAnswersController.onSubmit(firstIndex, firstIndex, mode, srn),
    None,
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    srn = srn
  )(fakeRequest, messages).toString

  "CheckYourAnswersController for Directors" when {
    "onPageLoad" must {

      "return OK and display all the answers" in {

        val result = controller(directorAnswers.dataRetrievalAction).onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "return OK and display all given answers for UpdateMode" in {

        val result = controller(directorAnswersUpdate.dataRetrievalAction).onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, updateAnswerRows, Some("srn"))
      }

      "return OK and display new Nino with Add link for UpdateMode and separateRefCollectionEnabled is true" in {

        val result = controller(directorDetailsAnswersUpdateWithoutNino.dataRetrievalAction, toggle = true).
          onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, Some("srn"))
      }

      "return OK and display new Nino with no link for UpdateMode and separateRefCollectionEnabled is true" in {

        val result = controller(directorAnswersUpdateWithNewNino.dataRetrievalAction, toggle = true).
          onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithNoLink, Some("srn"))
      }

      "return OK and display old Nino links for UpdateMode, New Director and separateRefCollectionEnabled is true" in {

        val result = controller(newDirectorAnswersUpdateWithOldNino.dataRetrievalAction, toggle = true).
          onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode, updateAnswerRowsWithChange, Some("srn"))
      }

      behave like changeableController(
        controller(directorAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)
      )
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(firstIndex, firstIndex, NormalMode, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
      }

      "mark the section as complete and redirect to the next page in UpdateMode if Establisher is new" in {
        val result = controller(newDirectorAnswers.dataRetrievalAction).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
      }

      "mark the section as complete and redirect to the next page in UpdateMode if Establisher is not new" in {
        val result = controller(directorAnswers.dataRetrievalAction).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends SpecBase {
  val firstIndex = Index(0)
  val schemeName = "test scheme name"
  val desiredRoute = controllers.routes.IndexController.onPageLoad()

  val directorPersonDetails = PersonDetails("first name", None, "last name", LocalDate.now(), false)

  implicit val directorDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(DirectorDetailsId(firstIndex, firstIndex))(directorPersonDetails)
    .asOpt.value


  implicit val directorAnswersUpdate = directorDetailsAnswersUpdateWithoutNino
    .set(DirectorNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")).asOpt.value

  implicit val directorAnswersUpdateWithNewNino = directorDetailsAnswersUpdateWithoutNino
    .set(DirectorNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value

  implicit val directorAnswers = directorAnswersUpdate
    .set(DirectorUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(DirectorAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(DirectorPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  val newDirectorAnswers = directorAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value

  implicit val newDirectorAnswersUpdateWithOldNino = directorAnswersUpdate
    .set(IsNewDirectorId(firstIndex, firstIndex))(true)
    .flatMap(_.set(DirectorNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")))
    .asOpt.value

  private def updateAnswerRows = Seq(AnswerSection(
      Some("messages__director__cya__details_heading"),
      Seq(
        AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
        AnswerRow(messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName), Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
        AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, None)
      )
    ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )

  private def updateAnswerRowsWithChange = Seq(AnswerSection(
      Some("messages__director__cya__details_heading"),
      Seq(
        AnswerRow("messages__director__cya__name", Seq("first name last name"), false,
          Some(Link("site.change", routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
            Some(Message("messages__visuallyhidden__common__name", "first name last name").resolve)))),
        AnswerRow(messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName), Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false,
          Some(Link("site.change", routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
            Some(Message("messages__visuallyhidden__common__dob", "first name last name").resolve)))),
        AnswerRow("messages__director_nino_question_cya_label", Seq(s"${Nino.Yes}"), answerIsMessageKey = false,
          Some(Link("site.change", routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
            Some("messages__visuallyhidden__director__nino_yes_no")))),
        AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false,
          Some(Link("site.change", routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
            Some("messages__visuallyhidden__director__nino"))))
      )
    ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )

  private def displayNewNinoAnswerRowWithAdd = Seq(AnswerSection(
      Some("messages__director__cya__details_heading"),
      Seq(
        AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
        AnswerRow(messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName), Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
        AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
          Some(Link("site.add",
            routes.DirectorNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
            Some(s"messages__visuallyhidden__director__nino_add"))))
      )
    ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )

  def displayNewNinoAnswerRowWithNoLink = Seq(AnswerSection(
      Some("messages__director__cya__details_heading"),
      Seq(
        AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
        AnswerRow(messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName), Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
        AnswerRow("messages__common__nino", Seq("AB100100A"), answerIsMessageKey = false, None)
      )
    ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )
}
