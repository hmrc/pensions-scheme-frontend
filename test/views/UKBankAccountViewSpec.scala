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

import controllers.routes
import forms.UKBankAccountFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.YesNoViewBehaviours
import views.html.uKBankAccount

class UKBankAccountViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "scheme_uk_bank_account"
  private val schemeName = "Test Scheme Name"

  val form = new UKBankAccountFormProvider()(schemeName)

  val view: uKBankAccount = app.injector.instanceOf[uKBankAccount]

  def createView(): () => HtmlFormat.Appendable = () => view(form, NormalMode, schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, NormalMode, schemeName)(fakeRequest, messages)

  "UKBankAccount view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", schemeName))

    behave like yesNoPageExplicitLegend(createView = createViewUsingForm, messageKeyPrefix = messageKeyPrefix,
      legend = Message("messages__scheme_uk_bank_account__h1", schemeName).resolve,
      expectedFormAction = routes.UKBankAccountController.onSubmit(NormalMode).url)

    behave like pageWithReturnLink(createView(), getReturnLink)

  }
}
