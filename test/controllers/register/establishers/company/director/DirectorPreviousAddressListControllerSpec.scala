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

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.AddressListFormProvider
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorPreviousAddressPostcodeLookupId}
import models.address.TolerantAddress
import models.person.PersonName
import models.{Index, NormalMode}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.UserAnswers
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class DirectorPreviousAddressListControllerSpec extends ControllerSpecBase with CSRFRequest {

  private val directorDetails = PersonName("Joe", "Bloggs")

  private val previousAddressTitle   = "What was the director’s previous address?"
  private val previousAddressHeading = s"What was ${directorDetails.fullName}’s previous address?"

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
      .set(DirectorNameId(0, 0))(directorDetails)
      .flatMap(_.set(DirectorPreviousAddressPostcodeLookupId(0, 0))(addresses))
      .asOpt
      .map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  "Company Director Previous Address List Controller" must {

    "return Ok and the correct view on a GET request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(dataRetrievalAction)
        )) { implicit app =>
        val request = addToken(FakeRequest(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, Index(0), Index(0), None)))
        val result  = route(app, request).value

        status(result) mustBe OK

        val viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form                            = new AddressListFormProvider()(viewModel.addresses)

        contentAsString(result) mustBe addressList(frontendAppConfig, form, viewModel, None)(request, messages).toString
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a GET request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(getEmptyData)
        )) { implicit app =>
        val request = addToken(FakeRequest(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, Index(0), Index(0), None)))
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, Index(0), Index(0), None).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(dontGetAnyData)
        )) { implicit app =>
        val request = addToken(FakeRequest(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, Index(0), Index(0), None)))
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

    }

    "redirect to the next page on POST of valid data" ignore {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(dataRetrievalAction)
        )) { implicit app =>
        val request =
          addToken(
            FakeRequest(routes.DirectorPreviousAddressListController.onSubmit(NormalMode, Index(0), Index(0), None))
              .withFormUrlEncodedBody(("value", "0"))
          )

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          controllers.register.establishers.company.director.routes.DirectorPreviousAddressController.onPageLoad(NormalMode, 0, 0, None).url)
      }

    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(dontGetAnyData)
        )) { implicit app =>
        val request =
          addToken(
            FakeRequest(routes.DirectorPreviousAddressListController.onSubmit(NormalMode, Index(0), Index(0), None))
              .withFormUrlEncodedBody(("value", "0"))
          )

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }

    }

    "redirect to Company Address Post Code Lookup if no address data on a POST request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(getEmptyData)
        )) { implicit app =>
        val request =
          addToken(
            FakeRequest(routes.DirectorPreviousAddressListController.onSubmit(NormalMode, Index(0), Index(0), None))
              .withFormUrlEncodedBody(("value", "0"))
          )

        val result = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, Index(0), Index(0), None).url)
      }

    }

  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.DirectorPreviousAddressListController.onSubmit(NormalMode, Index(0), Index(0), None),
      routes.DirectorPreviousAddressController.onPageLoad(NormalMode, Index(0), Index(0), None),
      addresses,
      title = previousAddressTitle,
      heading = previousAddressHeading
    )
  }

}
