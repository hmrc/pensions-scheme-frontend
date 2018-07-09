/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.trustees.company._
import models._
import models.address.Address
import models.register.{SchemeDetails, SchemeType}
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import utils._
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CompanyRegistrationNumberCYA}
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "Check Your Answers Controller" must {
    "return 200 and the correct view for a GET with full answers" in {
      val request = FakeDataRequest(fullAnswers)
      val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(index)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections(request))
    }

    "return 200 and the correct view for a GET with empty answers" in {
      val request = FakeDataRequest(emptyAnswers)
      val result = controller(emptyAnswers.dataRetrievalAction).onPageLoad(index)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(answerSections(request))
    }

    "redirect to Session Expired page" when {
      "GET" when {
        "trustee company name is not present" in {
          val result = controller(getEmptyData).onPageLoad(index)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

    "redirect to next page" when {
      "POST is called" in {
        val result=controller().onSubmit(index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

  }

}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {

  private val index = 0
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private val schemeName = "test-scheme-name"
  private val companyDetails = CompanyDetails("test-company-name", Some("test-vat"), Some("test-paye"))
  private val crn = CompanyRegistrationNumber.Yes("test-crn")
  private val utr = UniqueTaxReference.Yes("test-utr")
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYears = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()
    .schemeDetails(SchemeDetails(schemeName, SchemeType.BodyCorporate))

  private val fullAnswers = emptyAnswers
    .trusteesCompanyDetails(index, companyDetails)
    .trusteesCompanyRegistrationNumber(index, crn)
    .trusteesUniqueTaxReference(index, utr)
    .trusteesCompanyAddress(index, address)
    .trusteesCompanyAddressYears(index, addressYears)
    .trusteesCompanyPreviousAddress(index, previousAddress)

  private lazy val companyAddressRoute = routes.CompanyAddressController.onPageLoad(CheckMode, index).url
  private lazy val companyAddressYearsRoute = routes.CompanyAddressYearsController.onPageLoad(CheckMode, index).url
  private lazy val companyDetailsRoute = routes.CompanyDetailsController.onPageLoad(CheckMode, index).url
  private lazy val companyPreviousAddressRoute = routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index).url
  private lazy val companyRegistrationNumberRoute = routes.CompanyRegistrationNumberController.onPageLoad(CheckMode, index).url
  private lazy val companyUniqueTaxReferenceRoute = routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode, index).url

  private lazy val postUrl = routes.CheckYourAnswersController.onSubmit(index)

  private def answerSections(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {

    val crnRows = CompanyRegistrationNumberCYA[CompanyRegistrationNumberId]("messages__checkYourAnswers__trustees__company__crn")
      .companyRegistrationNumber
      .row(CompanyRegistrationNumberId(index))(companyRegistrationNumberRoute, request.userAnswers)

    val utrRows = CheckYourAnswers
      .uniqueTaxReference("messages__checkYourAnswers__trustees__company__utr")
      .row(CompanyUniqueTaxReferenceId(index))(companyUniqueTaxReferenceRoute, request.userAnswers)

    val companyDetailsSection = AnswerSection(
      Some("messages__checkYourAnswers__section__company_details"),
      CompanyDetailsId(index).row(companyDetailsRoute) ++
      crnRows ++
      utrRows
    )

    val addressYearsRows = AddressYearsCYA[CompanyAddressYearsId]("messages__checkYourAnswers__trustees__company__address_years")
      .addressYears
      .row(CompanyAddressYearsId(index))(companyAddressYearsRoute, request.userAnswers)

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
    Some(messages("messages__common__trustee_secondary_header", schemeName)),
    postUrl
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
      new FakeNavigator(onwardRoute)
    )

}
