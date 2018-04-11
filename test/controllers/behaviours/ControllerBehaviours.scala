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

package controllers.behaviours

import base.CSRFRequest
import controllers.ControllerSpecBase
import forms.address.AddressFormProvider
import models.address.Address
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{CountryOptions, InputOption}
import viewmodels.address.ManualAddressViewModel
import views.html.address.{manualAddress => manualAddressView}

trait ControllerBehaviours extends ControllerSpecBase
  with MockitoSugar
  with ScalaFutures
  with CSRFRequest
  with OptionValues { GuiceOneAppPerSuite =>

  def manualAddress(get: Call,
                    post: Call,
                    viewmodel: ManualAddressViewModel
                    )(implicit app: Application): Unit = {

    val countryOptions = new CountryOptions(
      Seq(InputOption("GB", "GB"))
    )

    val formProvider = new AddressFormProvider(countryOptions)
    val form: Form[Address] = formProvider()

    "CompanyAddress Controller" must {

        testTheGet(get, form, viewmodel)

        testThePost(post, form, viewmodel)

    }

  }

  private def testTheGet(get: Call, form: Form[Address], viewmodel: ManualAddressViewModel)(implicit app: Application): Unit =
    "render manualAddress from GET request" in {

        def viewAsString(form: Form[_] = form) = manualAddressView(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

        val request = addToken(FakeRequest(get).withHeaders("Csrf-Token" -> "nocheck"))

        val result = route(app, request).value

        status(result) must be(OK)

        contentAsString(result) mustEqual manualAddressView(
          frontendAppConfig,
          form,
          viewmodel
        )(request, messages).toString


    }

  private def testThePost(post: Call, form: Form[Address], viewmodel: ManualAddressViewModel)(implicit app: Application): Unit =
    "redirect to next page on POST request" which {
      "save address" in {

        val onwardCall = Call("GET", "/")

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postcode = Some("AB1 1AB"),
          country = "GB"
        )

        val fakeRequest = addToken(FakeRequest(post)
          .withHeaders("Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(
            ("addressLine1", address.addressLine1),
            ("addressLine2", address.addressLine2),
            ("postCode", address.postcode.get),
            "country" -> address.country))

        val result = route(app, fakeRequest).value

        status(result) must be(SEE_OTHER)
        redirectLocation(result).value mustEqual onwardCall.url

        //              FakeDataCacheConnector.verify(CompanyAddressId(firstIndex), address)

      }
    }

}
