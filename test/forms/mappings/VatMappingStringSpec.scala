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

package forms.mappings

import forms.behaviours.VatBehavioursString
import play.api.data.Forms._
import play.api.data.{Form, Mapping}

class VatMappingStringSpec extends VatBehavioursString {

  case class VatTestModel(vat: String)

  "VatMapping" must {
    val fieldName = "vat"
    val keyVatLength = "error.length"
    val keyVatInvalid = "error.invalid"

    val fieldMapping: Mapping[String] = vatMapping(keyVatLength, keyVatInvalid)

    val form: Form[VatTestModel] = Form(
      mapping(
        fieldName -> fieldMapping
      )(VatTestModel.apply)(VatTestModel.unapply)
    )

    behave like formWithVatField(
      form,
      fieldName,
      keyVatLength,
      keyVatInvalid
    )

  }

  "vatRegistrationNumberTransform" must {
    "strip leading, trailing ,and internal spaces" in {
      val actual = vatRegistrationNumberTransform("  123 456 789  ")
      actual mustBe "123456789"
    }

    "remove leading GB" in {
      val gb = Table(
        "vat",
        "GB123456789",
        "Gb123456789",
        "gB123456789",
        "gb123456789"
      )

      forAll(gb) { vat =>
        vatRegistrationNumberTransform(vat) mustBe "123456789"
      }
    }
  }

}
