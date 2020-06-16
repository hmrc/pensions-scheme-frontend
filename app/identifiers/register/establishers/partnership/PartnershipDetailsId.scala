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

package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.PartnershipDetails
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.PartnershipDetailsCYA
import viewmodels.AnswerRow

case class PartnershipDetailsId(index: Int) extends TypedIdentifier[PartnershipDetails] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipDetailsId.toString
}

object PartnershipDetailsId {

  override lazy val toString: String = "partnershipDetails"

  implicit def cya: CheckYourAnswers[PartnershipDetailsId] = {

    new CheckYourAnswers[PartnershipDetailsId] {

      override def row(id: PartnershipDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PartnershipDetailsCYA()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnershipDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => PartnershipDetailsCYA()().row(id)(changeUrl, userAnswers)
          case _ => PartnershipDetailsCYA()().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}


