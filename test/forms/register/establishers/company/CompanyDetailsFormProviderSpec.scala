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

import forms.behaviours.FormBehaviours
import models.{Field, Required}
import models.CompanyDetails
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.FormError

class CompanyDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "companyName" -> "test company name",
    "vatNumber" -> "GB123456789",
    "payeNumber" -> "123/A56789"
  )

  val form = new CompanyDetailsFormProvider()()

  "CompanyDetails form" must {
    behave like questionForm(CompanyDetails("test company name", Some("123456789"), Some("123/A56789")))

    behave like formWithMandatoryTextFields(
      Field("companyName", Required -> "messages__error__company_name")
    )

    Seq("GB123456789", "123435464").foreach{ vatNo =>
      s"successfully bind valid vat number $vatNo" in {
       val coForm = form.bind(Map("companyName" -> "test company name",
          "vatNumber" -> vatNo,
          "payeNumber" -> "123/A56789"
        ))

        coForm.get shouldBe CompanyDetails("test company name", Some(vatNo.replace("GB", "")), Some("123/A56789"))
      }
    }

    "fail to bind when a company name exceeds max length 255" in {
      val companyName = RandomStringUtils.randomAlphabetic(161)
      val data = validData + ("companyName" -> companyName)

      val expectedError: Seq[FormError] = error("companyName", "messages__error__company_name_length", 160)
      checkForError(form, data, expectedError)
    }

    "fail to bind when a paye number exceeds the max length 13" in {
      val payeNumber = RandomStringUtils.randomAlphabetic(14)
      val data = validData + ("payeNumber" -> payeNumber)

      val expectedError: Seq[FormError] = error("payeNumber", "messages__error__paye_length", 13)
      checkForError(form, data, expectedError)
    }
  }
}
