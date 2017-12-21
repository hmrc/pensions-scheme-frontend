/*
 * Copyright 2017 HM Revenue & Customs
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

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.BenefitsInsurer
import org.apache.commons.lang3.RandomStringUtils

class BenefitsInsurerFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "companyName" -> "value 1",
    "policyNumber" -> "value 2"
  )

  val validMaxLength = 255
  val invalidMaxLength = 256

  val form = new BenefitsInsurerFormProvider()()

  "BenefitsInsurer form" must {
    behave like questionForm(BenefitsInsurer("value 1", "value 2"))

    behave like formWithMandatoryTextFields(
      Field("companyName", Required -> "messages__error__company_name"),
      Field("policyNumber", Required -> "messages__error__benefits_insurance__policy")
    )
  }

  "fail to bind when the company name exceeds max length 255" in {
    val testString = RandomStringUtils.random(invalidMaxLength)
    val data = Map(
      "companyName" -> testString,
      "policyNumber" -> "value 2")
    val expectedError = error("companyName", "messages__error__companyName_length", validMaxLength)
    checkForError(form, data, expectedError)
  }

  "fail to bind when the policy number exceeds max length 255" in {
    val testString = RandomStringUtils.random(invalidMaxLength)
    val data = Map(
      "companyName" -> "value 1",
      "policyNumber" -> testString)
    val expectedError = error("policyNumber", "messages__error__policyNumber_length", validMaxLength)
    checkForError(form, data, expectedError)
  }
}
