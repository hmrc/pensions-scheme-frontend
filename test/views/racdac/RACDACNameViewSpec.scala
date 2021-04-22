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

package views.racdac

import forms.racdac.RACDACNameFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.racdac.racDACName

class RACDACNameViewSpec extends QuestionViewBehaviours[String] {
  private val psaName = "A PSA"
  val messageKeyPrefix = "racdac_name"

  override val form = new RACDACNameFormProvider()()

  val view: racDACName = app.injector.instanceOf[racDACName]

  def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode, psaName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, psaName)(fakeRequest, messages)

  def createViewInCheckMode: () => HtmlFormat.Appendable = () =>
    view(form, CheckMode, psaName)(fakeRequest, messages)

  "RACDACName view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithErrorOutsideLabel(createViewUsingForm, messageKeyPrefix, controllers.racdac.routes.RACDACNameController.onSubmit().url,
      "racDACName")

    behave like pageWithReturnLink(createView, frontendAppConfig.managePensionsSchemeOverviewUrl.url)
  }

//  "RACDACName view in check mode" must {
//    behave like pageWithReturnLink(createViewInCheckMode, controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None).url)
//  }
}
