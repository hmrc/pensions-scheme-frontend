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

package views.register

import play.api.data.Form
import forms.register.DeclarationDutiesFormProvider
import models.register.DeclarationDuties
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.declarationDuties

class DeclarationDutiesViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "declarationDuties"
  val testSchemeName = "test scheme name"

  val form = new DeclarationDutiesFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => declarationDuties(frontendAppConfig, form, testSchemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => declarationDuties(
    frontendAppConfig, form, testSchemeName)(fakeRequest, messages)

  "DeclarationDuties view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)
  }

  "DeclarationDuties view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- DeclarationDuties.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- DeclarationDuties.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- DeclarationDuties.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
