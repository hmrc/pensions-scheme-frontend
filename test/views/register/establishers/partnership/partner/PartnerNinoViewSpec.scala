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

package views.register.establishers.partnership.partner

import forms.register.establishers.partnership.partner.PartnerNinoFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.establishers.partnership.partner.partnerNino

class PartnerNinoViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partner_nino"
  val establisherIndex = Index(1)
  val partnerIndex = Index(1)
  val form = new PartnerNinoFormProvider()()

  def createView(): () => HtmlFormat.Appendable = () =>
    partnerNino(frontendAppConfig, form, NormalMode, establisherIndex, partnerIndex)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => partnerNino(frontendAppConfig, form, NormalMode,
    establisherIndex, partnerIndex)(fakeRequest, messages)

  "PartnerNino view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages("messages__partner_nino__title"))

      behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

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
        doc must haveLabelAndValue("nino_reason", messages("messages__partner_no_nino"), expectedValue)
      }
    }
  }
}
