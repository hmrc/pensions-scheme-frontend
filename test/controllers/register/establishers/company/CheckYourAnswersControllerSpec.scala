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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.routes._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import models._
import models.address.Address
import models.register.DeclarationDormant
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.checkyouranswers.CheckYourAnswers.{ContactDetailsCYA, PayeCYA, VatCYA}
import utils.checkyouranswers.Ops._
import utils.checkyouranswers._
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.checkYourAnswers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      "return OK and the correct view with empty answers" in {
        val request = FakeDataRequest(emptyAnswers)
        val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }
    }

    "on Page load in UpdateMode" must {
      "return OK and the correct view for vat, paye and crn if not new establisher" in {
        val answers = UserAnswers().set(CompanyEnterVATId(index))(ReferenceValue("098765432")).flatMap(
          _.set(CompanyPayeVariationsId(index))(ReferenceValue("12345678"))).asOpt.value
        implicit val request: FakeDataRequest = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = estCompanyDetailsSection(
          CompanyEnterVATId(index).row(companyEnterVATRoute, UpdateMode) ++
            CompanyPayeVariationsId(index).row(companyPayeVariationsRoute, UpdateMode) ++
            CompanyRegistrationNumberVariationsId(index).row(companyRegistrationNumberVariationsRoute, UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), srn)
      }

      "return OK and the correct view for vat and paye if new establisher" in {
        val answers = UserAnswers().set(CompanyVatId(index))(Vat.Yes("098765432")).flatMap(
          _.set(CompanyPayeId(index))(Paye.Yes("12345678"))).flatMap(_.set(IsEstablisherNewId(index))(true)).asOpt.value
        implicit val request: FakeDataRequest = FakeDataRequest(answers)

        val expectedCompanyDetailsSection = estCompanyDetailsSection(
          CompanyVatId(index).row(companyVatRoute(CheckUpdateMode, srn), UpdateMode) ++
            CompanyPayeId(index).row(companyPayeRoute(CheckUpdateMode, srn), UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyContactDetailsSection), srn)
      }
    }

    "rendering submit button_link" must {

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, None, index)(FakeDataRequest(fullAnswers))
      )
    }
  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  private val index = Index(0)
  private val testSchemeName = "Test Scheme Name"
  private val srn = Some("S123")
  private val companyDetails = CompanyDetails("test company")
  private val companyRegNoYes = CompanyRegistrationNumber.Yes("crn")
  private val utrYes = UniqueTaxReference.Yes("utr")
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")
  private val contactDetails = ContactDetails("test@test.com", "1234")

  private def emptyContactDetailsSection =
    AnswerSection(Some("messages__establisher_company_contact_details__title"), Nil)

  private def estCompanyDetailsSection(rows: Seq[AnswerRow]) =
    AnswerSection(Some("messages__common__company_details__title"), rows)

  private val emptyAnswers = UserAnswers()
  private val companyRegistrationNumberRoute = routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, None, 0).url
  private val companyRegistrationNumberVariationsRoute = routes.CompanyRegistrationNumberVariationsController.onPageLoad(CheckUpdateMode, srn, index).url
  private val companyEnterVATRoute = routes.CompanyEnterVATController.onPageLoad(CheckUpdateMode, 0, srn).url

  private def companyVatRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.CompanyVatController.onPageLoad(mode, 0, srn).url

  private val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, 0).url
  private val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, None, 0).url
  private val isCompanyDormantRoute = routes.IsCompanyDormantController.onPageLoad(CheckMode, None, 0).url
  private val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyContactDetailsRoute = routes.CompanyContactDetailsController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyPayeVariationsRoute = routes.CompanyPayeVariationsController.onPageLoad(CheckUpdateMode, 0, srn).url

  private def companyPayeRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.CompanyPayeController.onPageLoad(mode, 0, srn).url

  private val fullAnswers = emptyAnswers.
    establisherCompanyDetails(0, companyDetails).
    establisherCompanyRegistrationNumber(0, companyRegNoYes).
    establisherUniqueTaxReference(0, utrYes).
    establisherVat(0, Vat.Yes("123456789")).
    establisherPaye(0, Paye.Yes("12345678")).
    establisherCompanyDormant(0, DeclarationDormant.Yes).
    establishersCompanyAddress(0, address).
    establisherCompanyAddressYears(0, addressYears).
    establishersCompanyPreviousAddress(0, previousAddress).
    establishersCompanyContactDetails(0, contactDetails)

  private def companyDetailsSection(implicit request: DataRequest[AnyContent]): AnswerSection = {
    val companyDetailsRow = CompanyDetailsCYA()().row(CompanyDetailsId(index))(companyDetailsRoute, request.userAnswers)

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId](
      label = "messages__company__cya__crn_yes_no",
      changeHasCrn = "messages__visuallyhidden__establisher__crn_yes_no",
      changeCrn = "messages__visuallyhidden__establisher__crn",
      changeNoCrn = "messages__visuallyhidden__establisher__crn_no"
    )().row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute, request.userAnswers)

    val payeRows = PayeCYA[CompanyPayeId](
      Some("messages__company__cya__paye_yes_no"), "messages__visuallyhidden__establisher__paye_yes_no",
      "messages__visuallyhidden__establisher__paye_number"
    )().row(CompanyPayeId(index))(companyPayeRoute(), request.userAnswers)

    val vatRows = VatCYA(Some("messages__company__cya__vat_yes_no"),
      "messages__visuallyhidden__establisher__vat_yes_no",
      "messages__visuallyhidden__establisher__vat_number")().
      row(CompanyVatId(index))(companyVatRoute(), request.userAnswers)

    val utrRows = UniqueTaxReferenceCYA(
      label = "messages__company__cya__utr_yes_no",
      utrLabel = "messages__cya__utr",
      reasonLabel = "messages__company__cya__utr_no_reason"
    )().row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val isDormantRows = IsDormantCYA(
      label = "messages__company__cya__dormant",
      changeIsDormant = messages("messages__visuallyhidden__dynamic_company__dormant", companyDetails.companyName)
    )().row(IsCompanyDormantId(index))(isCompanyDormantRoute, request.userAnswers)

    AnswerSection(
      Some("messages__common__company_details__title"),
      companyDetailsRow ++ vatRows ++ payeRows ++ crnRows ++ utrRows ++ isDormantRows)
  }

  private def companyContactDetailsSection(implicit request: DataRequest[AnyContent]): AnswerSection = {

    val addressRows = AddressCYA(
      label = Message("messages__establisherConfirmAddress__cya_label", companyDetails.companyName),
      changeAddress = messages("messages__visuallyhidden__dynamic_address", companyDetails.companyName))().row(
      CompanyAddressId(index))(companyAddressRoute, request.userAnswers)

    val addressYearsRows = AddressYearsCYA(label = Message("messages__company_address_years__h1", companyDetails.companyName),
      changeAddressYears = messages("messages__visuallyhidden__dynamic_addressYears", companyDetails.companyName))().row(CompanyAddressYearsId(index))(
      companyAddressYearsRoute, request.userAnswers
    )

    val previousAddressRows = AddressCYA(
      label = Message("messages__establisherPreviousConfirmAddress__cya_label", companyDetails.companyName),
      changeAddress = messages("messages__visuallyhidden__dynamic_previousAddress", companyDetails.companyName)
    )().row(CompanyPreviousAddressId(index))(companyPreviousAddressRoute, request.userAnswers)

    val contactDetailsRows = ContactDetailsCYA(
      changeEmailAddress = "messages__visuallyhidden__establisher__email_address",
      changePhoneNumber = "messages__visuallyhidden__establisher__phone_number"
    )().row(CompanyContactDetailsId(index))(companyContactDetailsRoute, request.userAnswers)

    AnswerSection(
      Some("messages__establisher_company_contact_details__title"),
      addressRows ++ addressYearsRows ++ previousAddressRows ++ contactDetailsRows)
  }

  private def answerSections(implicit request: DataRequest[AnyContent]) = Seq(companyDetailsSection, companyContactDetailsSection)

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      new FakeNavigator(onwardRoute),
      FakeUserAnswersService,
      allowChangeHelper
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None): String =
    checkYourAnswers(
      frontendAppConfig,
      answerSections,
      IndexController.onPageLoad(),
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false
    )(fakeRequest, messages).toString

}



