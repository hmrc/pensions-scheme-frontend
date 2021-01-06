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

import forms.ReasonFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, ReasonViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.reason

class ReasonViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "noCompanyUtr"

  val form = new ReasonFormProvider()("messages__reason__error_ninoRequired", "companyName")
  val postCall: Call = Call("GET", "/")

  def viewmodel(srn:Option[String]): ReasonViewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__whyNoUTR"),
    heading = Message("messages__whyNoUTR"),
    srn = srn
  )

  val view: reason = app.injector.instanceOf[reason]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel(None), None)(fakeRequest, messages)

  "Reason view" when {
    "rendered" must {
      behave like normalPageWithTitle(createView(), messageKeyPrefix,
        title = Message("messages__whyNoUTR"),
        pageHeader = Message("messages__whyNoUTR"))

      behave like pageWithReturnLinkAndSrn(createView(), getReturnLinkWithSrn)

      behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, postCall.url,
        "reason")

    }
  }
}
