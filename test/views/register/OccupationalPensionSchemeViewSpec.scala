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
import controllers.register.routes
import forms.register.OccupationalPensionSchemeFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.occupationalPensionScheme

class OccupationalPensionSchemeViewSpec extends YesNoViewBehaviours {
  def appConfig(isHubEnabled: Boolean): FrontendAppConfig = new GuiceApplicationBuilder().configure(
    "features.is-hub-enabled" -> isHubEnabled
  ).build().injector.instanceOf[FrontendAppConfig]

  val messageKeyPrefix = "occupational_pension_scheme"

  val form = new OccupationalPensionSchemeFormProvider()()

  def createView(isHubEnabled:Boolean): () => HtmlFormat.Appendable = () =>
    occupationalPensionScheme(appConfig(isHubEnabled), form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    occupationalPensionScheme(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "OccupationalPensionScheme view  with hub disabled" must {

    behave like normalPage(createView(isHubEnabled=false), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView(isHubEnabled=false))

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.OccupationalPensionSchemeController.onSubmit(NormalMode).url)
  }

  "OccupationalPensionScheme view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }
}
