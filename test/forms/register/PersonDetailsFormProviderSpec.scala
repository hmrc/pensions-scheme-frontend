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

package forms.register

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class PersonDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  val form = new PersonDetailsFormProvider()()

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", new LocalDate(1962, 6, 9))
  // scalastyle:on magic.number

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = "messages__error__first_name"
    val lengthKey = "messages__error__first_name_length"
    val invalidKey = "messages__error__first_name_invalid"
    val maxLength = PersonDetailsFormProvider.firstNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexName)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      invalidString = "1A",
      error = FormError(fieldName, invalidKey, Seq(regexName))
    )

    behave like fieldWithTransform(
      form,
      fieldName,
      Map(
        "firstName" -> "  John ",
        "lastName" -> "Doe",
        "date.day" -> "9",
        "date.month" -> "6",
        "date.year" -> "1903"
      ),
      expected = "John",
      actual = (model: PersonDetails) => model.firstName
    )

  }

  ".middleName" must {

    val fieldName = "middleName"
    val lengthKey = "messages__error__middle_name_length"
    val invalidKey = "messages__error__middle_name_invalid"
    val maxLength = PersonDetailsFormProvider.middleNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexName)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      invalidString = "1A",
      error = FormError(fieldName, invalidKey, Seq(regexName))
    )

    behave like optionalField(
      form,
      fieldName,
      Map(
        "firstName" -> "John",
        "middleName" -> "J",
        "lastName" -> "Doe",
        "date.day" -> "9",
        "date.month" -> "6",
        "date.year" -> "1967"
      ),
      (model: PersonDetails) => model.middleName
    )

  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = "messages__error__last_name"
    val lengthKey = "messages__error__last_name_length"
    val invalidKey = "messages__error__last_name_invalid"
    val maxLength = PersonDetailsFormProvider.lastNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexName)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "1A",
      FormError(fieldName, invalidKey, Seq(regexName))
    )

    behave like fieldWithTransform(
      form,
      fieldName,
      Map(
        "firstName" -> "John",
        "lastName" -> " Doe  ",
        "date.day" -> "9",
        "date.month" -> "6",
        "date.year" -> "1956"
      ),
      expected = "Doe",
      actual = (model: PersonDetails) => model.lastName
    )
  }

  ".date" must {

    val fieldName = "date"
    val requiredKey = "messages__error__date"
    val invalidKey = "error.invalid_date"

    behave like dateFieldThatBindsValidData(
      form,
      fieldName,
      historicDate()
    )

    behave like mandatoryDateField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "only accept numeric input" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "date.day" -> "A",
          "date.month" -> "A",
          "date.year" -> "A"
        )
      ).errors must contain allOf(
        FormError("date.day", "error.date.day_invalid"),
        FormError("date.month", "error.date.month_invalid"),
        FormError("date.year", "error.date.year_invalid")
      )
    }

    "only accept inputs that are a valid date" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "date.day" -> "32",
          "date.month" -> "13",
          "date.year" -> "0"
        )
      ).errors mustBe Seq(FormError(fieldName, invalidKey))
    }

    val futureDate = LocalDate.now().plusDays(1)
    "not accept a future date" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "date.day" -> futureDate.getDayOfMonth.toString,
          "date.month" -> futureDate.getMonthOfYear.toString,
          "date.year" -> futureDate.getYear.toString
        )
      ).errors mustBe Seq(FormError(fieldName, "messages__error__date_future"))
    }

    "not accept a year before 1900" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "1899"
        )
      ).errors mustBe Seq(FormError(fieldName, "messages__error__date_past"))
    }
  }

  "PersonDetailsFormProvider" must {
    "apply PersonDetails correctly" in {
      val details = form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "date.day" -> johnDoe.date.getDayOfMonth.toString,
          "date.month" -> johnDoe.date.getMonthOfYear.toString,
          "date.year" -> johnDoe.date.getYear.toString
        )
      ).get

      details.firstName mustBe johnDoe.firstName
      details.lastName mustBe johnDoe.lastName
      details.date mustBe johnDoe.date
    }

    "unapply PersonDetails corectly" in {
      val filled = form.fill(johnDoe)
      filled("firstName").value.value mustBe johnDoe.firstName
      filled("lastName").value.value mustBe johnDoe.lastName
      filled("date.day").value.value mustBe johnDoe.date.getDayOfMonth.toString
      filled("date.month").value.value mustBe johnDoe.date.getMonthOfYear.toString
      filled("date.year").value.value mustBe johnDoe.date.getYear.toString
    }
  }
}
