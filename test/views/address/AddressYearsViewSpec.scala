/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.address.AddressYearsFormProvider
import models.AddressYears
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.address.AddressYearsViewModel
import views.behaviours.ViewBehaviours
import views.html.address.addressYears

class AddressYearsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "establisher_address_years"

  val form = new AddressYearsFormProvider()("error")

  val testName = "test name"

  val viewmodel = AddressYearsViewModel(
    postCall = Call("GET", "www.example.com"),
    title = Messages("messages__establisher_address_years__title", testName),
    heading = "heading",
    legend = "legend"
  )

  val view: addressYears = app.injector.instanceOf[addressYears]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel, None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel, None)(fakeRequest, messages)

  "AddressYears view" when {
    "rendered" must {
      behave like normalPageWithDynamicTitle(createView(), messageKeyPrefix, viewmodel.heading, testName)

      behave like pageWithReturnLink(createView(), getReturnLink)

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- viewmodel.inputs) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- viewmodel.inputs) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- AddressYears.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}

