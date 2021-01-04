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

import forms.register.SchemeTypeFormProvider
import models.register.SchemeType
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.schemeType

class SchemeTypeViewSpec extends QuestionViewBehaviours[SchemeType] {

  val messageKeyPrefix = "scheme_type"
  private val schemeName = "test scheme"

  override val form = new SchemeTypeFormProvider()()

  val view: schemeType = app.injector.instanceOf[schemeType]

  def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode, schemeName)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, schemeName)(fakeRequest, messages)

  private def schemeOptions = SchemeType.options(frontendAppConfig)

  "SchemeType view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", schemeName))

    behave like pageWithReturnLink(createView, frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }

  "SchemeType view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))

        for (option <- schemeOptions) {
          assertContainsRadioButton(doc, s"schemeType_type-${option.value}", "schemeType.type", option.value, isChecked = false)
        }
      }

      for (option <- schemeOptions) {
        s"rendered with a value of '${option.value}'" must {
          s"have the '${option.value}' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("schemeType.type" -> s"${option.value}"))))
            assertContainsRadioButton(doc, s"schemeType_type-${option.value}", "schemeType.type", option.value, isChecked = true)

            for (unselectedOption <- schemeOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"schemeType_type-${unselectedOption.value}", "schemeType.type", unselectedOption.value, isChecked = false)
            }
          }
        }
      }

      "display an input text box with the value when the other is selected" in {
        val expectedValue = "some value"
        val doc = asDocument(createViewUsingForm(form.bind(Map("schemeType.type" -> "Other", "schemeType.schemeTypeDetails" -> expectedValue))))
        doc must haveLabelAndValue("schemeType_schemeTypeDetails", messages("messages__scheme_details__type_other_more"), expectedValue)
      }
    }
  }

  "SchemeType view in check mode" must {
    behave like pageWithReturnLink(createViewInCheckMode, controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)
  }
}
