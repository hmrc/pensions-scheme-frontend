/*
 * Copyright 2020 HM Revenue & Customs
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

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersPartners}
import viewmodels.AnswerRow

case class PartnerAddressYearsId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.remove(PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex))
          .flatMap(_.remove(PartnerPreviousAddressId(establisherIndex, partnerIndex)))
          .flatMap(_.remove(PartnerPreviousAddressListId(establisherIndex, partnerIndex)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnerAddressYearsId {
  override lazy val toString: String = "partnerAddressYears"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerAddressYearsId] = {

    new CheckYourAnswersPartners[PartnerAddressYearsId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__hasBeen1Year")


      private def hiddenLabel(establisherIndex: Int, partnerIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_addressYears")

      override def row(id: PartnerAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        AddressYearsCYA(label(id.establisherIndex, id.partnerIndex, userAnswers), hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerAddressYearsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) =>
            AddressYearsCYA(label(id.establisherIndex, id.partnerIndex, userAnswers), hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers))()
              .row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA(label(id.establisherIndex, id.partnerIndex, userAnswers), hiddenLabel(id.establisherIndex, id.partnerIndex, userAnswers))()
              .updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}

