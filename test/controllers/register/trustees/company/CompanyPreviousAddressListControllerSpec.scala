/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPreviousAddressPostcodeLookupId}
import models.address.TolerantAddress
import models.{CompanyDetails, Index, NormalMode}
import play.api.inject.bind
import play.api.libs.json._
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.UserAnswers
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class CompanyPreviousAddressListControllerSpec extends ControllerSpecBase {


  private val companyDetails = CompanyDetails("Test company name")
  private val view = injector.instanceOf[addressList]

  private val previousAddressTitle = s"What was the company’s previous address?"
  private val previousAddressHeading = s"What was ${companyDetails.companyName}’s previous address?"


  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  private val data =
    UserAnswers(Json.obj())
      .set(CompanyDetailsId(0))(companyDetails)
      .flatMap(_.set(CompanyPreviousAddressPostcodeLookupId(0))(addresses))
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  "Company Previous Address List Controller" must {

    "return Ok and the correct view on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn)),

      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onPageLoad(NormalMode, Index(0), srn)(request)

        status(result) mustBe OK

        val viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form = new AddressListFormProvider()(viewModel.addresses)

        contentAsString(result).removeAllNonces() mustBe view(form, viewModel, None)(request, messages).toString
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(getEmptyData),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn)),

      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onPageLoad(NormalMode, Index(0), srn)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, Index(0), srn).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn)),

      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onPageLoad(NormalMode, Index(0), srn)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }

    }

    "redirect to the next page on POST of valid data" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn)),

      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, Index(0), srn)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          Some(routes.CheckYourAnswersCompanyAddressController.onPageLoad(NormalMode, Index(0), srn).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData)
      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, Index(0), srn)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a POST request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(getEmptyData)
      )) { implicit app =>
        val request =
          addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
        val controller = app.injector.instanceOf[CompanyPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, Index(0), srn)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, Index(0), srn).url)
      }

    }

  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      postCall = routes.CompanyPreviousAddressListController.onSubmit(NormalMode, Index(0), srn),
      manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(NormalMode, Index(0), srn),
      addresses = addresses,
      title = previousAddressTitle,
      heading = previousAddressHeading,
      selectAddress = Message("messages__common__select_address"),
      entityName = companyDetails.companyName,
      srn = srn
    )
  }
}
