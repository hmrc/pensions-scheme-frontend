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

import controllers.routes
import forms.register.trustees.HaveAnyTrusteesFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.haveAnyTrustees

class HaveAnyTrusteesViewSpec extends YesNoViewBehaviours {

  private val scheme = "Test Scheme Name"
  val messageKeyPrefix = "haveAnyTrustees"

  val form = new HaveAnyTrusteesFormProvider()()

  val view: haveAnyTrustees = app.injector.instanceOf[haveAnyTrustees]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, NormalMode, scheme)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, scheme)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => view(form,
    NormalMode, scheme)(fakeRequest, messages)

  "HaveAnyTrustees view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", scheme))

    behave like yesNoPageExplicitLegend(createViewUsingForm, messageKeyPrefix, routes.HaveAnyTrusteesController.onSubmit(NormalMode).url,
      legend = messages("messages__haveAnyTrustees__heading", scheme) )

    behave like pageWithReturnLink(createView(), frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }

  "HaveAnyTrustees view in check mode" must {
    behave like pageWithReturnLink(createViewInCheckMode, controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)
  }
}
