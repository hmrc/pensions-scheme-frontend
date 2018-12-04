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

import forms.register.DeclarationDormantFormProvider
import models.register.DeclarationDormant
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.declarationDormant

class DeclarationDormantViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "declarationDormant"

  val form = new DeclarationDormantFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => declarationDormant(frontendAppConfig, form)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => declarationDormant(frontendAppConfig, form)(fakeRequest, messages)

  "DeclarationDormant view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView)
  }

  "DeclarationDormant view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- DeclarationDormant.options(frontendAppConfig)) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- DeclarationDormant.options(frontendAppConfig)) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- DeclarationDormant.options(frontendAppConfig).filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
