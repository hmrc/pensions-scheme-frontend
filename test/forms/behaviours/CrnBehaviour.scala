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

package forms.behaviours

import forms.FormSpec
import forms.mappings.CrnMapping
import models.CompanyRegistrationNumber.Yes
import play.api.data.{Form, FormError}

trait CrnBehaviour extends FormSpec with CrnMapping {

  def formWithCrn(testForm: Form[_]): Unit = {

    "behave like form with crn" must {

      "bind successfully when CRN is valid" in {

        val result = testForm.bind(Map("companyRegistrationNumber.hasCrn" -> "true", "companyRegistrationNumber.crn" -> "1234567"))
        result.errors.size shouldBe 0
        result.get shouldBe Yes("1234567")
      }

      "fail to bind when yes is selected but Company Registration Number is not provided" in {
        val result = testForm.bind(Map("companyRegistrationNumber.hasCrn" -> "true"))
        result.errors shouldBe Seq(FormError("companyRegistrationNumber.crn", "messages__error__crn"))
      }

      "fail to bind when no is selected but reason is not provided" in {
        val result = testForm.bind(Map("companyRegistrationNumber.hasCrn" -> "false"))
        result.errors shouldBe Seq(FormError("companyRegistrationNumber.reason", "messages__error__no_crn_company"))
      }

      "fail to bind when CRN is invalid" in {
        val result = testForm.bind(Map("companyRegistrationNumber.hasCrn" -> "true", "companyRegistrationNumber.crn" -> "123.456"))
        result.errors shouldBe Seq(FormError("companyRegistrationNumber.crn", "messages__error__crn_invalid"))
      }
    }
  }

}
