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

package views.register.establishers.individual

import play.api.data.{Form, FormError}
import controllers.register.establishers.individual.routes
import forms.register.establishers.individual.EstablisherDetailsFormProvider
import models.{EstablisherDetails, Index, NormalMode}
import org.joda.time.LocalDate
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.individual.establisherDetails

class EstablisherDetailsViewSpec extends QuestionViewBehaviours[EstablisherDetails] {

  val messageKeyPrefix = "establisher_individual"

  override val form = new EstablisherDetailsFormProvider()()

  val schemeName = "test scheme name"

  def createView: () => HtmlFormat.Appendable = () =>
    establisherDetails(frontendAppConfig, form, NormalMode, Index(1), schemeName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    establisherDetails(frontendAppConfig, form, NormalMode, Index(1), schemeName)(fakeRequest, messages)

  val day = LocalDate.now().getDayOfMonth
  val year = LocalDate.now().getYear
  val month = LocalDate.now().getMonthOfYear

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName",
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )

  "EstablisherDetails view" must {

    behave like normalPage(createView, messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__title"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix,
      routes.EstablisherDetailsController.onSubmit(NormalMode, Index(0)).url, "firstName", "lastName")

    "display an input text box with the correct label and value for day" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_day", messages("messages__common__day"), s"$day")
    }

    "display an input text box with the correct label and value for month" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_month", messages("messages__common__month"), s"$month")
    }

    "display an input text box with the correct label and value for year" in {
      val doc = asDocument(createViewUsingForm(form.bind(validData)))
      doc must haveLabelAndValue("date_year", messages("messages__common__year"), s"$year")
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
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      )
      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    "display future date error when date is in future" in {
      val expectedError = messages("messages__error__date_future")
      val invalidData: Map[String, String] = Map(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName",
        "date.day" -> s"${day+1}",
        "date.month" -> s"$month",
        "date.year" -> s"$year"
      )
      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }
  }
}
