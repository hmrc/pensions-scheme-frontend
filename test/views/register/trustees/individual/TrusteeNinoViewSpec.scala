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

package views.register.trustees.individual

import forms.register.trustees.individual.TrusteeNinoFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.trustees.individual.trusteeNino

class TrusteeNinoViewSpec extends ViewBehaviours {

  import TrusteeNinoViewSpec._

  "TrusteeNino view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

      behave like pageWithReturnLink(createView(), getReturnLink)

      behave like pageWithSubmitButton(createView())

      val ninoOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- ninoOptions) {
          assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, isChecked = false)
        }
      }

      for (option <- ninoOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("nino.hasNino" -> s"$option"))))
            assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, isChecked = true)

            for (unselectedOption <- ninoOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"nino_hasNino-$unselectedOption", "nino.hasNino", unselectedOption, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when yes is selected" in {
        val expectedValue = "AB020202A"
        val doc = asDocument(createViewUsingForm(form.bind(Map("nino.hasNino" -> "true", "nino.nino" -> expectedValue))))
        doc must haveLabelAndValue("nino_nino", s"${messages("messages__common__nino")} ${messages("messages__common__nino_hint")}", expectedValue)
      }

      "display an input text box with the value when no is selected" in {
        val expectedValue = "don't have nino"
        val doc = asDocument(createViewUsingForm(form.bind(Map("nino.hasNino" -> "false", "nino.reason" -> expectedValue))))
        doc must haveLabelAndValue("nino_reason", messages("messages__trusteeNino__no_nino"), expectedValue)
      }
    }
  }

}

object TrusteeNinoViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "trusteeNino"

  private val mode = NormalMode
  private val index = Index(0)

  private val form = new TrusteeNinoFormProvider()()
  val submitUrl = controllers.register.trustees.individual.routes.TrusteeNinoController.onSubmit(NormalMode, index, None)
  private def createView() =
    () => trusteeNino(
      frontendAppConfig,
      form,
      mode,
      index,
      None,
      submitUrl
    )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => trusteeNino(
      frontendAppConfig,
      form,
      mode,
      index,
      None,
      submitUrl
    )(fakeRequest, messages)

}
