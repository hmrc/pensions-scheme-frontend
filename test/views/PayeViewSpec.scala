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

package views

import forms.PayeFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, PayeViewModel}
import views.behaviours.ViewBehaviours
import views.html.paye

class PayeViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipPaye"

  val form = new PayeFormProvider()()

  def viewmodel(srn:Option[String]) = PayeViewModel(
    postCall = Call("GET", "/"),
    title = Message("messages__partnershipPaye__title"),
    heading = Message("messages__partnershipPaye__heading"),
    hint = Some(Message("messages__common__paye_hint")),
    srn = srn
  )

  def createView(): () => HtmlFormat.Appendable = () =>
    paye(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    paye(frontendAppConfig, form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    paye(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  "Paye view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__heading"))

      behave like pageWithReturnLink(createView(), getReturnLink)

      behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

      val payeOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- payeOptions) {
          assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = false)
        }
      }


      for (option <- payeOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("paye.hasPaye" -> s"$option"))))
            assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = true)

            for (unselectedOption <- payeOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"paye_hasPaye-$unselectedOption", "paye.hasPaye", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
