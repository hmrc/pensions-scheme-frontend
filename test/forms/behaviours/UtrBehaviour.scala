/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.mappings.UtrMapping
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait UtrBehaviour extends FormSpec with UtrMapping with ScalaCheckPropertyChecks with Generators with StringFieldBehaviours {

  //  scalastyle:off magic.number

  def formWithUniqueTaxReference[A](testForm: Form[A],
                                    fieldName: String,
                                    requiredKey: String,
                                    maxLengthKey: String,
                                    invalidKey: String): Unit = {

    "behave like form with UTR" must {

      behave like fieldThatBindsValidData(
        testForm,
        fieldName,
        RegexpGen.from(regexUtr)
      )

      behave like mandatoryField(
        testForm,
        fieldName,
        FormError("utr", requiredKey)
      )

      Seq("1234", "12345678909").foreach { utr =>
        s"not bind numbers $utr with more/less than 10 digits" in {
          val result = testForm.bind(Map(fieldName -> utr)).apply(fieldName)
          result.errors mustEqual Seq(FormError("utr", maxLengthKey, Seq(10)))
        }
      }

      behave like fieldWithRegex(
        testForm,
        fieldName,
        invalidString = "AB12344555",
        FormError("utr", invalidKey, Seq(regexUtr))
      )
    }
  }
}
