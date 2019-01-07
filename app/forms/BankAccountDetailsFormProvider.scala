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

import forms.mappings.BankDetailsMapping
import javax.inject.Inject
import models.register.UKBankDetails
import play.api.data.Form
import play.api.data.Forms._

class BankAccountDetailsFormProvider @Inject() extends BankDetailsMapping {

  val nameMaxLength = 28
  val accountNoMaxLength = 8

  def apply(): Form[UKBankDetails] = Form(
    mapping(
      "bankName" ->
        text("messages__error__bank_name").
          verifying(maxLength(nameMaxLength, "messages__error__bank_name_length")),
      "accountName" ->
        text("messages__error__account_name").
          verifying(maxLength(nameMaxLength, "messages__error__account_name_length")),
      "sortCode" ->
        sortCodeMapping("messages__error__sort_code",
          "messages__error__sort_code_invalid", "messages__error__sort_code_length"),
      "accountNumber" ->
        text("messages__error__account_number").
          verifying(returnOnFirstFailure(regexp(regexAccountNo, "messages__error__account_number_invalid"),
            maxLength(accountNoMaxLength, "messages__error__account_number_length"))),
      "date" ->
        dateMapping("messages__error__date", "error.invalid_date")
          .verifying(futureDate("messages__error__date_future"))
    )(UKBankDetails.apply)(UKBankDetails.unapply)
  )
}
