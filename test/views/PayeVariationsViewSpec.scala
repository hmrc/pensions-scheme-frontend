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

import forms.PayeVariationsFormProvider
import models.ReferenceValue
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, PayeViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.payeVariations

class PayeVariationsViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "enter_paye"
  val postCall = Call("GET", "/")
  val companyName = "test company name"
  val form = new PayeVariationsFormProvider()(companyName)

  private def viewmodel(srn:Option[String]) = PayeViewModel(
    postCall = postCall,
    title = Message("messages__enterPAYE", Message("messages__theCompany").resolve),
    heading = Message("messages__enterPAYE", companyName),
    hint = Some(Message("messages__enter_paye__hint")),
    srn = srn,
    entityName = Some(companyName)
  )

  def createView(): () => HtmlFormat.Appendable = () =>
    payeVariations(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    payeVariations(frontendAppConfig, form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    payeVariations(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  "Paye view" when {
    "rendered" must {
      behave like normalPageWithTitle(createView(), messageKeyPrefix,
        title = Message("messages__enterPAYE", Message("messages__theCompany").resolve),
        pageHeader = Message("messages__enterPAYE", companyName))

      behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

      behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, postCall.url,
        "paye")

      "display correct p1" in {
        val doc = asDocument(createView()())
        doc must haveDynamicText(Message("messages__enter_paye__p1", companyName))
      }
    }
  }
}
