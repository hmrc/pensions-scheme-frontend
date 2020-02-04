/*
 * Copyright 2020 HM Revenue & Customs
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

package views.behaviours

import java.time.LocalDate
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  def normalPageWithTitle(view: () => HtmlFormat.Appendable,
                          messageKeyPrefix: String,
                          title: String,
                          pageHeader: String,
                          expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messagesApi("site.service_name")
        }

        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", title + " - " + messagesApi("messages__pension_scheme_registration__title"))
        }

        "display the correct page header" in {
          val doc = asDocument(view())
          assertPageHeaderEqualsMessage(doc, pageHeader)
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"messages__${messageKeyPrefix}_$key"))
        }
      }
    }

  }

  def normalPageWithHeaderCheck(view: () => HtmlFormat.Appendable,
                          messageKeyPrefix: String,
                          title: String,
                          pageHeader: String,
                          expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messagesApi("site.service_name")
        }

        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", title + " - " + messagesApi("messages__pension_scheme_registration__title"))
        }

        "display the correct page header" in {
          val doc = asDocument(view())
          assertContainsMessages(doc, pageHeader)
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"messages__${messageKeyPrefix}_$key"))
        }
      }
    }

  }
  def normalPageWithoutBrowserTitle(view: () => HtmlFormat.Appendable,
                          messageKeyPrefix: String,
                          pageHeader: String,
                          expectedGuidanceKeys: String*): Unit = {

    "behave like a normal page" when {
      "rendered" must {
        "have the correct banner title" in {
          val doc = asDocument(view())
          val nav = doc.getElementById("proposition-menu")
          val span = nav.children.first
          span.text mustBe messagesApi("site.service_name")
        }

        "display the correct page title" in {
          val doc = asDocument(view())
          assertPageHeaderEqualsMessage(doc, pageHeader)
        }

        "display the correct guidance" in {
          val doc = asDocument(view())
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"messages__${messageKeyPrefix}_$key"))
        }
      }
    }

  }

  def normalPageWithBrowserTitleSame(view: () => HtmlFormat.Appendable,
                                     messageKeyPrefix: String,
                                     pageHeader: String,
                                     expectedGuidanceKeys: String*): Unit = {

    normalPageWithoutBrowserTitle(view, messageKeyPrefix, pageHeader, expectedGuidanceKeys :_*)

    "behave like a normal page with browser title same as H1" when {
      "rendered" must {
        "display the correct browser title" in {
          val doc = asDocument(view())
          assertEqualsMessage(doc, "title", pageHeader + " - " + messagesApi("messages__pension_scheme_registration__title"))
        }

      }
    }

  }

  def normalPage(view: () => HtmlFormat.Appendable,
                 messageKeyPrefix: String,
                 pageHeader: String,
                 expectedGuidanceKeys: String*): Unit = {

    normalPageWithTitle(
      view,
      messageKeyPrefix,
      messagesApi(s"messages__${messageKeyPrefix}__title"),
      pageHeader,
      expectedGuidanceKeys: _*
    )
  }

  def normalPageWithDynamicTitle(view: () => HtmlFormat.Appendable,
                                 messageKeyPrefix: String,
                                 pageHeader: String,
                                 msgArgs: String*): Unit = {
    normalPageWithTitle(
      view,
      messageKeyPrefix,
      messagesApi(s"messages__${messageKeyPrefix}__title", msgArgs: _*),
      pageHeader
    )
  }

  def pageWithBackLink(view: () => HtmlFormat.Appendable): Unit = {

    "behave like a page with a back link" must {
      "have a back link" in {
        val doc = asDocument(view())
        assertRenderedById(doc, "back-link")
      }
    }
  }

  def pageWithSubmitButton(view: () => HtmlFormat.Appendable, action: Option[Call] = None): Unit = {
    "behave like a page with a submit button" in {
      val doc = asDocument(view())
      assertRenderedById(doc, "submit")
      action.map(href => assertLink(doc, "submit", href.url))
    }
  }

  def pageWithoutSubmitButton(view: () => HtmlFormat.Appendable): Unit = {
    "behave like a page without a submit button" in {
      val doc = asDocument(view())
      assertNotRenderedById(doc, "submit")
    }
  }

  def pageWithReturnLink(view: () => HtmlFormat.Appendable, url: String): Unit = {
    s"have a return link $url" in {
      val doc = asDocument(view())
      assertLink(doc, "return-link", url)
    }
  }

  def pageWithReturnLinkAndSrn(view: () => HtmlFormat.Appendable, url: String): Unit = {
    s"have a return link $url with srn" in {
      val doc = asDocument(view())
      assertLink(doc, "return-link", url)
    }
  }

  def pageWithReturnChangeLink(view: () => HtmlFormat.Appendable): Unit = {
    s"have a change link" in {
      val doc = asDocument(view())

      assertRenderedById(doc, "cya-0-0-change")
    }
  }

  def pageWithoutReturnChangeLink(view: () => HtmlFormat.Appendable): Unit = {
    s"don't have a return link" in {
      val doc = asDocument(view())
      assertNotRenderedById(doc, "cya-0-0-change")
    }
  }

  def pageWithDateFields(view: Form[_] => HtmlFormat.Appendable, form: Form[_]): Unit = {

    val day = LocalDate.now().getDayOfMonth
    val year = LocalDate.now().getYear
    val month = LocalDate.now().getMonthOfYear


    val validData: Map[String, String] = Map(
      "date.day" -> s"$day",
      "date.month" -> s"$month",
      "date.year" -> s"$year"
    )

    "display an input text box with the correct label and value for day" in {

      val v = view(form.bind(validData))

      val doc = asDocument(v)
      doc must haveLabelAndValue("date_day", messages("messages__common__day"), s"$day")
    }

    "display an input text box with the correct label and value for month" in {
      val doc = asDocument(view(form.bind(validData)))
      doc must haveLabelAndValue("date_month", messages("messages__common__month"), s"$month")
    }

    "display an input text box with the correct label and value for year" in {
      val doc = asDocument(view(form.bind(validData)))
      doc must haveLabelAndValue("date_year", messages("messages__common__year"), s"$year")
    }

    "display error for day field on error summary" in {
      val error = "error"
      val doc = asDocument(view(form.withError(FormError("date.day", error))))
      doc must haveErrorOnSummary("date_day", error)
    }

    "display error for month field on error summary" in {
      val error = "error"
      val doc = asDocument(view(form.withError(FormError("date.month", error))))
      doc must haveErrorOnSummary("date_month", error)
    }

    "display error for year field on error summary" in {
      val error = "error"
      val doc = asDocument(view(form.withError(FormError("date.year", error))))
      doc must haveErrorOnSummary("date_year", error)
    }

    "display only one date error when all the date fields are missing" in {
      val expectedError = s"${messages("site.error")} ${messages("messages__error__date")}"
      val invalidData: Map[String, String] = Map(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName"
      )
      val doc = asDocument(view(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

    "display future date error when date is in future" in {
      val tomorrow = LocalDate.now.plusDays(1)
      val expectedError = s"${messages("site.error")} ${messages("messages__error__date_future")}"
      val invalidData: Map[String, String] = Map(
        "firstName" -> "testFirstName",
        "lastName" -> "testLastName",
        "date.day" -> s"${tomorrow.getDayOfMonth}",
        "date.month" -> s"${tomorrow.getMonthOfYear}",
        "date.year" -> s"${tomorrow.getYear}"
      )
      val doc = asDocument(view(form.bind(invalidData)))
      doc.select("span.error-notification").text() mustEqual expectedError
    }

  }
}
