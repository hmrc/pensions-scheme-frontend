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

import forms.CurrentMembersFormProvider
import models.{Members, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.currentMembers

class CurrentMembersViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "current_members"

  private val schemeName = "Test Scheme Name"

  val form = new CurrentMembersFormProvider()(schemeName)

  val view: currentMembers = app.injector.instanceOf[currentMembers]

  def createView(): () => HtmlFormat.Appendable = () => view(form, NormalMode, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, schemeName)(fakeRequest, messages)

  "Current Members view" when {
    "rendered" must {
      behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", schemeName))

      behave like pageWithReturnLink(createView(), getReturnLink)

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- Members.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = false)
        }
      }
    }

    for (option <- Members.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = true)

          for (unselectedOption <- Members.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }
}
