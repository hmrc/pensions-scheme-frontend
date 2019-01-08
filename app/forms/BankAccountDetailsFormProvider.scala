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
import models.BankAccountDetails
import models.register.SortCode
import play.api.data.Forms._
import play.api.data.{Form, Mapping}

class BankAccountDetailsFormProvider @Inject() extends BankDetailsMapping {
  override protected def sortCodeMapping(requiredKey: String, lengthKey: String, invalidKey: String): Mapping[SortCode] = {
    tuple(
      "first" -> text,
      "second" -> text,
      "third" -> text
    )
      .verifying(
        returnOnFirstFailure(
          sortCodeRequiredConstraint(requiredKey), sortCodeLengthConstraint(lengthKey), sortCodeInvalidConstraint(invalidKey)
        )
      )
      .transform(
        input => SortCode(input._1, input._2, input._3),
        sortCode => (sortCode.first, sortCode.second, sortCode.third))
  }

  protected val nameMaxLength = 28
  protected val accountNoExactLength = 8

  def apply(): Form[BankAccountDetails] = Form(
    mapping(
      "bankName" ->
        text("messages__error__bank_name__blank").
          verifying(maxLength(nameMaxLength, "messages__error__bank_name__length")),
      "accountName" ->
        text("messages__error__bank_account_holder_name__blank").
          verifying(maxLength(nameMaxLength, "messages__error__bank_account_holder_name__length")),
      "sortCode" ->
        sortCodeMapping("messages__error__sort_code__blank",
          "messages__error__sort_code__length",
          "messages__error__sort_code__invalid"),
      "accountNumber" ->
        text("messages__error__bank_accno__blank").
          verifying(returnOnFirstFailure(regexp(regexAccountNo, "messages__error__bank_accno__invalid"),
            exactLength(accountNoExactLength, "messages__error__bank_accno__length")))
    )(BankAccountDetails.apply)(BankAccountDetails.unapply)
  )
}
