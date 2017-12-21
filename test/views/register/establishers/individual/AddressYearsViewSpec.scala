/*
 * Copyright 2017 HM Revenue & Customs
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

package views.register.establishers.individual

import play.api.data.Form
import forms.register.establishers.individual.AddressYearsFormProvider
import models.{NormalMode, AddressYears}
import views.behaviours.ViewBehaviours
import views.html.register.establishers.individual.addressYears

class AddressYearsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "establisher_address_years"

  val form = new AddressYearsFormProvider()()

  def createView = () => addressYears(frontendAppConfig, form, NormalMode, 0)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => addressYears(frontendAppConfig, form, NormalMode, 0)(fakeRequest, messages)

  "AddressYears view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "AddressYears view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- AddressYears.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for(option <- AddressYears.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for(unselectedOption <- AddressYears.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
