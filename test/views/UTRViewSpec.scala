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

import forms.UTRFormProvider
import models.ReferenceValue
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, UTRViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.utr

class UTRViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "companyUtr"

  val form = new UTRFormProvider()()
  val postCall = Call("GET", "/")

  def viewmodel(srn:Option[String]): UTRViewModel = UTRViewModel(
    postCall = postCall,
    title = Message("messages__companyUtr__title"),
    heading = Message("messages__enterUTR"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  def createView(): () => HtmlFormat.Appendable = () =>
    utr(frontendAppConfig, form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    utr(frontendAppConfig, form, viewmodel(None), None)(fakeRequest, messages)

  "UTR view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix,
        pageHeader = messages(s"messages__enterUTR"), "_guidance1", "_guidance2")

      behave like pageWithReturnLinkAndSrn(createView(), getReturnLinkWithSrn)

      behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, postCall.url,
        "utr")

    }
  }
}
