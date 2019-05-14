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

package identifiers.register.establishers.partnership.partner

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.AddressYears.UnderAYear
import models.Link
import models.address.Address
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import viewmodels.AnswerRow

case class PartnerPreviousAddressId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerPreviousAddressId.toString
}

object PartnerPreviousAddressId {
  override def toString: String = "partnerPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnerPreviousAddressId] = {
    val label: String = "messages__common__cya__previous_address"
    val changeAddress: String = "messages__visuallyhidden__partner__previous_address"

    new CheckYourAnswers[PartnerPreviousAddressId] {
      override def row(id: PartnerPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) =>
            AddressCYA(label, changeAddress)().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(_) => row(id)(changeUrl, userAnswers)
              case _ =>
                userAnswers.get(PartnerAddressYearsId(id.establisherIndex, id.partnerIndex)) match {
                  case Some(UnderAYear) => Seq(AnswerRow(label,
                    Seq("site.not_entered"),
                    answerIsMessageKey = true,
                    Some(Link("site.add", changeUrl, Some("messages__visuallyhidden__partner__previous_address_add")))))
                  case _ => Seq.empty[AnswerRow]
                }
            }
        }
    }
  }
}
