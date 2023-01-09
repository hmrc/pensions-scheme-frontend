/*
 * Copyright 2023 HM Revenue & Customs
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

import forms.behaviours.FormBehaviours
import models.MoneyPurchaseBenefits

class MoneyPurchaseBenefitsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] =
    Map("value" -> "01")

  val form = new MoneyPurchaseBenefitsFormProvider()()

  "MoneyPurchaseBenefits Form" must {

    behave like questionForm[MoneyPurchaseBenefits](MoneyPurchaseBenefits.Collective)

    "fail to bind when value is omitted" in {
      val expectedError = error("value", "messages__moneyPurchaseBenefits__error")
      checkForError(form, emptyForm, expectedError)
    }

    "fail to bind when value is invalid" in {
      val expectedError = error("value", "error.invalid")
      checkForError(form, Map("value" -> "invalid"), expectedError)
    }
  }
}
