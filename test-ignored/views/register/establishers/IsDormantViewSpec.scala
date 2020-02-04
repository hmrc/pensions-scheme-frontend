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

package views.register.establishers

import controllers.register.establishers.company.routes
import forms.register.establishers.IsDormantFormProvider
import models.NormalMode
import models.register.DeclarationDormant
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.isDormant

class IsDormantViewSpec extends ViewBehaviours {

  val companyName = "My company"

  val messageKeyPrefix = "is_dormant"

  val form = new IsDormantFormProvider()()
  val postCall: Call = routes.IsCompanyDormantController.onSubmit(NormalMode, None, 0)

  def createView: () => HtmlFormat.Appendable = () => isDormant(frontendAppConfig, form, companyName, postCall, None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => isDormant(frontendAppConfig, form, companyName, postCall, None)(fakeRequest, messages)

  "IsDormant view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", companyName))

    behave like pageWithReturnLink(createView, getReturnLink)

  }

  "IsDormant view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- DeclarationDormant.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- DeclarationDormant.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- DeclarationDormant.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
