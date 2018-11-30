/*
 * Copyright 2018 HM Revenue & Customs
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

package views.register

import config.FrontendAppConfig
import forms.register.SchemeTypeFormProvider
import models.NormalMode
import models.register.SchemeType
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.{schemeDetails, schemeType}

class SchemeTypeViewSpec extends QuestionViewBehaviours[SchemeType] {

  val messageKeyPrefix = "scheme_type"
  private val schemeName = "test scheme"

  override val form = new SchemeTypeFormProvider()()

  def appConfig(isHubEnabled: Boolean): FrontendAppConfig = new GuiceApplicationBuilder().configure(
    "features.is-hub-enabled" -> isHubEnabled
  ).build().injector.instanceOf[FrontendAppConfig]

  def createView: () => HtmlFormat.Appendable = () => schemeType(frontendAppConfig, form, NormalMode, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    schemeDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  private def schemeOptions = SchemeType.options(appConfig(true))

  "SchemeType view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", schemeName))

    behave like pageWithReturnLink(createView, controllers.register.routes.SchemeTaskListController.onPageLoad().url)
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
    }
  }
}
