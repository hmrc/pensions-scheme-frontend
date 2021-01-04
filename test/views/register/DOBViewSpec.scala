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

package views.register

import forms.DOBFormProvider
import models.{Index, Mode, NormalMode, UpdateMode}
import java.time.LocalDate
import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat
import viewmodels.dateOfBirth.DateOfBirthViewModel
import views.behaviours.QuestionViewBehaviours
import views.html.register.DOB

class DOBViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "DOB"

  val index: Index = Index(1)
  val personName: String = "John Doe"
  private val postCall = controllers.routes.IndexController.onPageLoad()

  private def viewModel(mode: Mode, index: Index, srn: Option[String], token: String): DateOfBirthViewModel = {
    DateOfBirthViewModel(
      postCall = postCall,
      srn = srn,
      token = token
    )
  }

  override val form = new DOBFormProvider()()
  val view: DOB = app.injector.instanceOf[DOB]
  def createView(): () => HtmlFormat.Appendable = () =>
    view(
      form,
      NormalMode,
      None,
      personName,
      viewModel(NormalMode, 0, None, "user token")
    )(fakeRequest, messages)

  def createUpdateView(): () => HtmlFormat.Appendable = () =>
    view(
      form,
      UpdateMode,
      Some("srn"),
      personName,
      viewModel(UpdateMode, 0, Some("srn"), "user token")
    )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(
      form,
      NormalMode,
      None,
      personName,
      viewModel(NormalMode, 0, None, "user token")
    )(fakeRequest, messages)

  private val day = LocalDate.now().getDayOfMonth
  private val year = LocalDate.now().getYear
  private val month = LocalDate.now().getMonthValue

  val validData: Map[String, String] = Map(
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )

  "DOB view" must {

    behave like normalPageWithTitle(createView(), messageKeyPrefix,
      messages("messages__DOB__heading", "user token"), messages(s"messages__${messageKeyPrefix}__heading", personName))

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
      val expectedError =s"${messages("site.error")} ${messages("messages__error__date")}"
      val invalidData: Map[String, String] = Map.empty

      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    "display future date error when date is in future" in {
      val tomorrow = LocalDate.now.plusDays(1)
      val expectedError = s"${messages("site.error")} ${messages("messages__error__date_future")}"
      val invalidData: Map[String, String] = Map(
        "date.day" -> s"${tomorrow.getDayOfMonth}",
        "date.month" -> s"${tomorrow.getMonthValue}",
        "date.year" -> s"${tomorrow.getYear}"
      )
      val doc = asDocument(createViewUsingForm(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    behave like pageWithReturnLink(createView(), getReturnLink)

    behave like pageWithReturnLinkAndSrn(createUpdateView(), getReturnLinkWithSrn)
  }
}
