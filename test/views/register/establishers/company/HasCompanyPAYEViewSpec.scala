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

package views.register.establishers.company

import forms.register.establishers.company.HasCompanyPAYEFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.establishers.company.hasCompanyPAYE

class HasCompanyPAYEViewSpec extends YesNoViewBehaviours {

  val companyName = "My company"

  val messageKeyPrefix = "companyPayeRef"

  val form = new HasCompanyPAYEFormProvider()()
  val postCall: Call = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(NormalMode, None, 0)

  def createView: () => HtmlFormat.Appendable = () => hasCompanyPAYE(frontendAppConfig, form, companyName, postCall, None)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => hasCompanyPAYE(frontendAppConfig, form, companyName, postCall, None)(fakeRequest, messages)

  "HasCompanyPAYEView view" must {
    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", companyName))

    behave like pageWithReturnLink(createView, getReturnLink)

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = controllers.register.establishers.company.routes.HasCompanyPAYEController.onSubmit(NormalMode, None, 0).url,
      valueId = "hasPaye"
    )

    behave like pageWithSubmitButton(createView)

  }
}
