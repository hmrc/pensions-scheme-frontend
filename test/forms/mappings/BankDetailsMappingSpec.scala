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

import forms.behaviours.BankDetailsBehaviour
import models.register.SortCode
import play.api.data.Form


class BankDetailsMappingSpec extends BankDetailsBehaviour {

  "SortCodeMapping" should {
    val testForm: Form[SortCode] = Form("sortCode" -> sortCodeMapping("error.required", "error.invalid", "error.max.error"))

    behave like formWithSortCode(
      testForm,
      "error.required",
      "error.invalid",
      "error.max.error",
      Map.empty,
      (sortCode: SortCode) => sortCode
    )
  }

  "AccountNumberMapping" should {
    val testForm: Form[String] = Form("accountNumber" -> accountNumberMapping("error.required", "error.invalid"))

    behave like formWithAccountNumber(
      testForm,
      "error.required",
      "error.invalid",
      "error.max.error",
      Map.empty,
      (str: String) => str
    )
  }
}
