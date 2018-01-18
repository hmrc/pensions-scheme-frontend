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
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.companyUniqueTaxReference

class CompanyUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "establisher__has_ct_utr__title"

  val form = new CompanyUniqueTaxReferenceFormProvider()()

  val index = Index(1)

  def createView: () => HtmlFormat.Appendable = () => companyUniqueTaxReference(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => companyUniqueTaxReference(frontendAppConfig, form,
    NormalMode, index)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("establisher__has_ct_utr__title"))
  }

  "CompanyUniqueTaxReference view" when {
    "rendered" must {
      val utrOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- utrOptions) {
          assertContainsRadioButton(doc, s"companyUniqueTaxReference_hasUtr-$option", "companyUniqueTaxReference.hasUtr", option, isChecked = false)
        }
      }


      for (option <- utrOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("companyUniqueTaxReference.hasUtr" -> s"$option"))))
            assertContainsRadioButton(doc, s"companyUniqueTaxReference_hasUtr-$option", "companyUniqueTaxReference.hasUtr", option, isChecked = true)

            for (unselectedOption <- utrOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"companyUniqueTaxReference_hasUtr-$unselectedOption", "companyUniqueTaxReference.hasUtr", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
