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

package views.register.establishers.company.director

import controllers.register.establishers.company.director.routes
import forms.register.establishers.company.director.DirectorDOBFormProvider
import models.{Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.establishers.company.director.directorDOB

class DirectorDOBViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "directorDOB"

  val establisherIndex = Index(1)
  val directorIndex = Index(1)
  val personName = "John Doe"
  private val postCall = routes.DirectorDOBController.onSubmit _

  override val form = new DirectorDOBFormProvider()()

  def createView(): () => HtmlFormat.Appendable = () =>
    directorDOB(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), None, personName)(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    directorDOB(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), Some("srn"), personName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    directorDOB(frontendAppConfig, form, NormalMode, establisherIndex, directorIndex, None,
      postCall(NormalMode, establisherIndex, directorIndex, None), None, personName)(fakeRequest, messages)

  private val day = LocalDate.now().getDayOfMonth
  private val year = LocalDate.now().getYear
  private val month = LocalDate.now().getMonthOfYear

  val validData: Map[String, String] = Map(
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )

  "DirectorDOB view" must {

    behave like normalPage(createView(), messageKeyPrefix, messages(s"messages__${messageKeyPrefix}__heading", "John Doe"))

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
      val invalidData: Map[String, String] = Map.empty

      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    "display future date error when date is in future" in {
      val tomorrow = LocalDate.now.plusDays(1)
      val expectedError = messages("messages__error__date_future")
      val invalidData: Map[String, String] = Map(
        "date.day" -> s"${tomorrow.getDayOfMonth}",
        "date.month" -> s"${tomorrow.getMonthOfYear}",
        "date.year" -> s"${tomorrow.getYear}"
      )
      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
