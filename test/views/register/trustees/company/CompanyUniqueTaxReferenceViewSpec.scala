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

package views.register.trustees.company

import forms.register.trustees.company.CompanyUniqueTaxReferenceFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.trustees.company.companyUniqueTaxReference

class CompanyUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "trustee__has_ct_utr"

  val form: Form[_] = new CompanyUniqueTaxReferenceFormProvider().apply()

  val index = Index(1)

  def createView(): () => HtmlFormat.Appendable = () =>
    companyUniqueTaxReference(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => companyUniqueTaxReference(frontendAppConfig, form,
    NormalMode, index)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages("messages__trustee__has_ct_utr__title"))

      behave like pageWithReturnLink(createView(), getReturnLink)

      val utrOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- utrOptions) {
          assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$option", "uniqueTaxReference.hasUtr", option, isChecked = false)
        }
      }

      for (option <- utrOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> s"$option"))))
            assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$option", "uniqueTaxReference.hasUtr", option, isChecked = true)

            for (unselectedOption <- utrOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$unselectedOption", "uniqueTaxReference.hasUtr",
                unselectedOption, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when yes is selected" in {
        val expectedValue = "1234567891"
        val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> "true", "uniqueTaxReference.utr" ->
          expectedValue))))
        doc must haveLabelAndValue("uniqueTaxReference_utr", s"${messages("messages__trustee__ct_utr")} ${
          messages(
            "messages__trustee__ct_utr_hint_format")
        }",
          expectedValue)
      }

      "display an input text box with the value when no is selected" in {
        val expectedValue = "don't have ctutr"
        val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> "false", "uniqueTaxReference.reason" ->
          expectedValue))))
        doc must haveLabelAndValue("uniqueTaxReference_reason", messages(
          "messages__trustee__no_ct_utr"), expectedValue)
      }
    }
  }
}
