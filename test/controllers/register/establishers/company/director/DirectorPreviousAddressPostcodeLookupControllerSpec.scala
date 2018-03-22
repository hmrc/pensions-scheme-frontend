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

package controllers.register.establishers.company.director

import connectors.{AddressLookupConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.{Index, NormalMode}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.establishers.company.director.directorPreviousAddressPostcodeLookup

class DirectorPreviousAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new PostCodeLookupFormProvider()
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val form = formProvider()
  val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new DirectorPreviousAddressPostcodeLookupController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      fakeAddressLookupConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider)

  def viewAsString(form: Form[_] = form) = directorPreviousAddressPostcodeLookup(frontendAppConfig, form, NormalMode)(fakeRequest, messages).toString

  val testAnswer = "answer"

  "DirectorPreviousAddressPostcodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, establisherIndex, directorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode, establisherIndex, directorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
