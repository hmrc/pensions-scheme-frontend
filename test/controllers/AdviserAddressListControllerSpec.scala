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

package controllers

import base.CSRFRequest
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.AddressListFormProvider
import identifiers.AdviserAddressPostCodeLookupId
import models.NormalMode
import models.address.TolerantAddress
import navigators.Navigator
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.Adviser
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class AdviserAddressListControllerSpec extends ControllerSpecBase with CSRFRequest {

  import AdviserAddressListControllerSpec._

  "Adviser Address List Controller" must {

    "return OK and the correct view on a GET" in {
      val viewModel: AddressListViewModel = addressListViewModel(addresses)
      val form = new AddressListFormProvider()(viewModel.addresses)

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))),
        retrievalAction,
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe addressList(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))),
        getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to the next page on POST of valid data" in {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), retrievalAction,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }
  }
}

object AdviserAddressListControllerSpec extends ControllerSpecBase {

  lazy val onwardRoute: Call = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()

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
      .set(AdviserAddressPostCodeLookupId)(addresses)
      .asOpt.map(_.json)

  val retrievalAction = new FakeDataRetrievalAction(data)

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.AdviserAddressListController.onSubmit(NormalMode),
      routes.AdviserAddressController.onPageLoad(NormalMode),
      addresses
    )
  }

  private def requestResult[T](request: Application => Request[T], data: DataRetrievalAction,
                               test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(
      _.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[UserAnswersService].to(FakeUserAnswersService),
        bind[DataRetrievalAction].to(data),
        bind(classOf[Navigator]).qualifiedWith(classOf[Adviser]).to(new FakeNavigator(onwardRoute))
      )
    ) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}

