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

import controllers.register.routes
import forms.register.OccupationalPensionSchemeFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.occupationalPensionScheme

class OccupationalPensionSchemeViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "occupational_pension_scheme"

  val form = new OccupationalPensionSchemeFormProvider()()

  def createView(): () => HtmlFormat.Appendable = () =>
    occupationalPensionScheme(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    occupationalPensionScheme(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "OccupationalPensionScheme view  with hub disabled" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like yesNoPage(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = routes.OccupationalPensionSchemeController.onSubmit(NormalMode).url)

    behave like pageWithReturnLink(createView(), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)
  }
}
