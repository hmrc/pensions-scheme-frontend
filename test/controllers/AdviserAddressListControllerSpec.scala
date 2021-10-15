/*
 * Copyright 2021 HM Revenue & Customs
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

import audit.{AddressAction, AddressEvent, AuditService}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.AddressListFormProvider
import identifiers.{AdviserAddressPostCodeLookupId, AdviserNameId}
import models.NormalMode
import models.address.{Address, TolerantAddress}
import navigators.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import org.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class AdviserAddressListControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with BeforeAndAfterEach {

  import AdviserAddressListControllerSpec._

  private val view = injector.instanceOf[addressList]
  private val auditService: AuditService = mock[AuditService]

  override def beforeEach(): Unit = {
    reset(
      auditService
    )
  }

  "Adviser Address List Controller" must {

    "return OK and the correct view on a GET" in {
      val viewModel: AddressListViewModel = addressListViewModel(addresses)
      val form = new AddressListFormProvider()(viewModel.addresses)

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(retrievalAction),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onPageLoad(NormalMode)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(request, messages).toString()
      }
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a GET request" in {
      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(getEmptyData),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onPageLoad(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode).url)
      }
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(dontGetAnyData),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onPageLoad(NormalMode)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to the next page on POST of valid data" in {

      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(retrievalAction),
          bind[AuditService].to(auditService),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val fakeRequest = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onSubmit(NormalMode)(fakeRequest)
          val argCaptor = ArgumentCaptor.forClass(classOf[AddressEvent])

          val auditEvent = AddressEvent(
            externalId = "id",
            action = AddressAction.Lookup,
            context = "Adviser Address: the Adviser",
            address = Address(
              addressLine1 = "Address 1 Line 1",
              addressLine2 = "Address 1 Line 2",
              addressLine3 = Some("Address 1 Line 3"),
              addressLine4 = Some("Address 1 Line 4"),
              postcode = Some("A1 1PC"), country = "GB")
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          verify(auditService, times(1)).sendEvent(argCaptor.capture())(any(), any())
          argCaptor.getValue mustBe auditEvent
      }
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(dontGetAnyData),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val fakeRequest = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onSubmit(NormalMode)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a POST request" in {
      running(
        _.overrides(
          bind[AuthAction].to(FakeAuthAction),
          bind[UserAnswersService].to(FakeUserAnswersService),
          bind[DataRetrievalAction].to(getEmptyData),
          bind(classOf[Navigator]).to(new FakeNavigator(onwardRoute))
        )
      ) {
        implicit app =>
          val fakeRequest = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("value", "0")))
          val controller = app.injector.instanceOf[AdviserAddressListController]
          val result = controller.onSubmit(NormalMode)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserPostCodeLookupController.onPageLoad(NormalMode).url)
      }
    }
  }
}

object AdviserAddressListControllerSpec {

  lazy val onwardRoute: Call = controllers.routes.AdviserCheckYourAnswersController.onPageLoad()
  private val adviserName = "the Adviser"
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
    UserAnswers(Json.obj(AdviserNameId.toString -> adviserName))
      .set(AdviserAddressPostCodeLookupId)(addresses)
      .asOpt.map(_.json)

  val retrievalAction = new FakeDataRetrievalAction(data)

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.AdviserAddressListController.onSubmit(NormalMode),
      routes.AdviserAddressController.onPageLoad(NormalMode),
      addresses,
      heading = Message("messages__dynamic_whatIsAddress", adviserName),
      title = Message("messages__dynamic_whatIsAddress", Message("messages__theAdviser")),
      entityName = adviserName
    )
  }
}

