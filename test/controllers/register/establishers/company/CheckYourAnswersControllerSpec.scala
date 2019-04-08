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
import identifiers.register.establishers.company._
import models._
import models.address.Address
import models.register.DeclarationDormant
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import utils.checkyouranswers.CheckYourAnswers.ContactDetailsCYA
import utils.checkyouranswers._
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, FakeSectionComplete, UserAnswers, _}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET with all the answers" in {
      val request = FakeDataRequest(fullAnswers)
      val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections(request))
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "mark company as complete on submit" in {
      val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
      FakeSectionComplete.verify(IsCompanyCompleteId(index), true)
    }
  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with Enumerable.Implicits {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index = Index(0)
  val testSchemeName = "Test Scheme Name"
  private val companyDetails = CompanyDetails("test company")
  private val companyRegNoYes = CompanyRegistrationNumber.Yes("crn")
  private val utrYes = UniqueTaxReference.Yes("utr")
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")
  private val contactDetails = ContactDetails("test@test.com", "1234")

  private val emptyAnswers = UserAnswers()
  private val companyRegistrationNumberRoute = routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, None, 0).url
  private val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, None, 0).url
  private val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, None, 0).url
  private val isCompanyDormantRoute = routes.IsCompanyDormantController.onPageLoad(CheckMode, None, 0).url
  private val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, None, Index(index)).url
  private val companyContactDetailsRoute = routes.CompanyContactDetailsController.onPageLoad(CheckMode, None, Index(index)).url

  private val fullAnswers = emptyAnswers.
    establisherCompanyDetails(0, companyDetails).
    establisherCompanyRegistrationNumber(0, companyRegNoYes).
    establisherUniqueTaxReference(0, utrYes).
    establisherCompanyDormant(0, DeclarationDormant.Yes).
    establishersCompanyAddress(0, address).
    establisherCompanyAddressYears(0, addressYears).
    establishersCompanyPreviousAddress(0, previousAddress).
    establishersCompanyContactDetails(0, contactDetails)

  def postUrl: Call = routes.CheckYourAnswersController.onSubmit(NormalMode, None, index)

  private def companyDetailsSection(implicit request: DataRequest[AnyContent]): AnswerSection = {
    val companyDetailsRow = CompanyDetailsCYA(
      nameLabel = "messages__common__cya__name",
      vatLabel = "messages__common__cya__vat",
      changeVat = "messages__visuallyhidden__establisher__vat_number",
      payeLabel = "messages__common__cya__paye",
      changePaye = "messages__visuallyhidden__establisher__paye_number")().row(CompanyDetailsId(index))(companyDetailsRoute, request.userAnswers)

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId](
      label = "messages__company__cya__crn_yes_no",
      changeHasCrn = "messages__visuallyhidden__establisher__crn_yes_no",
      changeCrn = "messages__visuallyhidden__establisher__crn",
      changeNoCrn = "messages__visuallyhidden__establisher__crn_no"
    )().row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute, request.userAnswers)

    val utrRows = UniqueTaxReferenceCYA(
      label = "messages__company__cya__utr_yes_no",
      utrLabel = "messages__company__cya__utr",
      reasonLabel = "messages__company__cya__utr_no_reason",
      changeHasUtr = "messages__visuallyhidden__establisher__utr_yes_no",
      changeUtr = "messages__visuallyhidden__establisher__utr",
      changeNoUtr = "messages__visuallyhidden__establisher__utr_no"
    )().row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val isDormantRows = IsDormantCYA()().row(IsCompanyDormantId(index))(isCompanyDormantRoute, request.userAnswers)

    AnswerSection(
      Some("messages__common__company_details__title"),
      companyDetailsRow ++ crnRows ++ utrRows ++ isDormantRows)
  }

  private def companyContactDetailsSection(implicit request: DataRequest[AnyContent]): AnswerSection = {

    val addressRows = AddressCYA(
      changeAddress = "messages__visuallyhidden__establisher__address")().row(
      CompanyAddressId(index))(companyAddressRoute, request.userAnswers)

    val addressYearsRows = AddressYearsCYA(label = "companyAddressYears.checkYourAnswersLabel",
      changeAddressYears = "messages__visuallyhidden__establisher__address_years")().row(CompanyAddressYearsId(index))(
      companyAddressYearsRoute, request.userAnswers
    )

    val previousAddressRows = AddressCYA(
      label = "messages__common__cya__previous_address",
      changeAddress = "messages__visuallyhidden__establisher__previous_address"
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

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeCountryOptions,
      new FakeNavigator(onwardRoute),
      FakeSectionComplete
    )

  def viewAsString(answerSections: Seq[AnswerSection]): String =
    check_your_answers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None
    )(fakeRequest, messages).toString

}




