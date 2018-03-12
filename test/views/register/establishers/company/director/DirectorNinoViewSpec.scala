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

package views.register.establishers.company.director

import play.api.data.Form
import controllers.register.establishers.company.director.routes
import forms.register.establishers.company.director.DirectorNinoFormProvider
import views.behaviours.ViewBehaviours
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.html.register.establishers.company.director.directorNino

class DirectorNinoViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "director_nino"
  val establisherIndex = Index(1)
  val directorIndex = Index(1)
  val directorName = "First Name Middle Name Last Name"
  val form = new DirectorNinoFormProvider()()

  def createView: () => HtmlFormat.Appendable = () => directorNino(frontendAppConfig, form, NormalMode, establisherIndex,
    directorIndex,directorName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => directorNino(frontendAppConfig, form, NormalMode,
    establisherIndex, directorIndex, directorName)(fakeRequest, messages)

  "rectorNino view" must {
    behave like normalPage(createView, messageKeyPrefix, messages("messages__director_nino__title"))

    behave like pageWithBackLink(createView)
  }

  "CompanyDirectorNino view" when {
    "rendered" must {
      val ninoOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- ninoOptions) {
          assertContainsRadioButton(doc, s"directorNino_hasNino-$option", "directorNino.hasNino", option, isChecked = false)
        }
      }

      for (option <- ninoOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("directorNino.hasNino" -> s"$option"))))
            assertContainsRadioButton(doc, s"directorNino_hasNino-$option", "directorNino.hasNino", option, isChecked = true)

            for (unselectedOption <- ninoOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"directorNino_hasNino-$unselectedOption", "directorNino.hasNino", unselectedOption, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when yes is selected" in {
        val expectedValue = "AB020202A"
        val doc = asDocument(createViewUsingForm(form.bind(Map("directorNino.hasNino" -> "true", "directorNino.nino" -> expectedValue))))
        doc must haveLabelAndValue("directorNino_nino", s"${messages("messages__common__nino")} ${messages("messages__common__nino_hint")}", expectedValue)
      }

      "display an input text box with the value when no is selected" in {
        val expectedValue = "don't have nino"
        val doc = asDocument(createViewUsingForm(form.bind(Map("directorNino.hasNino" -> "false", "directorNino.reason" -> expectedValue))))
        doc must haveLabelAndValue("directorNino_reason", messages("messages__director_no_nino"), expectedValue)
      }
    }
  }
}
