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

package views.address

import forms.address.ConfirmAddressFormProvider
import models.address.{Address, TolerantAddress}
import models.{Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.address.confirmPreviousAddress

class ConfirmAddressViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "confirmPreviousAddress"
  val schemeName = Some("Test Scheme Name")
  val name = "Test name"
  val formProvider = new ConfirmAddressFormProvider()
  val form = formProvider(name)

  val testAddress = Address(
    "address line 1",
    "address line 2",
    Some("test town"),
    Some("test county"),
    Some("test post code"),
    "GB"
  )

  val testCountry = "United Kingdom"

  def viewmodel(mode: Mode = NormalMode): ConfirmAddressViewModel = ConfirmAddressViewModel(
    postCall = Call("GET", "www.example.com"),
    title = Message("messages__confirmPreviousAddress__title"),
    heading = Message("messages__confirmPreviousAddress__heading", name),
    hint = None,
    address = testAddress,
    name = "Test name",
    srn = Some("srn")
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def createView: () => HtmlFormat.Appendable = () => confirmPreviousAddress(frontendAppConfig, form, viewmodel(), countryOptions,
    schemeName)(fakeRequest, messages)

  def createViewUpdateMode: () => HtmlFormat.Appendable = () => confirmPreviousAddress(frontendAppConfig, form, viewmodel(UpdateMode), countryOptions,
    schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    confirmPreviousAddress(frontendAppConfig, form, viewmodel(), countryOptions, schemeName)(fakeRequest, messages)

  "Same Contact Address View" must {
    behave like normalPage(createView, messageKeyPrefix, viewmodel().heading)

    behave like yesNoPageExplicitLegend(createViewUsingForm, messageKeyPrefix, "www.example.com", legend = viewmodel().heading)

    behave like pageWithSubmitButton(createView)

    behave like pageWithReturnLink(createView, getReturnLinkWithSrn)

    "display the address" in {
      val doc = asDocument(createView())
      assertRenderedByIdWithText(doc, "address-value-0", testAddress.addressLine1)
      assertRenderedByIdWithText(doc, "address-value-1", testAddress.addressLine2)
      assertRenderedByIdWithText(doc, "address-value-2", testAddress.addressLine3.value)
      assertRenderedByIdWithText(doc, "address-value-3", testAddress.addressLine4.value)
      assertRenderedByIdWithText(doc, "address-value-4", testAddress.postcode.value)
      assertRenderedByIdWithText(doc, "address-value-5", testCountry)
    }
  }

}
