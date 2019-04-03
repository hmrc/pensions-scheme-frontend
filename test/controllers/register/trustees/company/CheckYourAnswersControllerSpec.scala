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
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.trustees.IsTrusteeCompleteId
import identifiers.register.trustees.company._
import models._
import models.address.Address
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import utils._
import utils.checkyouranswers.Ops._
import utils.checkyouranswers.{AddressYearsCYA, CompanyRegistrationNumberCYA, UniqueTaxReferenceCYA}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET with full answers" in {
      val request = FakeDataRequest(fullAnswers)
      val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections(request))
    }

    "return 200 and the correct view for a GET with empty answers" in {
      val request = FakeDataRequest(emptyAnswers)
      val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections(request))
    }

    "redirect to next page" when {
      "POST is called" in {
        val result = controller().onSubmit(NormalMode, index, None)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "mark trustee company as complete" in {
        val result = controller().onSubmit(NormalMode, index, None)(fakeRequest)
        status(result) mustBe SEE_OTHER
        FakeSectionComplete.verify(IsTrusteeCompleteId(index), true)
      }
    }

  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

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

  private lazy val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, index, None).url
  private lazy val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, index, None).url
  private lazy val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index, None).url
  private lazy val companyRegistrationNumberRoute = routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, index, None).url
  private lazy val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, index, None).url

  private lazy val postUrl = routes.CheckYourAnswersController.onSubmit(NormalMode, index, None)

  private def answerSections(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId](
      label = "messages__checkYourAnswers__trustees__company__crn",
      changeHasCrn = "messages__visuallyhidden__trustee__crn_yes_no",
      changeCrn = "messages__visuallyhidden__trustee__crn",
      changeNoCrn = "messages__visuallyhidden__trustee__crn_no"
    )().row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute, request.userAnswers)

    val utrRows = UniqueTaxReferenceCYA[CompanyUniqueTaxReferenceId](
      label = "messages__checkYourAnswers__trustees__company__utr",
      changeHasUtr = "messages__visuallyhidden__trustee__utr_yes_no",
      changeUtr = "messages__visuallyhidden__trustee__utr",
      changeNoUtr = "messages__visuallyhidden__trustee__utr_no"
    )().row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val companyDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__company_details"),
      CompanyDetailsId(index).row(companyDetailsRoute) ++
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

    Seq(
      companyDetailsSection,
      contactDetailsSection
    )

  }

  private def viewAsString(answerSections: Seq[AnswerSection]) = check_your_answers(
    frontendAppConfig,
    answerSections,
    postUrl,
    None
  )(fakeRequest, messages).toString

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new CheckYourAnswersFactory(fakeCountryOptions),
      fakeCountryOptions,
      new FakeNavigator(onwardRoute),
      FakeSectionComplete
    )

}
