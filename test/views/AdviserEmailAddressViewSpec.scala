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

import forms.register.adviser.AdviserEmailFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.adviserEmailAddress

class AdviserEmailAddressViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "adviser__email__address"
  val form = new AdviserEmailFormProvider().apply()
  val adviserName = "test adviser"

  private val createView: () => HtmlFormat.Appendable = () => adviserEmailAddress(frontendAppConfig, form, NormalMode, adviserName, None)(fakeRequest, messages)

  private val createViewWithForm: Form[String] => HtmlFormat.Appendable =
    (form: Form[String]) => adviserEmailAddress(frontendAppConfig, form, NormalMode, adviserName, None)(fakeRequest, messages)

  behave like normalPage(createView, messageKeyPrefix,
    messages("messages__adviser__email__address__heading", adviserName))

  behave like pageWithErrorOutsideLabel(
    createViewWithForm,
    messageKeyPrefix,
    controllers.routes.AdviserEmailAddressController.onSubmit(NormalMode).url,
    "email"
  )

  "display the paragraph" in {
    Jsoup.parse(createViewWithForm(form).toString()) must haveDynamicText(s"messages__${messageKeyPrefix}__p1", adviserName)
  }

  behave like pageWithSubmitButton(createView)

  behave like pageWithReturnLink(createView, getReturnLink)
}
