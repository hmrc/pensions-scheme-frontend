/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.NINOFormProvider
import models.ReferenceValue
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, NinoViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.nino

class NinoViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "enterNino"

  val form = new NINOFormProvider()("Mark")

  val testName = "test name"

  private def viewmodel(srn: Option[String]) = NinoViewModel(
    postCall = Call("POST", "/"),
    title = Message("messages__enterNINO", Message("messages__thePerson").resolve),
    heading = Message("messages__enterNINO", testName),
    hint = Message("messages__common__nino_hint"),
    srn = srn
  )

  val view: nino = app.injector.instanceOf[nino]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(None), None)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    view(form, viewmodel(Some("srn")), None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel(None), None)(fakeRequest, messages)

  "Nino view" when {

    "rendered" must {
      behave like normalPageWithTitle(createView(), messageKeyPrefix,
        title =  messages("messages__enterNINO", messages("messages__thePerson")),
        pageHeader = messages(s"messages__enterNINO", testName))

      behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, Call("POST", "/").url,
        "nino")

      behave like pageWithReturnLink(createView(), getReturnLink)

      behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)

    }
  }
}
