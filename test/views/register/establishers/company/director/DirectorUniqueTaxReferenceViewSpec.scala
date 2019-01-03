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

package views.register.establishers.company.director

import forms.register.establishers.company.CompanyUniqueTaxReferenceFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.company.director.directorUniqueTaxReference

class DirectorUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "director_has_sautr"
  val form: Form[_] = new CompanyUniqueTaxReferenceFormProvider().apply()
  val establisherIndex = Index(1)
  val directorIndex = Index(1)

  def createView(): () => HtmlFormat.Appendable = () =>
    directorUniqueTaxReference(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => directorUniqueTaxReference(frontendAppConfig, form,
    NormalMode, establisherIndex, directorIndex)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages("messages__director_has_sautr__title"))

      behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

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
        doc must haveLabelAndValue("uniqueTaxReference_utr", s"${messages("messages__director_sautr")} ${
          messages(
            "messages__director_sautr_hint_format")
        }",
          expectedValue)
      }

      "display an input text box with the value when no is selected" in {
        val expectedValue = "don't have ctutr"
        val doc = asDocument(createViewUsingForm(form.bind(Map("uniqueTaxReference.hasUtr" -> "false", "uniqueTaxReference.reason" ->
          expectedValue))))
        doc must haveLabelAndValue("uniqueTaxReference_reason", messages(
          "messages__director_no_sautr"), expectedValue)
      }
    }
  }
}
