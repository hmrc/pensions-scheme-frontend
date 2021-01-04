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

import forms.register.SchemeNameFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.schemeName

class SchemeNameViewSpec extends QuestionViewBehaviours[String] {
  private val scheme = "A scheme"
  val messageKeyPrefix = "scheme_name"

  override val form = new SchemeNameFormProvider()()

  val view: schemeName = app.injector.instanceOf[schemeName]

  def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode, scheme)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, scheme)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, scheme)(fakeRequest, messages)

  "SchemeName view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, controllers.routes.SchemeNameController.onSubmit(NormalMode).url,
      "schemeName")

    behave like pageWithReturnLink(createView, frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }

  "SchemeName view in check mode" must {
    behave like pageWithReturnLink(createViewInCheckMode, controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)
  }
}