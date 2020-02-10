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

package forms.register

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.person.PersonName
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import wolfendale.scalacheck.regexp.RegexpGen

class PersonNameFormProviderSpec extends StringFieldBehaviours with Constraints with GuiceOneAppPerSuite {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  val form = new PersonNameFormProvider()(messages("messages__error__trustees"))

  // scalastyle:off magic.number
  private val johnDoe = PersonName("John", "Doe")
  // scalastyle:on magic.number

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = messages("messages__error__first_name", messages("messages__error__trustees"))
    val lengthKey = "messages__error__first_name_length"
    val invalidKey = "messages__error__first_name_invalid"
    val maxLength = PersonNameFormProvider.firstNameLength

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
        "lastName" -> "Doe"
      ),
      expected = "John",
      actual = (model: PersonName) => model.firstName
    )

  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = messages("messages__error__last_name", messages("messages__error__trustees"))
    val lengthKey = "messages__error__last_name_length"
    val invalidKey = "messages__error__last_name_invalid"
    val maxLength = PersonNameFormProvider.lastNameLength

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
        "lastName" -> " Doe  "
      ),
      expected = "Doe",
      actual = (model: PersonName) => model.lastName
    )
  }

  "PersonNameFormProvider" must {
    "apply PersonName correctly" in {
      val details = form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName
        )
      ).get

      details.firstName mustBe johnDoe.firstName
      details.lastName mustBe johnDoe.lastName
    }

    "unapply PersonName correctly" in {
      val filled = form.fill(johnDoe)
      filled("firstName").value.value mustBe johnDoe.firstName
      filled("lastName").value.value mustBe johnDoe.lastName
    }
  }
}
