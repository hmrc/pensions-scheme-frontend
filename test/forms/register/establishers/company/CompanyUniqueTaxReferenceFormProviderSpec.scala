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
import models.UniqueTaxReference


class CompanyUniqueTaxReferenceFormProviderSpec extends FormSpec {

  val requiredKey = "messages__error__has_ct_utr_establisher"
  val requiredUtrKey = "messages__error__ct_utr"
  val requiredReasonKey = "messages__error__no_ct_utr_establisher"
  val invalidUtrKey = "messages__error__ct_utr_invalid"
  val maxLengthReasonKey = "messages__error__no_sautr_length"

  val formProvider = new CompanyUniqueTaxReferenceFormProvider()

  "CompanyUniqueTaxReference form" must {
    "successfully bind when the utr is provided and yes is selected" in {
      val form = formProvider().bind(Map("companyUniqueTaxReference.hasUtr" -> "true", "companyUniqueTaxReference.utr" -> "1234556676"))
      form.get shouldBe UniqueTaxReference.Yes("1234556676")
    }

    "successfully bind when the reason is provided and no is selected" in {
      val form = formProvider().bind(Map("companyUniqueTaxReference.hasUtr" -> "false", "companyUniqueTaxReference.reason" -> "haven't got ctutr"))
      form.get shouldBe UniqueTaxReference.No("haven't got ctutr")
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("companyUniqueTaxReference.hasUtr", requiredKey)
      checkForError(formProvider(), emptyForm, expectedError)
    }

    "fail to bind when yes is selected but utr is not provided" in {
      val expectedError = error("companyUniqueTaxReference.utr", requiredUtrKey)
      checkForError(formProvider(), Map("companyUniqueTaxReference.hasUtr" -> "true"), expectedError)
    }

    "fail to bind when no is selected and reason is not provided" in {
      val expectedError = error("companyUniqueTaxReference.reason", requiredReasonKey)
      checkForError(formProvider(), Map("companyUniqueTaxReference.hasUtr" -> "false"), expectedError)
    }

  }
}
