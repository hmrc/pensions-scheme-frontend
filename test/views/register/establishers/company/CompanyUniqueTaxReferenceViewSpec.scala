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

package views.register.establishers.company

import play.api.data.Form
import forms.register.establishers.company.CompanyUniqueTaxReferenceFormProvider
import models.{NormalMode, UniqueTaxReference}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyUniqueTaxReference

class CompanyUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyUniqueTaxReference"

  val form = new CompanyUniqueTaxReferenceFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => companyUniqueTaxReference(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => companyUniqueTaxReference(frontendAppConfig, form,
    NormalMode)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" must {
    behave like normalPage(createView, messageKeyPrefix, messages("messages__establisher__has_sautr__title"))
  }

  "CompanyUniqueTaxReference view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- UniqueTaxReference.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for(option <- UniqueTaxReference.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for(unselectedOption <- UniqueTaxReference.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
