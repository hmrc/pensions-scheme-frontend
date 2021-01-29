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

package controllers.register.establishers.individual

import audit.testdoubles.StubSuccessfulAuditService
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.establishers.individual._
import models.address.TolerantAddress
import models.{Index, NormalMode, person}
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}

import utils.{Enumerable, FakeNavigator, MapFormats}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class AddressListControllerSpec extends ControllerSpecBase with Enumerable.Implicits with MapFormats with MockitoSugar {
  val fakeAuditService = new StubSuccessfulAuditService()
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressListFormProvider()
  val form = formProvider(Seq.empty)
  val firstIndex = Index(0)

  val establisherName: String = "test first name test last name"
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )
  val validDataNoAddresses: JsObject = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherNameId.toString ->
          person.PersonName("test first name", "test last name", false),
        EstablisherHasUTRId.toString -> true,
        EstablisherUTRId.toString -> "1234567891"
    )))

  private val view = injector.instanceOf[addressList]

  def controller(
                  dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher,
                  dataCacheConnector: UserAnswersService = FakeUserAnswersService
                ): AddressListController =
    new AddressListController(
      frontendAppConfig, messagesApi,
      dataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeAuditService,
      view,
      controllerComponents
    )

  def viewAsString(form: Form[_] = form, address: Seq[TolerantAddress] = addresses): String =
    view(
      form,
      AddressListViewModel(
        postCall = routes.AddressListController.onSubmit(NormalMode, firstIndex, None),
        manualInputCall = routes.AddressController.onPageLoad(NormalMode, firstIndex, None),
        addresses = addresses,
        heading = Message("messages__dynamic_whatIsAddress", establisherName),
        title = Message("messages__dynamic_whatIsAddress", Message("messages__theIndividual")),
        entityName = establisherName
      ),
      None
    )(fakeRequest, messages).toString

  def address(postCode: String): TolerantAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some(postCode),
    Some("United Kingdom"))

  val validData: JsObject = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherNameId.toString ->
          person.PersonName("test first name", "test last name", false),
        EstablisherHasUTRId.toString -> true,
        EstablisherUTRId.toString -> "1234567891",
        PostCodeLookupId.toString -> addresses)
    ))

  "AddressResults Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val result = controller(new FakeDataRetrievalAction(Some(validData))).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(address = Seq(address("test post code 1"), address("test post code 2")))
    }

    "redirect to Address look up page when no addresses are present after lookup" in {
      val result = controller(new FakeDataRetrievalAction(Some(validDataNoAddresses))).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(NormalMode, firstIndex, None).url)
    }

    "redirect to Address look up page when no addresses are present after lookup (post)" in {
      val result = controller().onSubmit(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(
        controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(NormalMode, firstIndex, None).url)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))

      val result = controller(new FakeDataRetrievalAction(Some(validData))).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "update the country of the chosen address to `GB`" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "0")

      val result = controller(new FakeDataRetrievalAction(Some(validData)), FakeUserAnswersService)
        .onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustEqual SEE_OTHER
      FakeUserAnswersService.userAnswer.get(AddressListId(firstIndex)).value mustEqual(addresses.head.copy(country = Some("GB")))
    }

    "return a Bad Request and errors when no data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller(new FakeDataRetrievalAction(Some(validData))).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
