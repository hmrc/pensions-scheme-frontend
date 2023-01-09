/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.address.TolerantAddress
import models.{Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnershipPostcodeLookupControllerSpec extends ControllerSpecBase with ScalaFutures {

  import PartnershipPostcodeLookupControllerSpec._

  "PartnershipPostCodeLookup Controller" must {
    "render postCodeLookup from a GET request" in {
      running(_.overrides(modules(retrieval): _*)) {
        app =>
          val controller = app.injector.instanceOf[PartnershipPostcodeLookupController]
          val result = controller.onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(fakeRequest, messages).toString
      }
    }

    "redirect to next page on POST request" which {
      "returns a list of addresses from addressLookup given a postcode" in {
        val validPostcode = "ZZ1 1ZZ"
        running(_.overrides(modules(retrieval) ++
          Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(FakeUserAnswersService),
            bind[AddressLookupConnector].toInstance(addressLookupConnector)
          ): _*)) {
          app =>
            when(addressLookupConnector.addressLookupByPostCode(ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(Seq(address)))
            val controller = app.injector.instanceOf[PartnershipPostcodeLookupController]
            val postRequest = fakeRequest.withFormUrlEncodedBody("postcode" -> validPostcode)
            val result = controller.onSubmit(NormalMode, firstIndex, None)(postRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }
    }
  }
}

object PartnershipPostcodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val firstIndex = Index(0)
  val formProvider = new PostCodeLookupFormProvider()
  val form = formProvider()
  val partnershipDetails = PartnershipDetails("test partnership name")
  val validPostcode = "ZZ1 1ZZ"
  lazy val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  val address = TolerantAddress(Some("address line 1"), Some("address line 2"), None, None, Some(validPostcode), Some("GB"))
  private val addressLookupConnector = mock[AddressLookupConnector]
  private val view = injector.instanceOf[postcodeLookup]
  lazy val viewModel = PostcodeLookupViewModel(
    postCall = routes.PartnershipPostcodeLookupController.onSubmit(NormalMode, firstIndex, None),
    manualInputCall = routes.PartnershipAddressController.onPageLoad(NormalMode, firstIndex, None),
    title = Message("messages__partnershipPostcodeLookup__heading", Message("messages__thePartnership").resolve),
    heading = Message("messages__partnershipPostcodeLookup__heading", partnershipDetails.name),
    subHeading = Some(partnershipDetails.name)
  )

  val retrieval = new FakeDataRetrievalAction(Some(
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> partnershipDetails
        )
      )
    )
  ))
}
