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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.IsTrusteeCompleteId
import identifiers.register.trustees.company._
import models._
import models.address.Address
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.CheckYourAnswers.VatCYA
import utils.checkyouranswers.Ops._
import utils.checkyouranswers.{AddressYearsCYA, CompanyRegistrationNumberCYA, UniqueTaxReferenceCYA}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller " when {
    "on Page load if toggle off/toggle on in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with empty answers" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }
    }

    "on Page load if toggle on in UpdateMode" must {
      "return OK and the correct view for vat if not new trustee" in {
        val answers = UserAnswers().trusteesCompanyVatVariations(index, "098765432")
        val request = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = companyDetailsSectionWithOnlyVat(
          Seq(AnswerRow("messages__common__cya__vat", Seq("098765432"), answerIsMessageKey = false, None))
        )
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), UpdateMode)
      }

      "return OK and the correct view for vat if new trustee" in {
        val answers = UserAnswers().trusteesCompanyVatVariations(index, "098765432").isTrusteeNew(index, true)
        val request = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = companyDetailsSectionWithOnlyVat(vatRow(answers))
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), UpdateMode)
      }
    }

    "on Submit" must {
      "redirect to next page " in {
        val result = controller().onSubmit(NormalMode, index, None)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "mark trustee company as complete" in {
        val result = controller().onSubmit(NormalMode, index, None)(fakeRequest)
        status(result) mustBe SEE_OTHER
        FakeUserAnswersService.verify(IsTrusteeCompleteId(index), true)
      }

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, index, None)(FakeDataRequest(fullAnswers))
      )
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  private val index = 0
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val schemeName = "test-scheme-name"
  private val companyDetails = CompanyDetails("test-company-name")
  private val crn = CompanyRegistrationNumber.Yes("test-crn")
  private val utr = UniqueTaxReference.Yes("test-utr")
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()

  private val fullAnswers = emptyAnswers
    .trusteesCompanyDetails(index, companyDetails)
    .trusteesCompanyRegistrationNumber(index, crn)
    .trusteesUniqueTaxReference(index, utr)
    .trusteesCompanyAddress(index, address)
    .trusteesCompanyAddressYears(index, addressYears)
    .trusteesCompanyPreviousAddress(index, previousAddress)
    .trusteesCompanyVat(index, Vat.Yes("123456789"))

  private lazy val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, index, None).url
  private lazy val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index, None).url
  private lazy val companyRegistrationNumberRoute = routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, index, None).url
  private lazy val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, index, None).url
  private lazy val companyVatRoute = routes.CompanyVatController.onPageLoad(CheckMode, index, None).url

  private def postUrl(mode: Mode) = routes.CheckYourAnswersController.onSubmit(mode, index, None)

  def vatRow(answers: UserAnswers): Seq[AnswerRow] = VatCYA(
    Some("messages__checkYourAnswers__trustees__company__vat"),
    "messages__visuallyhidden__trustee__vat_yes_no",
    "messages__visuallyhidden__trustee__vat_number")().row(CompanyVatId(index))(companyVatRoute, answers)

  private def emptyContactDetailsSection =
    AnswerSection(Some("messages__checkYourAnswers__section__contact_details"), Nil)

  private def companyDetailsSectionWithOnlyVat(vatRow: Seq[AnswerRow]) =
    AnswerSection(Some("messages__checkYourAnswers__section__company_details"), vatRow)

  private def answerSections(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId](
      label = "messages__checkYourAnswers__trustees__company__crn",
      changeHasCrn = "messages__visuallyhidden__trustee__crn_yes_no",
      changeCrn = "messages__visuallyhidden__trustee__crn",
      changeNoCrn = "messages__visuallyhidden__trustee__crn_no"
    )().row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute, request.userAnswers)

    val utrRows = UniqueTaxReferenceCYA[CompanyUniqueTaxReferenceId](
      label = "messages__checkYourAnswers__trustees__company__utr",
      utrLabel = "messages__company__cya__utr",
      changeHasUtr = "messages__visuallyhidden__trustee__utr_yes_no",
      changeUtr = "messages__visuallyhidden__trustee__utr",
      changeNoUtr = "messages__visuallyhidden__trustee__utr_no"
    )().row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val vatRows = vatRow(request.userAnswers)

    val companyDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__company_details"),
      CompanyDetailsId(index).row(companyDetailsRoute) ++
        vatRows ++
        crnRows ++
        utrRows
    )

    val addressYearsRows = AddressYearsCYA[CompanyAddressYearsId](
      label = "messages__checkYourAnswers__trustees__company__address_years",
      changeAddressYears = "messages__visuallyhidden__trustee__address_years"
    )().row(CompanyAddressYearsId(index))(companyAddressYearsRoute, request.userAnswers)

    val contactDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__contact_details"),
      CompanyAddressId(index).row(companyAddressRoute) ++
        addressYearsRows ++
        CompanyPreviousAddressId(index).row(companyPreviousAddressRoute)
    )
    Seq(companyDetailsSection, contactDetailsSection)
  }

  private def viewAsString(answerSections: Seq[AnswerSection], mode: Mode = NormalMode) = check_your_answers(
    frontendAppConfig, answerSections, postUrl(mode), None, hideEditLinks = false, hideSaveAndContinueButton = false
  )(fakeRequest, messages).toString

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach,
                         isToggleOn: Boolean = false): CheckYourAnswersController =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction, dataRetrievalAction,
      FakeAllowAccessProvider(), new DataRequiredActionImpl, fakeCountryOptions, new FakeNavigator(onwardRoute),
      FakeUserAnswersService, allowChangeHelper, new FakeFeatureSwitchManagementService(isToggleOn))

}
