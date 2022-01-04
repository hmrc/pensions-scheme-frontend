/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.EnterVATFormProvider
import models.ReferenceValue
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{EnterVATViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.enterVATView

class EnterVATViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "enterVAT"

  val form = new EnterVATFormProvider()("companyName")
  val postCall = Call("GET", "/")

  def viewModel(srn:Option[String]): EnterVATViewModel = EnterVATViewModel(
    postCall = postCall,
    title = Message(s"messages__$messageKeyPrefix"),
    heading = Message(s"messages__$messageKeyPrefix"),
    hint = Message(s"messages__${messageKeyPrefix}__hint"),
    subHeading = None,
    srn = srn
  )

  val view: enterVATView = app.injector.instanceOf[enterVATView]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewModel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel(None), None)(fakeRequest, messages)

  "Vat Variations view" when {
    "rendered" must {
      behave like normalPageWithTitle(createView(), messageKeyPrefix,
        title = Message(s"messages__$messageKeyPrefix"),
        pageHeader = Message(s"messages__$messageKeyPrefix"))

      behave like pageWithReturnLinkAndSrn(createView(), getReturnLinkWithSrn)

      behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, postCall.url,
        "vat")

    }
  }
}
