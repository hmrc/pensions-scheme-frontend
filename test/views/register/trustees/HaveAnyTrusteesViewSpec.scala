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

package views.register.trustees

import controllers.routes
import forms.register.trustees.HaveAnyTrusteesFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.trustees.haveAnyTrustees

class HaveAnyTrusteesViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "haveAnyTrustees"

  val form = new HaveAnyTrusteesFormProvider()()
  val submitUrl = controllers.register.trustees.routes.HaveAnyTrusteesController.onSubmit(NormalMode, None)
  def createView(): () => HtmlFormat.Appendable = () =>
    haveAnyTrustees(frontendAppConfig, form, NormalMode, None, submitUrl)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => haveAnyTrustees(frontendAppConfig, form,
    NormalMode, None, submitUrl)(fakeRequest, messages)

  "HaveAnyTrustees view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading"))

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, routes.HaveAnyTrusteesController.onSubmit(NormalMode).url)

    behave like pageWithReturnLink(createView(), getReturnLink)
  }

}
