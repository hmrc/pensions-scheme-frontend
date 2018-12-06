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
import forms.register.UKBankDetailsFormProvider
import models.NormalMode
import models.register.UKBankDetails
import org.apache.commons.lang3.RandomUtils
import org.joda.time.LocalDate
import play.api.data.{Form, FormError}
import views.behaviours.QuestionViewBehaviours
import views.html.register.uKBankDetails

class UKBankDetailsViewSpec extends QuestionViewBehaviours[UKBankDetails] {

  val messageKeyPrefix = "uk_bank_account_details"

  override val form = new UKBankDetailsFormProvider()()

  private def createView(isHubEnabled:Boolean) = () =>
    uKBankDetails(appConfig(isHubEnabled), form, NormalMode)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    uKBankDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  val validData: Map[String, String] = Map(
    "bankName" -> "test bank",
    "accountName" -> "test account",
    "sortCode" -> RandomUtils.nextInt(100000, 999999).toString,
    "accountNumber" -> RandomUtils.nextInt(10000000, 99999999).toString,
    "date.day" -> "1",
    "date.month" -> "2",
    "date.year" -> LocalDate.now().getYear.toString
  )

  "UKBankDetails view" must {

    behave like normalPage(createView(isHubEnabled = false), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithBackLink(createView(isHubEnabled = false))

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      routes.UKBankDetailsController.onSubmit(NormalMode).url,
      "bankName", "accountName", "sortCode", "accountNumber"
    )

    "not have a return link" in {
      val doc = asDocument(createView(isHubEnabled = false)())
      assertNotRenderedById(doc, "return-link")
    }

    "display an input text box with the correct label and value for day" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_day", messages("messages__common__day"), "1")
    }

    "display an input text box with the correct label and value for month" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_month", messages("messages__common__month"), "2")
    }

    "display an input text box with the correct label and value for year" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_year", messages("messages__common__year"), LocalDate.now().getYear.toString)
    }

    "display error for day field on error summary" in {
      val error = "error"
      val doc = asDocument(createViewUsingForm(form.withError(FormError("date.day", error))))
      doc must haveErrorOnSummary("date_day", error)
    }

    "display error for month field on error summary" in {
      val error = "error"
      val doc = asDocument(createViewUsingForm(form.withError(FormError("date.month", error))))
      doc must haveErrorOnSummary("date_month", error)
    }

    "display error for year field on error summary" in {
      val error = "error"
      val doc = asDocument(createViewUsingForm(form.withError(FormError("date.year", error))))
      doc must haveErrorOnSummary("date_year", error)
    }

    "display only one date error when all the date fields are missing" in {
      val expectedError = messages("messages__error__date")
      val invalidData: Map[String, String] = Map(
        "bankName" -> "test bank",
        "accountName" -> "test account",
        "sortCode" -> RandomUtils.nextInt(100000, 999999).toString,
        "accountNumber" -> RandomUtils.nextInt(10000000, 99999999).toString
      )
      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }
  }

  "UKBankDetails view with hub enabled" must {
    behave like pageWithReturnLink(createView(isHubEnabled = true), url = controllers.register.routes.SchemeTaskListController.onPageLoad().url)

    "not have a back link" in {
      val doc = asDocument(createView(isHubEnabled = true)())
      assertNotRenderedById(doc, "back-link")
    }
  }
}
