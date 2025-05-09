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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.address.AddressListFormProvider
import identifiers.register.trustees.individual.*
import models.address.TolerantAddress
import models.person.PersonName
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, person}
import navigators.Navigator
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, route, running, status, *}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswerOps, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class IndividualAddressListControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val view = injector.instanceOf[addressList]
  val trusteeDetails = person.PersonName("Test", "Name")
  val trusteeName = PersonName("Test", "Name")

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

  private def retrieval: DataRetrievalAction = {
    UserAnswers(Json.obj())
      .set(TrusteeNameId(0))(trusteeName)
      .flatMap(_.set(IndividualPostCodeLookupId(0))(addresses))
      .asOpt
      .value
      .dataRetrievalAction
  }

  lazy val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  "Individual Address List Controller" must {
    s"return Ok and the correct view on a Get Request" in {
      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].toInstance(retrieval),
          bind(classOf[Navigator]).toInstance(fakeNavigator)
        )) { implicit app =>
        val request = addCSRFToken(FakeRequest(routes.IndividualAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
        val result = route(app, request).value

        status(result) mustBe OK

        val viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form = new AddressListFormProvider()(viewModel.addresses)

        contentAsString(result).removeAllNonces() mustBe view(form, viewModel, None)(request, messages).toString
      }
    }

    s"redirect to the next page on POST of valid data" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind(classOf[Navigator]).toInstance(fakeNavigator),
          bind[DataRetrievalAction].toInstance(retrieval)
        )) { implicit app =>
              val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
              val controller = app.injector.instanceOf[IndividualAddressListController]
              val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }

  "redirect to Individual Post Code Lookup if no address data on a GET request" in {

    running(
      _.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(getEmptyData),
        bind(classOf[Navigator]).toInstance(fakeNavigator)
      )) { implicit app =>
      val request = addCSRFToken(FakeRequest(routes.IndividualAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
      val result = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.IndividualPostCodeLookupController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber).url)
    }

  }

  "redirect to Session Expired controller when no session data exists on a GET request" in {

    running(
      _.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData),
        bind(classOf[Navigator]).toInstance(fakeNavigator)
      )) { implicit app =>
      val request = addCSRFToken(FakeRequest(routes.IndividualAddressListController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)))
      val result = route(app, request).value

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

  "redirect to Session Expired controller when no session data exists on a POST request" in {

    running(
      _.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(dontGetAnyData),
        bind(classOf[Navigator]).toInstance(fakeNavigator)
      )) { implicit app =>
      val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
      val controller = app.injector.instanceOf[IndividualAddressListController]
      val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }

  "redirect to Individual Address Post Code Lookup if no address data on a POST request" in {

    running(
      _.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[DataRetrievalAction].toInstance(getEmptyData),
        bind(classOf[Navigator]).toInstance(fakeNavigator)
      )) { implicit app =>
      val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
      val controller = app.injector.instanceOf[IndividualAddressListController]
      val result = controller.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.IndividualPostCodeLookupController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber).url)
    }

  }


  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      postCall = routes.IndividualAddressListController.onSubmit( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber),
      manualInputCall = routes.TrusteeAddressController.onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber),
      addresses = addresses,
      title = messages("messages__trustee__individual__address__heading",  Message("messages__theIndividual").resolve),
      heading = messages("messages__trustee__individual__address__heading", trusteeName.fullName),
      entityName = trusteeDetails.fullName
    )
  }
}
