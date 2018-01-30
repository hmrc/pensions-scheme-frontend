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

package forms.register.establishers.company

import forms.FormSpec
import forms.behaviours.FormBehaviours
import models._

class CompanyRegistrationNumberFormProviderSpec extends FormSpec {

  val requiredKey: String = "messages__error__has_crn_company"
  val requiredCRNKey: String = "messages__error__crn"
  val requiredReasonKey : String = "messages__company__no_crn"
  val invalidCRNKey: String = "messages__error__crn_invalid"

  val formProvider = new CompanyRegistrationNumberFormProvider()()

  val validData:Map[String,String] = Map(
    "companyRegistrationNumber.hasCrn" ->"true",
    "companyRegistrationNumber.crn" -> "1234567"
  )

  "CompanyRegistrationNumber form" must {

    "successfully bind when yes is selected and valid CRN is provided" in {
      val form = formProvider.bind(Map("companyRegistrationNumber.hasCrn" -> "true", "companyRegistrationNumber.crn" -> "1234567"))
      form.get shouldBe CompanyRegistrationNumber.Yes("1234567")
    }

    "successfully bind when no is selected and reason is provided" in {
      val form = formProvider.bind(Map("companyRegistrationNumber.hasCrn" -> "false", "companyRegistrationNumber.reason" -> "haven't got Crn"))
      form.get shouldBe CompanyRegistrationNumber.No("haven't got Crn")
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("companyRegistrationNumber.hasCrn", requiredKey)
      checkForError(formProvider, emptyForm, expectedError)
    }
  }
}
