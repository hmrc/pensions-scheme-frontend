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

package views.register.establishers.individual

import play.api.data.Form
import forms.register.establishers.individual.UniqueTaxReferenceFormProvider
import views.behaviours.ViewBehaviours
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.html.register.establishers.individual.uniqueTaxReference

class UniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "establisher__has_sautr"

  val form = new UniqueTaxReferenceFormProvider()()

  val index = Index(1)
  val establisherName = "test name"

  def createView: () => HtmlFormat.Appendable = () => uniqueTaxReference(frontendAppConfig, form, NormalMode, index, establisherName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => uniqueTaxReference(frontendAppConfig, form, NormalMode,
    index, establisherName)(fakeRequest, messages)

  "UniqueTaxReference view" must {

    behave like normalPage(createView, messageKeyPrefix, messages("messages__establisher__has_sautr__title"))
  }

  "UniqueTaxReference view" when {
    "rendered" must {
      val utrOptions = Seq("yes", "no")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- utrOptions) {
          assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$option", "uniqueTaxReference.hasUtr", option, isChecked = false)
        }
      }

      for (option <- utrOptions) {
        s"rendered with a value of '$option'" must {
          s"have '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> s"$option"))))
            assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$option", "uniqueTaxReference.hasUtr", option, isChecked = true)

            for (unselectedOption <- utrOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"uniqueTaxReference_hasUtr-$unselectedOption", "uniqueTaxReference.hasUtr", unselectedOption, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when yes is selected" in {
        val expectedValue = "1234567891"
        val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> "yes", "uniqueTaxReference.utr" -> expectedValue))))
        doc must haveLabelAndValue("uniqueTaxReference_utr", s"${messages("messages__common__utr")} ${messages("messages__common__utr_hint_format")}", expectedValue)
      }

      "display an input text box with the value when no is selected" in {
        val expectedValue = "don't have utr"
        val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> "no", "uniqueTaxReference.reason" -> expectedValue))))
        doc must haveLabelAndValue("uniqueTaxReference_reason", messages("messages__establisher__no_sautr"), expectedValue)
      }
    }
  }
}
