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

import forms.PhoneFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.phoneNumber

class PhoneNumberViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "establisher_phone"
  val form = new PhoneFormProvider().apply()
  val companyName = "test company"

  val viewModel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    Message("messages__trustee_phone__title"),
    Message("messages__enterPhoneNumber", companyName),
    Some(Message("messages__contact_details__hint", companyName)),
    None
  )

  val view: phoneNumber = app.injector.instanceOf[phoneNumber]

  private val createView: () => HtmlFormat.Appendable = () => view(form, viewModel, Some("test scheme"))(fakeRequest, messages)

  private val createViewWithForm: Form[String] => HtmlFormat.Appendable =
    (form: Form[String]) => view(form, viewModel, Some("test scheme"))(fakeRequest, messages)

  behave like normalPageWithTitle(createView, messageKeyPrefix, Message("messages__trustee_phone__title"), messages("messages__enterPhoneNumber", companyName))

  "have correct hint text" in {
    assertContainsText(asDocument(createView()), messages("messages__contact_details__hint", companyName))
  }

  behave like pageWithErrorOutsideLabel(
    createViewWithForm,
    messageKeyPrefix,
    Call("POST", "/").url,
    "phone"
  )

  behave like pageWithSubmitButton(createView)

  behave like pageWithReturnLink(createView, getReturnLink)
}