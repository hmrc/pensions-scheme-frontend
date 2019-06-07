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

import forms.VatVariationsFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, VatViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.vatVariations

class VatVariationsViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "vatVariations"

  val form = new VatVariationsFormProvider()()
  val postCall = Call("GET", "/")

  def viewmodel(srn:Option[String]): VatViewModel = VatViewModel(
    postCall = postCall,
    title = Message("messages__vatVariations__company_title"),
    heading = Message("messages__vatVariations__heading"),
    hint = Message("messages__vatVariations__hint"),
    subHeading = None,
    srn = srn
  )

  def createView(): () => HtmlFormat.Appendable = () =>
    vatVariations(frontendAppConfig, form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    vatVariations(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  "Vat Variations view" when {
    "rendered" must {
      behave like normalPageWithoutBrowserTitle(createView(), messageKeyPrefix, pageHeader = messages(s"messages__${messageKeyPrefix}__heading"))

      behave like pageWithReturnLinkAndSrn(createView(), getReturnLinkWithSrn)

      behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, postCall.url,
        "vat")

    }
  }
}
