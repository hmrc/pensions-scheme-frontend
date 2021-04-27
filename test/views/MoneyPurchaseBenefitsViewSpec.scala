/*
 * Copyright 2021 HM Revenue & Customs
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

package views

import controllers.routes
import forms.MoneyPurchaseBenefitsFormProvider
import models.{MoneyPurchaseBenefits, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.moneyPurchaseBenefits

class MoneyPurchaseBenefitsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "moneyPurchaseBenefits"

  val schemeName = "schemeName"
  val form = new MoneyPurchaseBenefitsFormProvider()()
  val postCall: Call = routes.MoneyPurchaseBenefitsController.onSubmit(NormalMode, None)
  val view: moneyPurchaseBenefits = app.injector.instanceOf[moneyPurchaseBenefits]

  private def createView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, Some(schemeName), postCall, None)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, Some(schemeName), postCall, None)(fakeRequest, messages)

  "MoneyPurchaseBenefits view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1",schemeName))

      behave like pageWithReturnLink(createView(), getReturnLink)

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for ((option, i) <- MoneyPurchaseBenefits.options.zipWithIndex) {
          assertContainsRadioButton(doc, s"value-$i", s"$i", option.value, isChecked = false)
        }
      }
    }

    for ((option, i) <- MoneyPurchaseBenefits.options.zipWithIndex) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-$i", s"$i", option.value, isChecked = false)

        }
      }
    }
  }
}
