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
import identifiers.register.establishers.EstablishersId
import models.Link
import models.address.Address
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class PartnershipPreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipPreviousAddressId.toString
}

object PartnershipPreviousAddressId {
  override def toString: String = "partnershipPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[PartnershipPreviousAddressId] =
    new CheckYourAnswers[PartnershipPreviousAddressId] {

      def addressAnswer(address: Address): Seq[String] = {
        val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
        Seq(
          Some(address.addressLine1),
          Some(address.addressLine2),
          address.addressLine3,
          address.addressLine4,
          address.postcode,
          Some(country)
        ).flatten
      }

      def previousAddressRow(userAnswers: UserAnswers, changeLink: Option[Link], id: PartnershipPreviousAddressId): Seq[AnswerRow] = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            "messages__common__cya__previous_address",
            addressAnswer(address),
            answerIsMessageKey = false,
            changeLink
          ))
        }.getOrElse(Seq.empty[AnswerRow])
      }

      override def row(id: PartnershipPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        previousAddressRow(userAnswers, Some(Link("site.change", changeUrl,
          Some("messages__visuallyhidden__partnership__previous_address"))), id)

      override def updateRow(id: PartnershipPreviousAddressId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        previousAddressRow(userAnswers, None, id)
    }
}
