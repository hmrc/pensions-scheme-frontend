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
import config.FeatureSwitchManagementService
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

  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(directorAnswers)
  implicit val userAnswers = request.userAnswers

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach,
                         toggle: Boolean
                        ): CheckYourAnswersController =
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

  private def viewAsString(mode: Mode,
                           answerSection: (Mode, Option[String]) => Seq[AnswerSection],
                           srn: Option[String]): String =
    viewAsString(mode, answerSection(NormalMode, srn), srn)

  private def viewAsString(mode: Mode = NormalMode,
                           answerSection: Seq[AnswerSection],
                           srn: Option[String] = None): String =
    check_your_answers(
      frontendAppConfig,
      answerSection,
      routes.CheckYourAnswersController.onSubmit(firstIndex, firstIndex, mode, srn),
      None,
      hideEditLinks = false,
      hideSaveAndContinueButton = false,
      srn = srn
    )(fakeRequest, messages).toString

  "CheckYourAnswersController for Directors" when {
    "toggle isEstablisherCompanyHnSEnabled is false" when {
      implicit val fs: FakeFeatureSwitchManagementService = new FakeFeatureSwitchManagementService(false)


      def answerSectionForIsEstablisherCompanyHnSEnabled(mode: Mode, srn: Option[String] = None)(implicit fs: FeatureSwitchManagementService): Seq[AnswerSection] = Seq(
        AnswerSection(
          Some("messages__director__cya__details_heading"),
          Seq(
            DirectorDetailsId(firstIndex, firstIndex).
              row(routes.DirectorDetailsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
            DirectorNinoId(firstIndex, firstIndex).
              row(routes.DirectorNinoController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url),
            DirectorUniqueTaxReferenceId(firstIndex, firstIndex).
              row(routes.DirectorUniqueTaxReferenceController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url)
          ).flatten
        ),
        AnswerSection(
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
      )


      "onPageLoad" must {

        "return OK and display all the answers" in {
          val result = controller(directorAnswers.dataRetrievalAction, toggle = false).onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(NormalMode, answerSectionForIsEstablisherCompanyHnSEnabled _, None)
        }

        "return OK and display all given answers for UpdateMode" in {

          val result = controller(directorAnswersUpdate.dataRetrievalAction, toggle = false).onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, Some("srn"))
        }

        "return OK and display new Nino with Add link for UpdateMode" in {

          val result = controller(directorDetailsAnswersUpdateWithoutNino.dataRetrievalAction, toggle = false).
            onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, Some("srn"))
        }

        "return OK and display new Nino with no link for UpdateMode" in {

          val displayNewNinoAnswerRowWithNoLink = Seq(
            AnswerSection(
              Some("messages__director__cya__details_heading"),
              Seq(
                AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
                AnswerRow(messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName), Seq(DateHelper.formatDate(LocalDate.now())), answerIsMessageKey = false, None),
                AnswerRow(messages("messages__common__nino", directorPersonDetails.firstAndLastName), Seq("AB100100A"), answerIsMessageKey = false, None)
              )
            ),
            AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
          )


          val directorAnswersUpdateWithNewNino = directorDetailsAnswersUpdateWithoutNino
            .set(DirectorNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value

          val result = controller(directorAnswersUpdateWithNewNino.dataRetrievalAction, toggle = false).
            onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithNoLink, Some("srn"))
        }

        "return OK and display old Nino links for UpdateMode, New Director" in {
          val newDirectorAnswersUpdateWithOldNino = directorAnswersUpdate
            .set(IsNewDirectorId(firstIndex, firstIndex))(true)
            .flatMap(_.set(DirectorNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")))
            .asOpt.value

          val result = controller(newDirectorAnswersUpdateWithOldNino.dataRetrievalAction, toggle = false).
            onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)


          val expectedAnswerRowsForUpdateWithChange = Seq(AnswerSection(
            Some("messages__director__cya__details_heading"),
            Seq(
              AnswerRow(
                "messages__director__cya__name",
                Seq("first name last name"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
                  Some(Message("messages__visuallyhidden__common__name", "first name last name").resolve)
                ))
              ),
              AnswerRow(
                messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName),
                Seq(DateHelper.formatDate(LocalDate.now())),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
                  Some(Message("messages__visuallyhidden__common__dob", "first name last name").resolve)
                ))
              ),
              AnswerRow(
                messages("messages__director__cya__nino", directorPersonDetails.firstAndLastName),
                Seq(s"${Nino.Yes}"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
                  Some("messages__visuallyhidden__director__nino_yes_no")
                ))
              ),
              AnswerRow(
                "messages__common__nino",
                Seq("AB100100A"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
                  Some("messages__visuallyhidden__director__nino")
                ))
              )
            )
          ),
            AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
          )

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, expectedAnswerRowsForUpdateWithChange, Some("srn"))
        }

        behave like changeableController(
          controller(directorAnswers.dataRetrievalAction, _: AllowChangeHelper, toggle = false)
            .onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)
        )
      }

      "onSubmit" must {
        "mark the section as complete and redirect to the next page" in {
          val result = controller(toggle = false).onSubmit(firstIndex, firstIndex, NormalMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }

        "mark the section as complete and redirect to the next page in UpdateMode if Establisher is new" in {
          val newDirectorAnswers = directorAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value

          val result = controller(newDirectorAnswers.dataRetrievalAction, toggle = false).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }

        "mark the section as complete and redirect to the next page in UpdateMode if Establisher is not new" in {
          val result = controller(directorAnswers.dataRetrievalAction, toggle = false).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }
      }
    }

    "toggle isEstablisherCompanyHnSEnabled is true" when {

      def answerSectionForIsEstablisherCompanyHnSDisabled(mode: Mode, srn: Option[String] = None)(implicit fs: FeatureSwitchManagementService): Seq[AnswerSection] =
        Seq(
          AnswerSection(
            None,
            Seq(
              DirectorNameId(firstIndex, firstIndex).row(routes.DirectorNameController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorDOBId(firstIndex, firstIndex).row(routes.DirectorDOBController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorHasNINOId(firstIndex, firstIndex).row(routes.DirectorHasNINOController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorNewNinoId(firstIndex, firstIndex).row(routes.DirectorNinoNewController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorNoNINOReasonId(firstIndex, firstIndex).row(routes.DirectorNoNINOReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorHasUTRId(firstIndex, firstIndex).row(routes.DirectorHasUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorUTRId(firstIndex, firstIndex).row(routes.DirectorUTRController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorNoUTRReasonId(firstIndex, firstIndex).row(routes.DirectorNoUTRReasonController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorAddressId(firstIndex, firstIndex).row(routes.DirectorAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorAddressYearsId(firstIndex, firstIndex).row(routes.DirectorAddressYearsController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorPreviousAddressId(firstIndex, firstIndex).row(routes.DirectorPreviousAddressController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorEmailId(firstIndex, firstIndex).row(routes.DirectorEmailController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode),

              DirectorPhoneNumberId(firstIndex, firstIndex).row(routes.DirectorPhoneNumberController.onPageLoad(Mode.checkMode(mode), firstIndex, firstIndex, srn).url, mode)
            ).flatten
          )
        )


      "onPageLoad" must {
        implicit val fs: FakeFeatureSwitchManagementService = new FakeFeatureSwitchManagementService(true)

        "return OK and display all the answers" in {


          val result = controller(directorAnswers.dataRetrievalAction, toggle = true).onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(NormalMode, answerSectionForIsEstablisherCompanyHnSDisabled _, None)
        }

        "return OK and display all given answers for UpdateMode" in {
          val result = controller(directorAnswers.dataRetrievalAction, toggle = true).onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, answerSectionForIsEstablisherCompanyHnSDisabled(UpdateMode, Some("srn"))(fs), Some("srn"))
        }

        "return OK and display new Nino with no link for UpdateMode" in {

          val directorAnswersUpdateWithNewNino = UserAnswers()
            .set(DirectorDetailsId(firstIndex, firstIndex))(directorPersonDetails).asOpt.value
            .set(DirectorNewNinoId(firstIndex, firstIndex))(ReferenceValue("AB100100A")).asOpt.value


          val req: FakeDataRequest = FakeDataRequest(directorAnswersUpdateWithNewNino)

          val displayNewNinoAnswerRowWithNoLink = Seq(
            AnswerSection(
              None,
              DirectorNameId(firstIndex, firstIndex)
                .row(routes.DirectorNameController.onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn")).url, UpdateMode)(req, implicitly) ++

              DirectorDOBId(firstIndex, firstIndex)
                .row(routes.DirectorDOBController.onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn")).url, UpdateMode)(req, implicitly) ++

              DirectorNewNinoId(firstIndex, firstIndex)
                .row(routes.DirectorNinoNewController.onPageLoad(UpdateMode, firstIndex, firstIndex, Some("srn")).url, UpdateMode)(req, implicitly)
            )
          )

          val result = controller(directorAnswersUpdateWithNewNino.dataRetrievalAction, toggle = true).
            onPageLoad(firstIndex, firstIndex, UpdateMode, Some("srn"))(req)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithNoLink, Some("srn"))
        }

        behave like changeableController(
          controller(directorAnswers.dataRetrievalAction, _: AllowChangeHelper, toggle = true)
            .onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)
        )
      }

      "onSubmit" when {
        "mark the section as complete and redirect to the next page" in {
          val result = controller(toggle = true).onSubmit(firstIndex, firstIndex, NormalMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }

        "mark the section as complete and redirect to the next page in UpdateMode if Establisher is new" in {
          val newDirectorAnswers = directorAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value

          val result = controller(newDirectorAnswers.dataRetrievalAction, toggle = true).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }

        "mark the section as complete and redirect to the next page in UpdateMode if Establisher is not new" in {
          val result = controller(directorAnswers.dataRetrievalAction, toggle = true).onSubmit(firstIndex, firstIndex, UpdateMode, None)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(desiredRoute.url)

          FakeUserAnswersService.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
        }
      }
    }
  }

}

object CheckYourAnswersControllerSpec extends SpecBase {
  val firstIndex = Index(0)
  val schemeName = "test scheme name"
  val desiredRoute = controllers.routes.IndexController.onPageLoad()

  val directorPersonDetails = PersonDetails("first name", None, "last name", LocalDate.now(), false)

  val directorDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(DirectorDetailsId(firstIndex, firstIndex))(directorPersonDetails)
    .asOpt.value

  val directorAnswersUpdate = directorDetailsAnswersUpdateWithoutNino
    .set(DirectorNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")).asOpt.value

  val directorAnswers = directorAnswersUpdate
    .set(DirectorUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(DirectorAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(DirectorPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  val displayNewNinoAnswerRowWithAdd = Seq(AnswerSection(
    Some("messages__director__cya__details_heading"),
    Seq(
      AnswerRow("messages__director__cya__name", Seq("first name last name"), false, None),
      AnswerRow(
        messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName),
        Seq(DateHelper.formatDate(LocalDate.now())),
        answerIsMessageKey = false,
        None
      ),
      AnswerRow(
        messages("messages__common__nino", directorPersonDetails.firstAndLastName),
        Seq("site.not_entered"),
        answerIsMessageKey = true,
        Some(Link(
          "site.add",
          routes.DirectorNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), firstIndex, firstIndex, Some("srn")).url,
          Some(s"messages__visuallyhidden__director__nino_add")
        ))
      )
    )
  ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )
}
