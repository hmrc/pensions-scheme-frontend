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

package identifiers.register.establishers.partnership

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.Index
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipPreviousAddressId.toString
}

object PartnershipPreviousAddressId {
  override def toString: String = "partnershipPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages): CheckYourAnswers[PartnershipPreviousAddressId] = {

    def establisherName(index: Index, ua: UserAnswers) = ua.get(PartnershipDetailsId(index)).fold(messages("messages__theEstablisher"))(_.name)

    def label(index: Int, ua: UserAnswers) = messages("messages__previousAddress__cya", establisherName(index, ua))

    def changeAddress(index: Int, ua: UserAnswers) = messages("messages__visuallyhidden__dynamic_previousAddress", establisherName(index, ua))

    new CheckYourAnswers[PartnershipPreviousAddressId] {
      override def row(id: PartnershipPreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label(id.index, ua), changeAddress(id.index, ua))().row(id)(changeUrl, ua)

      override def updateRow(id: PartnershipPreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        PreviousAddressCYA(label(id.index, ua),
          changeAddress(id.index, ua),
          ua.get(IsEstablisherNewId(id.index)),
          ua.get(PartnershipConfirmPreviousAddressId(id.index))
        )().updateRow(id)(changeUrl, ua)
    }
  }
}
