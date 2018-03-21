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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import forms.address.AddressFormProvider
import play.api.data.Form
import play.api.mvc.Call
import utils.InputOption
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class PreviousAddressControllerSpec extends ControllerSpecBase {

  val viewmodel = ManualAddressViewModel(
    Call("GET", "/"),
    Seq.empty[InputOption],
    "title",
    "heading",
    None
  )

  val formProvider = new AddressFormProvider()
  val form = formProvider()

  def viewAsString(form: Form[_] = form) = manualAddress(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

  "PreviousAddress Controller" must {

  }
}
