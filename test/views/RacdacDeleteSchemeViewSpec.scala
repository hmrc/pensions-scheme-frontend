/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.DeleteSchemeFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.YesNoViewBehaviours
import views.html.deleteSchemeRacdac

class RacdacDeleteSchemeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "deleteScheme"
  val schemeName = "Test Scheme Name"
  val psaName = "Test Psa Name"
  private val hintTextMessageKey = "messages__deleteScheme__hint"

  val form = new DeleteSchemeFormProvider()()

  private val deleteSchemeView = injector.instanceOf[deleteSchemeRacdac]

  def createView: () => HtmlFormat.Appendable = () => deleteSchemeView(form, schemeName, psaName, hintTextMessageKey)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => deleteSchemeView(form,
    schemeName, psaName, hintTextMessageKey)(fakeRequest, messages)

  "DeleteScheme view" must {
    behave like normalPageWithTitle(createView, messageKeyPrefix, Message("messages__deleteScheme__title"),
      Message("messages__deleteScheme__heading", schemeName), expectedGuidanceKeys = "_hint")

    behave like pageWithBackLink(createView)

    behave like yesNoPageNoLegend(createViewUsingForm, messageKeyPrefix, controllers.routes.RacdacDeleteSchemeController.onSubmit.url)

    behave like pageWithReturnLink(createView, frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }
}
