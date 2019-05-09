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
import models.Nino
import play.api.libs.json.{JsPath, Reads}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, NinoCYA}
import viewmodels.AnswerRow

case class PartnerNinoId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerNinoId.toString
}

object PartnerNinoId {
  override lazy val toString: String = "partnerNino"

  implicit val nino: CheckYourAnswers[PartnerNinoId] = {
    val label = "messages__partner_nino_question_cya_label"
    val reasonLabel = "messages__partner_nino_reason_cya_label"
    val changeHasNino = "messages__visuallyhidden__partner__nino_yes_no"
    val changeNino = "messages__visuallyhidden__partner__nino"
    val changeNoNino = "messages__visuallyhidden__partner__nino_no"

    new CheckYourAnswers[PartnerNinoId] {
      override def row(id: PartnerNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        NinoCYA[PartnerNinoId](label, reasonLabel, changeHasNino, changeNino, changeNoNino)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => NinoCYA[PartnerNinoId](label, reasonLabel, changeHasNino, changeNino, changeNoNino)().row(id)(changeUrl, userAnswers)
          case _ => NinoCYA[PartnerNinoId](label, reasonLabel, changeHasNino, changeNino, changeNoNino)().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
