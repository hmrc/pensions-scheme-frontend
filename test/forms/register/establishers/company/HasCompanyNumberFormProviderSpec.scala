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

package forms.register.establishers.company

import base.SpecBase
import forms.FormSpec
import org.scalatest.Assertion
import play.api.data.{Form, FormError}
import viewmodels.Message

class HasCompanyNumberFormProviderSpec extends SpecBase  {

  val requiredKey = Message("messages__hasCompanyNumber__error__required", "ABC").resolve
  val invalidKey = "error.boolean"

  def formProvider(companyName:String) = new HasCompanyNumberFormProvider().apply(companyName)

  "HasCompanyNumber Form Provider" must {

    "bind true" in {
      val form = formProvider("ABC").bind(Map("value" -> "true"))
      form.get mustBe true
    }

    "bind false" in {
      val form = formProvider("ABC").bind(Map("value" -> "false"))
      form.get mustBe false
    }

    "fail to bind non-booleans" in {
      val expectedError = error("value", invalidKey)
      checkForError(formProvider("ABC"), Map("value" -> "not a boolean"), expectedError)
    }

    "fail to bind a blank value" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider("ABC"), Map("value" -> ""), expectedError)
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("value", requiredKey)
      checkForError(formProvider("ABC"), emptyForm, expectedError)
    }
  }

  private def checkForError(form: Form[_], data: Map[String, String], expectedErrors: Seq[FormError]): Assertion = {

    form.bind(data).fold(
      formWithErrors => {
        for (error <- expectedErrors) formWithErrors.errors must contain(FormError(error.key, error.message, error.args))
        formWithErrors.errors.size mustBe expectedErrors.size
      },
      _ => {
        fail("Expected a validation error when binding the form, but it was bound successfully.")
      }
    )
  }

  private def error(key: String, value: String, args: Any*): Seq[FormError] = Seq(FormError(key, value, args))

  lazy val emptyForm: Map[String, String] = Map[String, String]()
}
