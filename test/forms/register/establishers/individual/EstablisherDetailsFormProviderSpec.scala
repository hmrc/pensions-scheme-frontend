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

package forms.register.establishers.individual

import forms.behaviours.FormBehaviours
import models.register.establishers.individual.EstablisherDetails
import models.{Field, Required}
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.LocalDate

class EstablisherDetailsFormProviderSpec extends FormBehaviours {

  val day = LocalDate.now().getDayOfMonth
  val month = LocalDate.now().getMonthOfYear
  val year = LocalDate.now().getYear

  val validData: Map[String, String] = Map(
    "firstName" -> "testFirstName",
    "lastName" -> "testLastName",
    "date.day" -> s"$day",
    "date.month" -> s"$month",
    "date.year" -> s"$year"
  )
  val regexFirstName = "[a-zA-Z]{1}[a-zA-Z-‘]*"
  val regexLastName = "[a-zA-Z0-9,.‘(&)-/ ]*"

  val form = new EstablisherDetailsFormProvider()()

  val date = new LocalDate(year, month, day)

  "EstablisherDetails form" must {
    behave like questionForm(EstablisherDetails("testFirstName", "testLastName", date))

    behave like formWithMandatoryTextFields(
      Field("firstName", Required -> "messages__error__first_name"),
      Field("lastName", Required -> "messages__error__last_name"),
      Field("date.day", Required -> "messages__error__date"),
      Field("date.month", Required -> "messages__error__date"),
      Field("date.year", Required -> "messages__error__date")
    )

    "fail to bind when the first name exceeds max length 35" in {
      val testString = RandomStringUtils.random(36)
      val data = validData + ("firstName" -> testString)

      val expectedError = error("firstName", "messages__error__first_name_length", 35)
      checkForError(form, data, expectedError)
    }

    "fail to bind when the last name exceeds max length 35" in {
      val testString = RandomStringUtils.random(36)
      val data = validData + ("lastName" -> testString)

      val expectedError = error("lastName", "messages__error__last_name_length", 35)
      checkForError(form, data, expectedError)
    }

    Seq("-sfygAFD", "‘GHJGJG", "SDSAF^*NJ", "^*", "first name").foreach { name =>
      s"fail to bind when the first name $name is invalid" in {
        val data = validData + ("firstName" -> name)

        val expectedError = error("firstName", "messages__error__first_name_invalid", regexFirstName)
        checkForError(form, data, expectedError)
      }
    }

    s"fail to bind when the last name is invalid" in {
      val data = validData + ("lastName" -> "strbvhjbv^*")

      val expectedError = error("lastName", "messages__error__last_name_invalid", regexLastName)
      checkForError(form, data, expectedError)
    }

    Seq("first-name", "King‘s").foreach { firstName =>
      s"successfully bind valid first name $firstName" in {

        val detailsForm = form.bind(Map("firstName" -> firstName,
          "lastName" -> "testLastName",
          "date.day" -> s"$day",
          "date.month" -> s"$month",
          "date.year" -> s"$year"))

        val expectedData = EstablisherDetails(firstName, "testLastName", date)

        detailsForm.get shouldBe expectedData
      }
    }

    Seq("-242798‘/", "(last,.&)", "Last Name").foreach { lastName =>
      s"successfully bind valid last name $lastName" in {

        val detailsForm = form.bind(Map("firstName" -> "testFirstName",
          "lastName" -> lastName,
          "date.day" -> s"$day",
          "date.month" -> s"$month",
          "date.year" -> s"$year"))

        val expectedData = EstablisherDetails("testFirstName", lastName, date)
        detailsForm.get shouldBe expectedData
      }
    }

    "fail to bind when the date is in future" in {
      val tomorrow = LocalDate.now.plusDays(1)
      val data = validData +
        ("date.day" -> s"${tomorrow.getDayOfMonth}", "date.month" -> s"${tomorrow.getMonthOfYear}", "date.year" -> s"${tomorrow.getYear}")

      val expectedError = error("date", "messages__error__date_future")
      checkForError(form, data, expectedError)
    }
  }
}
