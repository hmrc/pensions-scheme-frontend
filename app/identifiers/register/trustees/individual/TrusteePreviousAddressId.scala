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

package identifiers.register.trustees.individual

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteePreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ TrusteePreviousAddressId.toString
}

object TrusteePreviousAddressId {
  override def toString: String = "trusteePreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua: UserAnswers): CheckYourAnswers[TrusteePreviousAddressId] = {
    def trusteeName(index: Int): String = ua.get(TrusteeNameId(index)).fold(messages("messages__theTrustee"))(_.fullName)
    def label(index: Int) = messages("messages__trusteePreviousAddress", trusteeName(index))
    def changeAddress(index: Int) = messages("messages__changeTrusteePreviousAddress", trusteeName(index))
    def addAddress(index: Int) = messages("messages__addTrusteePreviousAddress", trusteeName(index))

    new CheckYourAnswers[TrusteePreviousAddressId] {
      override def row(id: TrusteePreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label(id.index), changeAddress(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteePreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label(id.index),
          changeAddress(id.index),
          userAnswers.get(IsTrusteeNewId(id.index)),
          userAnswers.get(TrusteeAddressYearsId(id.index)),
          Some(addAddress(id.index)))().updateRow(id)(changeUrl, userAnswers)
    }
  }
}
