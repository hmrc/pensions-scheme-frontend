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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError

class NinoYesFormProviderSpec extends StringFieldBehaviours with Constraints {

  val validData: Map[String, String] = Map("nino" -> "CS700100A")
  val form = new NinoYesFormProvider()()

  ".nino" must {
    val fieldName = "nino"
    val requiredKey = "messages__error__nino"
    val invalidKey = "messages__error__nino_invalid"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "successfully bind when yes is selected and valid NINO is provided" in {
      val res = form.bind(Map("nino" -> "AB020202A"))
      res.get shouldEqual "AB020202A"
    }

    "successfully bind when yes is selected and valid NINO with spaces is provided" in {
      val res = form.bind(Map("nino" -> " a b 0 2 0 2 0 2 a "))
      res.get shouldEqual "AB020202A"
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val result = form.bind(Map("nino" -> nino))
        result.errors shouldBe Seq(FormError("nino", invalidKey))
      }
    }
  }
}
