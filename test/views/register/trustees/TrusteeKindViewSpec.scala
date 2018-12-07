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

package views.register.trustees

import forms.register.trustees.TrusteeKindFormProvider
import models.register.trustees.TrusteeKind
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.trustees.trusteeKind

class TrusteeKindViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "trusteeKind"

  private val form = new TrusteeKindFormProvider()()
  private val index = Index(0)

  private def createView(isHubEnabled: Boolean = true) = () => trusteeKind(appConfig(isHubEnabled), form, NormalMode, index)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => trusteeKind(frontendAppConfig, form, NormalMode, index)(fakeRequest, messages)

  private def trusteeKindOptions = TrusteeKind.options

  "TrusteeKind view with hub enabled" must {
    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithReturnLink(createView(), controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView()())
      assertNotRenderedById(doc, "back-link")
    }
  }

  "TrusteeKind view with hub disabled" must {
    behave like pageWithBackLink(createView(isHubEnabled = false))

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }
  }

  "TrusteeKind view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- trusteeKindOptions) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = false)
        }
      }
    }

    for (option <- trusteeKindOptions) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = true)

          for (unselectedOption <- trusteeKindOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }

}
