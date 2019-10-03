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

import forms.EmailFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.emailAddress

class EmailAddressViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "establisher_email"
  val form = new EmailFormProvider().apply()
  val companyName = "test company"

  val viewModel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    Message("messages__trustee_email__title"),
    Message("messages__enterEmail", companyName),
    Some(Message("messages__contact_details__hint", companyName)),
    None
  )

  private val createView: () => HtmlFormat.Appendable = () => emailAddress(frontendAppConfig, form, viewModel, Some("test scheme"))(fakeRequest, messages)

  private val createViewWithForm: Form[String] => HtmlFormat.Appendable =
    (form: Form[String]) => emailAddress(frontendAppConfig, form, viewModel, Some("test scheme"))(fakeRequest, messages)



  behave like normalPageWithTitle(createView, messageKeyPrefix, Message("messages__trustee_email__title"),
    messages("messages__enterEmail", companyName))

  "have correct hint text" in {
    assertContainsText(asDocument(createView()), messages("messages__contact_details__hint", companyName))
  }

  behave like pageWithTextFields(
    createViewWithForm,
    messageKeyPrefix,
    Call("POST", "/").url,
    "email"
  )

  behave like pageWithSubmitButton(createView)

  behave like pageWithReturnLink(createView, getReturnLink)
}
