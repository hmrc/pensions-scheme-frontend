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
import forms.register.BenefitsFormProvider
import models.NormalMode
import models.register.Benefits
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import views.behaviours.ViewBehaviours
import views.html.register.benefits

class BenefitsViewSpec extends ViewBehaviours {
  def appConfig(isHubEnabled: Boolean): FrontendAppConfig = new GuiceApplicationBuilder().configure(
    "features.is-hub-enabled" -> isHubEnabled
  ).build().injector.instanceOf[FrontendAppConfig]

  val messageKeyPrefix = "benefits"

  val form = new BenefitsFormProvider()()

  private def createView(isHubEnabled:Boolean) = () =>
    benefits(appConfig(isHubEnabled), form, NormalMode)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    benefits(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "Benefits view with the hub disabled" must {
    behave like normalPage(createView(isHubEnabled = false), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView(isHubEnabled = false))

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }
  }

  "Benefits view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }

  "Benefits view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- Benefits.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- Benefits.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- Benefits.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
