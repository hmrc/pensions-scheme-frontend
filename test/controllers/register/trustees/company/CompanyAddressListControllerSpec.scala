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
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPostcodeLookupId}
import models.address.TolerantAddress
import models.{CompanyDetails, EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.UserAnswers
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class CompanyAddressListControllerSpec extends ControllerSpecBase {

  private val companyDetails = CompanyDetails("Test company name")

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
      .flatMap(_.set(CompanyPostcodeLookupId(0))(addresses))
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  private val view = injector.instanceOf[addressList]

  "Company Director Previous Address List Controller" must {

    "return Ok and the correct view on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction)
      )) { implicit app =>
        val request = addCSRFToken(FakeRequest(routes.CompanyAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
        val result = route(app, request).value

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
        bind[DataRetrievalAction].toInstance(getEmptyData)
      )) { implicit app =>
        val request = addCSRFToken(FakeRequest(routes.CompanyAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CompanyPostCodeLookupController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData)
      )) { implicit app =>
        val request = addCSRFToken(FakeRequest(routes.CompanyAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }

    }

    "redirect to the next page on POST of valid data" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dataRetrievalAction)
      )) { implicit app =>
        val request =
          addCSRFToken(
            FakeRequest()
              .withFormUrlEncodedBody(("value", "0"))
          )

        val controller = app.injector.instanceOf[CompanyAddressListController]
        val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, 0, EmptyOptionalSchemeReferenceNumber).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {

      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData)
      )) { implicit app =>
        val request =
          addCSRFToken(
            FakeRequest()
              .withFormUrlEncodedBody(("value", "0"))
          )

        val controller = app.injector.instanceOf[CompanyAddressListController]
        val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

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
          addCSRFToken(
            FakeRequest()
              .withFormUrlEncodedBody(("value", "0"))
          )

        val controller = app.injector.instanceOf[CompanyAddressListController]
        val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CompanyPostCodeLookupController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber).url)
      }

    }

  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.CompanyAddressListController.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber),
      routes.CompanyAddressController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber),
      addresses,
      title = Message("messages__dynamic_whatIsAddress", Message("messages__theCompany")),
      heading = Message("messages__dynamic_whatIsAddress", companyDetails.companyName),
      entityName = companyDetails.companyName
    )
  }

}




