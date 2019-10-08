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

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.Link
import org.joda.time.LocalDate
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{DateHelper, UserAnswers}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartners}
import viewmodels.AnswerRow

case class PartnerDOBId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerDOBId.toString
}

object PartnerDOBId {
  override def toString: String = "dateOfBirth"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerDOBId] = {
    new CheckYourAnswersPartners[PartnerDOBId] {

      private def label(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__DOB__heading")

      private def hiddenText(establisherIndex: Int, partnerIndex: Int, ua:UserAnswers):String =
        dynamicMessage(establisherIndex, partnerIndex, ua, "messages__visuallyhidden__dynamic_date_of_birth")

      override def row(id: PartnerDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {

        userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob => {
          Seq(
            AnswerRow(
              label(id.establisherIndex, id.partnerIndex, userAnswers),
              Seq(DateHelper.formatDate(dob)),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl, Some(hiddenText(id.establisherIndex, id.partnerIndex, userAnswers))))
            )
          )
        }
        }
      }

      override def updateRow(id: PartnerDOBId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id).fold(Nil: Seq[AnswerRow]) { dob =>
              Seq(
                AnswerRow(
                  label(id.establisherIndex, id.partnerIndex, userAnswers),
                  Seq(DateHelper.formatDate(dob)),
                  answerIsMessageKey = false,
                  None
                )
              )
            }
        }
    }
  }
}

