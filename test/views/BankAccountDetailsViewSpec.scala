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
import forms.BankAccountDetailsFormProvider
import models.{BankAccountDetails, NormalMode}
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.bankAccountDetails

class BankAccountDetailsViewSpec extends QuestionViewBehaviours[BankAccountDetails] {

  val messageKeyPrefix = "bank_account_details"

  override val form = new BankAccountDetailsFormProvider()()

  private val schemeName = "Test Scheme Name"

  val view: bankAccountDetails = app.injector.instanceOf[bankAccountDetails]

  private def createView() = () =>
    view(form, NormalMode, schemeName)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    view(form, NormalMode, schemeName)(fakeRequest, messages)

  "Bank Account Details view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", schemeName))

    behave like pageWithErrorOutsideLabel(
      createViewUsingForm,
      messageKeyPrefix,
      routes.BankAccountDetailsController.onSubmit(NormalMode).url, "accountNumber", "sortCode"
    )

    behave like pageWithReturnLink(createView(), getReturnLink)
  }
}
