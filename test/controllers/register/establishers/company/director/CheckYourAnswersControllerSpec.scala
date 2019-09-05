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
import controllers.register.establishers.company.routes.AddCompanyDirectorsController
import identifiers.register.establishers.company.director._
import models._
import models.address.Address
import models.person.{PersonDetails, PersonName}
import models.requests.DataRequest
import navigators.{EstablishersCompanyDirectorNavigator, EstablishersCompanyNavigator}
import org.joda.time.LocalDate
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.checkyouranswers.Ops._
import utils.{AllowChangeHelper, DateHelper, FakeCountryOptions, FakeDataRequest, FakeFeatureSwitchManagementService, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.checkYourAnswers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()
  implicit val request: DataRequest[AnyContent] = FakeDataRequest(directorAnswers)
  implicit val userAnswers: UserAnswers = request.userAnswers

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
      countryOptions,
      allowChangeHelper,
      new FakeFeatureSwitchManagementService(toggle)
    )

  private def viewAsString(mode: Mode,
                           answerSection: (Mode, Option[String]) => Seq[AnswerSection],
                           href: Call,
                           srn: Option[String]
                          ): String =
    viewAsString(mode, answerSection(NormalMode, srn), href, srn)

  private def viewAsString(mode: Mode = NormalMode,
                           answerSection: Seq[AnswerSection],
                           href: Call,
                           srn: Option[String] = None): String =
    checkYourAnswers(
      frontendAppConfig,
      answerSection,
      href,
      None,
      hideEditLinks = false,
      hideSaveAndContinueButton = false,
      srn = srn
    )(fakeRequest, messages).toString

  "CheckYourAnswersController for Directors" when {
    "toggle isEstablisherCompanyHnSEnabled is false" when {
      implicit val featureSwitchManagementService: FeatureSwitchManagementService = new FakeFeatureSwitchManagementService(false)
      def answerSectionForIsEstablisherCompanyHnSEnabled(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] = Seq(
        AnswerSection(
          Some("messages__director__cya__details_heading"),
          Seq(
            DirectorDetailsId(index, index).
              row(routes.DirectorDetailsController.onPageLoad(Mode.checkMode(mode), index, index, srn).url),
            DirectorNinoId(index, index).
              row(routes.DirectorNinoController.onPageLoad(Mode.checkMode(mode), index, index, srn).url),
            DirectorUniqueTaxReferenceId(index, index).
              row(routes.DirectorUniqueTaxReferenceController.onPageLoad(Mode.checkMode(mode), index, index, srn).url)
          ).flatten
        ),
        AnswerSection(
          Some("messages__director__cya__contact__details_heading"),
          Seq(
            DirectorAddressId(index, index).
              row(routes.DirectorAddressController.onPageLoad(Mode.checkMode(mode), index, index, srn).url),
            DirectorAddressYearsId(index, index).
              row(routes.DirectorAddressYearsController.onPageLoad(Mode.checkMode(mode), index, index, srn).url),
            DirectorPreviousAddressId(index, index).
              row(routes.DirectorPreviousAddressController.onPageLoad(Mode.checkMode(mode), index, index, srn).url),
            DirectorContactDetailsId(index, index).
              row(routes.DirectorContactDetailsController.onPageLoad(Mode.checkMode(mode), index, index, srn).url)
          ).flatten
        )
      )

      "onPageLoad" must {

        "return OK and display all the answers" in {
          val result = controller(directorAnswers.dataRetrievalAction, toggle = false).onPageLoad(index, index, NormalMode, None)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(NormalMode, answerSectionForIsEstablisherCompanyHnSEnabled _, href(NormalMode, None, 0), None)
        }

        "return OK and display all given answers for UpdateMode" in {

          val result = controller(directorAnswersUpdate.dataRetrievalAction, toggle = false).
            onPageLoad(index, index, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, href(UpdateMode, Some("srn"), 0), Some("srn"))
        }

        "return OK and display new Nino with Add link for UpdateMode" in {

          val result = controller(directorDetailsAnswersUpdateWithoutNino.dataRetrievalAction, toggle = false).
            onPageLoad(index, index, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithAdd, href(UpdateMode, Some("srn"), 0), Some("srn"))
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
            .set(DirectorNewNinoId(index, index))(ReferenceValue("AB100100A")).asOpt.value

          val result = controller(directorAnswersUpdateWithNewNino.dataRetrievalAction, toggle = false).
            onPageLoad(index, index, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, displayNewNinoAnswerRowWithNoLink, href(UpdateMode, Some("srn"), 0), Some("srn"))
        }

        "return OK and display old Nino links for UpdateMode, New Director" in {
          val newDirectorAnswersUpdateWithOldNino = directorAnswersUpdate
            .set(IsNewDirectorId(index, index))(true)
            .flatMap(_.set(DirectorNinoId(index, index))(Nino.Yes("AB100100A")))
            .asOpt.value

          val result = controller(newDirectorAnswersUpdateWithOldNino.dataRetrievalAction, toggle = false).
            onPageLoad(index, index, UpdateMode, Some("srn"))(request)


          val expectedAnswerRowsForUpdateWithChange = Seq(AnswerSection(
            Some("messages__director__cya__details_heading"),
            Seq(
              AnswerRow(
                "messages__director__cya__name",
                Seq("first name last name"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), index, index, Some("srn")).url,
                  Some(Message("messages__visuallyhidden__common__name", "first name last name").resolve)
                ))
              ),
              AnswerRow(
                messages("messages__director__cya__dob", directorPersonDetails.firstAndLastName),
                Seq(DateHelper.formatDate(LocalDate.now())),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorDetailsController.onPageLoad(Mode.checkMode(UpdateMode), index, index, Some("srn")).url,
                  Some(Message("messages__visuallyhidden__common__dob", "first name last name").resolve)
                ))
              ),
              AnswerRow(
                messages("messages__director__cya__nino", directorPersonDetails.firstAndLastName),
                Seq(s"${Nino.Yes}"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), index, index, Some("srn")).url,
                  Some("messages__visuallyhidden__director__nino_yes_no")
                ))
              ),
              AnswerRow(
                "messages__common__nino",
                Seq("AB100100A"),
                answerIsMessageKey = false,
                Some(Link(
                  "site.change",
                  routes.DirectorNinoController.onPageLoad(Mode.checkMode(UpdateMode), index, index, Some("srn")).url,
                  Some("messages__visuallyhidden__director__nino")
                ))
              )
            )
          ),
            AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
          )

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, expectedAnswerRowsForUpdateWithChange, href(UpdateMode, Some("srn"), 0), Some("srn"))
        }

        behave like changeableController(
          controller(directorAnswers.dataRetrievalAction, _: AllowChangeHelper, toggle = false)
            .onPageLoad(index, index, NormalMode, None)(request)
        )
      }
    }

    "toggle isEstablisherCompanyHnSEnabled is true" when {
      val request: DataRequest[AnyContent] = FakeDataRequest(directorAnswersHnsEnabled)
      implicit val userAnswers: UserAnswers = request.userAnswers
      implicit val featureSwitchManagementService: FeatureSwitchManagementService = new FakeFeatureSwitchManagementService(true)
      def answerSectionDirectorHnSEnabled(mode: Mode, srn: Option[String] = None): Seq[AnswerSection] =
        Seq(
          AnswerSection(
            None,
            Seq(
              DirectorNameId(index, index).row(routes.DirectorNameController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorDOBId(index, index).row(routes.DirectorDOBController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorHasNINOId(index, index).row(routes.DirectorHasNINOController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorNewNinoId(index, index).row(routes.DirectorNinoNewController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorNoNINOReasonId(index, index).row(routes.DirectorNoNINOReasonController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorHasUTRId(index, index).row(routes.DirectorHasUTRController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorUTRId(index, index).row(routes.DirectorUTRController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorNoUTRReasonId(index, index).row(routes.DirectorNoUTRReasonController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorAddressId(index, index).row(routes.DirectorAddressController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorAddressYearsId(index, index).row(routes.DirectorAddressYearsController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorPreviousAddressId(index, index).row(routes.DirectorPreviousAddressController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorEmailId(index, index).row(routes.DirectorEmailController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly),
              DirectorPhoneNumberId(index, index).row(routes.DirectorPhoneNumberController.onPageLoad(Mode.checkMode(mode), index, index, srn).url, mode)(request, implicitly)
            ).flatten
          )
        )

      "onPageLoad" must {

        "return OK and display all the answers" in {
          val result = controller(directorAnswersHnsEnabled.dataRetrievalAction, toggle = true).onPageLoad(index, index, NormalMode, None)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(NormalMode, answerSectionDirectorHnSEnabled _, href(NormalMode, None, 0), None)
        }

        "return OK and display all given answers for UpdateMode" in {
          val result = controller(directorAnswersHnsEnabled.dataRetrievalAction, toggle = true).onPageLoad(index, index, UpdateMode, Some("srn"))(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(UpdateMode, answerSectionDirectorHnSEnabled(UpdateMode, Some("srn")), href(UpdateMode, Some("srn"), 0), Some("srn"))
        }

        behave like changeableController(
          controller(directorAnswersHnsEnabled.dataRetrievalAction, _: AllowChangeHelper, toggle = true)
            .onPageLoad(index, index, NormalMode, None)(request)
        )
      }
    }
  }

}

object CheckYourAnswersControllerSpec extends SpecBase {
  val index = Index(0)
  val schemeName = "test scheme name"
  def href(mode: Mode, srn: Option[String], companyIndex: Int) = AddCompanyDirectorsController.onPageLoad(mode, srn, companyIndex)
  val name = "First Name"

  val directorPersonDetails = PersonDetails("first name", None, "last name", LocalDate.now(), false)

  val directorDetailsAnswersUpdateWithoutNino = UserAnswers()
    .set(DirectorDetailsId(index, index))(directorPersonDetails)
    .asOpt.value

  val directorAnswersUpdate = directorDetailsAnswersUpdateWithoutNino
    .set(DirectorNinoId(index, index))(Nino.Yes("AB100100A")).asOpt.value

  val directorAnswers = directorAnswersUpdate
    .set(DirectorUniqueTaxReferenceId(index, index))(UniqueTaxReference.Yes("1234567890"))
    .flatMap(_.set(DirectorAddressId(index, index))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorAddressYearsId(index, index))(AddressYears.UnderAYear))
    .flatMap(_.set(DirectorPreviousAddressId(index, index))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorContactDetailsId(index, index))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value

  val directorAnswersHnsEnabled: UserAnswers = UserAnswers().set(DirectorNameId(index, index))(PersonName("First", "Last")).flatMap(
    _.set(DirectorNewNinoId(index, index))(ReferenceValue("AB100100A")).flatMap(
      _.set(DirectorUTRId(index, index))(ReferenceValue("1234567890")).flatMap(_.set(DirectorAddressId(index, index))
      (Address("Address 1", "Address 2", None, None, None, "GB")))
        .flatMap(_.set(DirectorAddressYearsId(index, index))(AddressYears.UnderAYear))
        .flatMap(_.set(DirectorPreviousAddressId(index, index))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB"))).flatMap(
        _.set(DirectorEmailId(index, index))("test@test.com").flatMap(
          _.set(DirectorPhoneNumberId(index, index))("123456789").flatMap(
            _.set(DirectorDOBId(index, index))(LocalDate.now())
          )
        )
      )
    )
  ).asOpt.value

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
          routes.DirectorNinoNewController.onPageLoad(Mode.checkMode(UpdateMode), index, index, Some("srn")).url,
          Some(s"messages__visuallyhidden__director__nino_add")
        ))
      )
    )
  ),
    AnswerSection(Some("messages__director__cya__contact__details_heading"), Seq())
  )
}
