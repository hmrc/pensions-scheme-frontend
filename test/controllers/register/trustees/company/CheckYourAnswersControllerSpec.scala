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
import identifiers.register.trustees.company._
import models._
import models.address.Address
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.CheckYourAnswers.{PayeCYA, VatCYA}
import utils.checkyouranswers.Ops._
import utils.checkyouranswers.{AddressCYA, AddressYearsCYA, CompanyRegistrationNumberCYA, UniqueTaxReferenceCYA}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.checkYourAnswers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller " when {
    "on Page load in Normal Mode" must {
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

    "on Page load in UpdateMode" must {
      "return OK and the correct view for vat, crn and paye if not new trustee" in {
        val answers = UserAnswers().trusteesCompanyEnterVAT(index, ReferenceValue("098765432")).
          trusteesCompanyPayeVariations(index, ReferenceValue("12345678"))
          .trusteesCompanyCrnVariations(index, ReferenceValue("AB123456"))
        implicit val request = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = companyDetailsSection(
          CompanyEnterVATId(index).row(companyEnterVATRoute, UpdateMode) ++
            CompanyPayeVariationsId(index).row(companyPayeVariationsRoute, UpdateMode)++
            CompanyRegistrationNumberVariationsId(index).row(companyRegistrationNumberVariationsRoute, UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), UpdateMode, srn)
      }

      "return OK and the correct view for vat, crn and paye if new trustee" in {
        val answers = UserAnswers().trusteesCompanyVat(index, Vat.Yes("098765432")).
          trusteesCompanyPaye(index, Paye.Yes("12345678")).trusteesCompanyCrn(index, CompanyRegistrationNumber.Yes("AB2344576")).isTrusteeNew(index, true)
        implicit val request = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = companyDetailsSection(
          CompanyVatId(index).row(companyVatRoute(CheckUpdateMode, srn), UpdateMode) ++
          CompanyPayeId(index).row(companyPayeRoute(CheckUpdateMode, srn), UpdateMode)++
          CompanyRegistrationNumberId(index).row(companyRegistrationNumberRoute(CheckUpdateMode, srn), UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction, isToggleOn = true).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), UpdateMode, srn)
      }
    }

    "when rendering page" must {

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, index, None)(FakeDataRequest(fullAnswers))
      )
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  private val index = 0
  private val srn = Some("S123")
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
    .trusteesCompanyPaye(index, Paye.Yes("12345678"))

  private lazy val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, index, None).url
  private lazy val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index, None).url
  private def companyRegistrationNumberRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.CompanyRegistrationNumberController.onPageLoad(mode, srn, index).url
  private lazy val companyRegistrationNumberVariationsRoute = routes.CompanyRegistrationNumberVariationsController.onPageLoad(CheckUpdateMode, srn, index).url
  private lazy val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, index, None).url
  private def companyVatRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.CompanyVatController.onPageLoad(mode, index, srn).url
  private def companyPayeRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.CompanyPayeController.onPageLoad(mode, index, srn).url
  private lazy val companyPayeVariationsRoute = routes.CompanyPayeVariationsController.onPageLoad(CheckMode, index, srn).url
  private lazy val companyEnterVATRoute = routes.CompanyEnterVATController.onPageLoad(CheckMode, index, srn).url

  private def postUrl = controllers.routes.IndexController.onPageLoad()

  private def emptyContactDetailsSection =
    AnswerSection(Some("messages__checkYourAnswers__section__contact_details"), Nil)

  private def companyDetailsSection(vatRow: Seq[AnswerRow]) =
    AnswerSection(Some("messages__checkYourAnswers__section__company_details"), vatRow)

  private def answerSections(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId](
      label = "messages__checkYourAnswers__trustees__company__crn",
      changeHasCrn = "messages__visuallyhidden__trustee__crn_yes_no",
      changeCrn = "messages__visuallyhidden__trustee__crn",
      changeNoCrn = "messages__visuallyhidden__trustee__crn_no"
    )().row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute(), request.userAnswers)

    val utrRows = UniqueTaxReferenceCYA[CompanyUniqueTaxReferenceId](
      label = "messages__checkYourAnswers__trustees__company__utr",
      utrLabel = "messages__cya__utr",
      changeHasUtr = "messages__visuallyhidden__trustee__utr_yes_no",
      changeUtr = "messages__visuallyhidden__trustee__utr",
      changeNoUtr = "messages__visuallyhidden__trustee__utr_no"
    )().row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val payeRows = PayeCYA(
      Some("messages__checkYourAnswers__trustees__company__paye"),
      "messages__visuallyhidden__trustee__paye_yes_no",
      "messages__visuallyhidden__trustee__paye_number")().row(CompanyPayeId(index))(companyPayeRoute(), request.userAnswers)

    val vatRows = VatCYA(
      Some("messages__checkYourAnswers__trustees__company__vat"),
      "messages__visuallyhidden__trustee__vat_yes_no",
      "messages__visuallyhidden__trustee__vat_number")().row(CompanyVatId(index))(companyVatRoute(), request.userAnswers)

    val companyDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__company_details"),
      CompanyDetailsId(index).row(companyDetailsRoute) ++
        vatRows ++
        payeRows ++
        crnRows ++
        utrRows
    )

    def trusteeName(index: Int) = request.userAnswers.get(CompanyDetailsId(index)).fold(messages("messages__theTrustee"))(_.companyName)

    val addressYearsRows = {
      def label(index: Int) = messages("messages__trusteeAddressYears__heading", trusteeName(index))

      def changeAddressYears(index: Int) = messages("messages__visuallyhidden__dynamic_addressYears", trusteeName(index))
      AddressYearsCYA[CompanyAddressYearsId](
        label = label(index),
        changeAddressYears = changeAddressYears(index)
      )().row(CompanyAddressYearsId(index))(companyAddressYearsRoute, request.userAnswers)
    }

    val addressRows = {
      def label(index: Int) = messages("messages__trusteeAddress", trusteeName(index))
      def changeAddress(index: Int) = messages("messages__changeTrusteeAddress", trusteeName(index))
      AddressCYA[CompanyAddressId](
        label = label(index),
        changeAddress = changeAddress(index)
      )().row(CompanyAddressId(index))(companyAddressRoute, request.userAnswers)
    }

    val previousAddressRows = {
      def label(index: Int) = messages("messages__trusteePreviousAddress", trusteeName(index))
      def changeAddress(index: Int) = messages("messages__changeTrusteePreviousAddress", trusteeName(index))
      AddressCYA[CompanyPreviousAddressId](
        label = label(index),
        changeAddress = changeAddress(index)
      )().row(CompanyPreviousAddressId(index))(companyPreviousAddressRoute, request.userAnswers)
    }

    val contactDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__contact_details"),
      addressRows ++ addressYearsRows ++ previousAddressRows
    )
    Seq(companyDetailsSection, contactDetailsSection)
  }

  private def viewAsString(answerSections: Seq[AnswerSection], mode: Mode = NormalMode, srn: Option[String] = None) = checkYourAnswers(
    frontendAppConfig, answerSections, postUrl, None, srn = srn, hideEditLinks = false, hideSaveAndContinueButton = false
  )(fakeRequest, messages).toString

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach,
                         isToggleOn: Boolean = false): CheckYourAnswersController =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction, dataRetrievalAction,
      FakeAllowAccessProvider(), new DataRequiredActionImpl, fakeCountryOptions, new FakeNavigator(onwardRoute),
      FakeUserAnswersService, allowChangeHelper)

}