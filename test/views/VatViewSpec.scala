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

import forms.VatFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, EnterVATViewModel}
import views.behaviours.ViewBehaviours
import views.html.vat

class VatViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipVat"

  val form = new VatFormProvider()()

  def viewmodel(srn:Option[String]) = EnterVATViewModel(
    postCall = Call("GET", "/"),
    title = Message("messages__partnershipVat__title"),
    heading = Message("messages__partnershipVat__heading"),
    hint = Message("messages__common__vat__hint"),
    subHeading = Some(Message("test company name")),
    srn = srn
  )

  def createView(): () => HtmlFormat.Appendable = () =>
    vat(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)
  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    vat(frontendAppConfig, form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    vat(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  "Vat view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__heading"))

      behave like pageWithReturnLink(createView(), getReturnLink)

      behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

      val vatOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- vatOptions) {
          assertContainsRadioButton(doc, s"vat_hasVat-$option", "vat.hasVat", option, isChecked = false)
        }
      }


      for (option <- vatOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("vat.hasVat" -> s"$option"))))
            assertContainsRadioButton(doc, s"vat_hasVat-$option", "vat.hasVat", option, isChecked = true)

            for (unselectedOption <- vatOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"vat_hasVat-$unselectedOption", "vat.hasVat", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
