/*
 * Copyright 2024 HM Revenue & Customs
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

package forms.register.establishers.company

import com.google.inject.Inject
import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import forms.register.NoCompanyNumberFormProvider
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import wolfendale.scalacheck.regexp.RegexpGen

class NoCompanyNumberFormProviderSpec @Inject() extends StringFieldBehaviours with Constraints with GuiceOneAppPerSuite {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(FakeRequest())

  val validData: Map[String, String] = Map(
    "reason" -> "test reason")
  val validMaxLength = 160
  val form = new NoCompanyNumberFormProvider().apply("test company")

  ".reason" must {
    val fieldName = "reason"
    val lengthKey = "messages__error__no_company_number_maxlength"
    val requiredKey = Messages("messages__error__no_company_number", "test company")
    val invalidKey = "messages__error__no_company_number_invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(regexSafeText)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = validMaxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(validMaxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "{name}",
      error = FormError(fieldName, invalidKey, Seq(regexSafeText))
    )
  }
}