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

class CompanyDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "companyName" -> "value 1",
    "vatNumber" -> "value 2",
    "payeNumber" -> "value 3"
  )

  val form = new CompanyDetailsFormProvider()()

  "CompanyDetails form" must {
    behave like questionForm(CompanyDetails("value 1", "value 2, value 3"))

    behave like formWithMandatoryTextFields(
      Field("companyName", Required -> "companyDetails.error.field1.required"),
      Field("vatNumber", Required -> "companyDetails.error.field2.required"),
      Field("payeN", Required -> "companyDetails.error.field2.required")
    )
  }
}
