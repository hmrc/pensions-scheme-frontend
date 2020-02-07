/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import forms.address.AddressFormProvider
import identifiers.TypedIdentifier
import models.address.Address
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{CountryOptions, InputOption}
import viewmodels.address.ManualAddressViewModel
import views.html.address.{manualAddress => manualAddressView}
import play.api.test.CSRFTokenHelper.addCSRFToken

trait AddressControllerBehaviours extends ControllerSpecBase
  with MockitoSugar
  with ScalaFutures
  with OptionValues {

  private val view = injector.instanceOf[manualAddressView]

  def manualAddress(get: Call,
                    post: Call,
                    id: TypedIdentifier[Address],
                    viewmodel: ManualAddressViewModel
                   )(implicit builder: GuiceApplicationBuilder): Unit = {

    val countryOptions = new CountryOptions(
      Seq(InputOption("GB", "GB"))
    )

    val formProvider = new AddressFormProvider(countryOptions)
    val form: Form[Address] = formProvider()

    "ManualAddressController" must {

      testTheGet(get, form, viewmodel)

    }

  }

  private def testTheGet(get: Call, form: Form[Address], viewmodel: ManualAddressViewModel)(implicit builder: GuiceApplicationBuilder): Unit =
    "render manualAddress from GET request" in {
      running(_ => builder) {
        implicit app =>

          val request = addCSRFToken(FakeRequest(get).withHeaders("Csrf-Token" -> "nocheck"))

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual view(
            form,
            viewmodel,
            None
          )(request, messages).toString
      }
    }

}
