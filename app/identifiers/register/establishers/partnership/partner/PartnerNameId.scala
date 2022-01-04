/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.register.establishers.partnership.OtherPartnersId
import models.Link
import models.person.PersonName
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.{AnswerRow, Message}

case class PartnerNameId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath =
    EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerNameId.toString

  //scalastyle:off magic.number
  override def cleanup(value: Option[PersonName], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.allPartnersAfterDelete(this.establisherIndex).lengthCompare(10) match {
      case lengthCompare if lengthCompare <= 0 => userAnswers.remove(OtherPartnersId(this.establisherIndex))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnerNameId {
  def collectionPath(establisherIndex: Int): JsPath =
    EstablishersId(establisherIndex).path \ "partner" \\ PartnerNameId.toString

  override def toString: String = "partnerDetails"

  implicit def cya: CheckYourAnswers[PartnerNameId] = {
    new CheckYourAnswers[PartnerNameId] {

      override def row(id: PartnerNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails => {
          Seq(
            AnswerRow(
              "messages__partnerName__cya",
              Seq(personDetails.fullName),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__dynamic_name", personDetails.fullName))))
            )
          )
        }
        }

      override def updateRow(id: PartnerNameId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { personDetails =>
              Seq(
                AnswerRow(
                  "messages__partnerName__cya",
                  Seq(personDetails.fullName),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}

