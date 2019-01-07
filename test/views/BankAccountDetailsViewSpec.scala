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

package views

import controllers.routes
import forms.BankAccountDetailsFormProvider
import models.{BankAccountDetails, NormalMode}
import play.api.data.{Form, FormError}
import views.behaviours.QuestionViewBehaviours
import views.html.bankAccountDetails

class BankAccountDetailsViewSpec extends QuestionViewBehaviours[BankAccountDetails] {

  val messageKeyPrefix = "bank_account_details"

  override val form = new BankAccountDetailsFormProvider()()

  private val schemeName = "Test Scheme Name"

  private def createView() = () =>
    bankAccountDetails(frontendAppConfig, form, NormalMode, schemeName)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    bankAccountDetails(frontendAppConfig, form, NormalMode, schemeName)(fakeRequest, messages)

  "Bank Account Details view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__h1", schemeName))

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      routes.BankAccountDetailsController.onSubmit(NormalMode).url,
      "bankName", "accountName", "accountNumber"
    )

    "contain an input for each sortCode field" in {
      Seq("sortCode_first", "sortCode_second", "sortCode_third") foreach { field =>
        val doc = asDocument(createViewUsingForm(form))
        assertRenderedById(doc, field)
      }
    }

    "show an error in the legend for sortCode when sort code field has error" in {
      val doc = asDocument(createViewUsingForm(form.withError(FormError("sortCode", "error"))))
      val errorSpan = doc.getElementsByClass("error-notification").first
      errorSpan.id mustBe "error-message-sortCode-input"
    }

    behave like pageWithReturnLink(createView(), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)
  }
}
