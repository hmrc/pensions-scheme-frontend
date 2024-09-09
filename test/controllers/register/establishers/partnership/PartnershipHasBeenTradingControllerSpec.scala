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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.HasBeenTradingFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership._
import models.address.{Address, TolerantAddress}
import models.{Index, NormalMode, PartnershipDetails}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class PartnershipHasBeenTradingControllerSpec extends ControllerSpecBase {
  private val schemeName = None
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val formProvider = new HasBeenTradingFormProvider()
  private val form = formProvider("messages__tradingAtLeastOneYear__error","test partnership name")
  private val index = Index(0)

  val viewModel = CommonFormWithHintViewModel(
    controllers.register.establishers.partnership.routes.PartnershipHasBeenTradingController.onSubmit(NormalMode, index, srn),
    title = Message("messages__partnership_trading_time__title"),
    heading = Message("messages__hasBeenTrading__h1", "test partnership name"),
    hint = None,
    srn = srn
  )
  val tolerantAddress = TolerantAddress(None, None, None, None, None, None)
  val address = Address("line 1", "line 2", None, None, None, "GB")

  private def getEstablisherPartnershipDataWithPreviousAddress(hasBeenTrading: Boolean): FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name"),
          PartnershipHasBeenTradingId.toString -> hasBeenTrading,
          PartnershipPreviousAddressPostcodeLookupId.toString -> Seq(tolerantAddress),
          PartnershipPreviousAddressId.toString -> address,
          PartnershipPreviousAddressListId.toString -> tolerantAddress
        )
      )
    ))
  )

  private val view = injector.instanceOf[hasReferenceNumber]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherPartnership): PartnershipHasBeenTradingController =
    new PartnershipHasBeenTradingController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(srn),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view,
      global
    )

  private def viewAsString(form: Form[_] = form): String = view(form, viewModel, schemeName)(fakeRequest, messages).toString

  "PartnershipHasBeenTradingController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index, srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted for true" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode, index, srn)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersService.verify(PartnershipHasBeenTradingId(index), value = true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index, srn)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "clean up previous address, if user changes answer from yes to no" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller(getEstablisherPartnershipDataWithPreviousAddress(hasBeenTrading = true)).onSubmit(NormalMode, index, srn)(postRequest)

      status(result) mustBe SEE_OTHER

      FakeUserAnswersService.userAnswer.get(PartnershipHasBeenTradingId(index)).value mustEqual false
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressPostcodeLookupId(index)) mustBe None
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressId(index)) mustBe None
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressListId(index)) mustBe None
    }

    "not clean up for previous address, if user changes answer from no to yes" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(getEstablisherPartnershipDataWithPreviousAddress(hasBeenTrading = false)).onSubmit(NormalMode, index, srn)(postRequest)

      status(result) mustBe SEE_OTHER

      FakeUserAnswersService.userAnswer.get(PartnershipHasBeenTradingId(index)).value mustEqual true
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressPostcodeLookupId(index)) mustBe Some(Seq(tolerantAddress))
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressId(index)) mustBe Some(address)
      FakeUserAnswersService.userAnswer.get(PartnershipPreviousAddressListId(index)) mustBe Some(tolerantAddress)
    }
  }
}

