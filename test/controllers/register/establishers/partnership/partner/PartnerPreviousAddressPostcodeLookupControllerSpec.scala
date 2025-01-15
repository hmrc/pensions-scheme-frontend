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

package controllers.register.establishers.partnership.partner

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import forms.address.PostCodeLookupFormProvider
import models.address.TolerantAddress
import models.person.PersonName
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnerPreviousAddressPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = routes.PartnerPreviousAddressPostcodeLookupController.onSubmit(NormalMode, establisherIndex, partnerIndex, EmptyOptionalSchemeReferenceNumber)

  def manualInputCall: Call = routes.PartnerPreviousAddressController.onPageLoad(NormalMode, establisherIndex, partnerIndex, EmptyOptionalSchemeReferenceNumber)

  private val formProvider = new PostCodeLookupFormProvider()

  private val establisherIndex = Index(0)
  private val partnerIndex = Index(0)

  private val partner = PersonName("first", "last")

  private val form = formProvider()
  lazy val viewmodel = PostcodeLookupViewModel(
    onwardRoute,
    manualInputCall,
    Message("messages__partnerPreviousAddressPostcodeLookup__heading", Message("messages__thePartner")),
    Message("messages__partnerPreviousAddressPostcodeLookup__heading", partner.fullName),
    Some(partner.fullName)
  )
  private val addressLookupConnector = mock[AddressLookupConnector]
  private val address = TolerantAddress(Some("value 1"), Some("value 2"), None, None, Some("AB1 1AB"), Some("GB"))
  private val view = injector.instanceOf[postcodeLookup]

  "PartnerPreviousAddressPostcodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      running(_.overrides(modules(getMandatoryPartner): _*)) {
        app =>
          val controller = app.injector.instanceOf[PartnerPreviousAddressPostcodeLookupController]
          val result = controller.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, EmptyOptionalSchemeReferenceNumber)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewmodel, None)(fakeRequest, messages).toString
      }
    }

    "redirect to the next page on POST request" in {
      val validPostcode = "ZZ1 1ZZ"
      running(_.overrides(modules(getMandatoryPartner) ++
        Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AddressLookupConnector].toInstance(addressLookupConnector)
        ): _*)) {
        app =>
          when(addressLookupConnector.addressLookupByPostCode(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Seq(address)))
          val controller = app.injector.instanceOf[PartnerPreviousAddressPostcodeLookupController]
          val postRequest = fakeRequest.withFormUrlEncodedBody("postcode" -> validPostcode)
          val result = controller.onSubmit(NormalMode, establisherIndex = 0, partnerIndex = 0, EmptyOptionalSchemeReferenceNumber)(postRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}
