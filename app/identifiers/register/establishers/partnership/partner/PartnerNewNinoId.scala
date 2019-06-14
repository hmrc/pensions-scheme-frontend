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
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow


case class PartnerNewNinoId(establisherIndex: Int, partnerIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "partner" \ partnerIndex \ PartnerNinoId.toString \ "nino"
}

object PartnerNewNinoId {
  override def toString: String = "partnerNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[PartnerNewNinoId] = {

    new CheckYourAnswers[PartnerNewNinoId] {

      override def row(id: PartnerNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA[PartnerNewNinoId]()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: PartnerNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsNewPartnerId(id.establisherIndex, id.partnerIndex)) match {
          case Some(true) => StringCYA[PartnerNewNinoId]()().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(nino) => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false, None))
              case _ => Seq(AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
                Some(Link("site.add", changeUrl, Some(s"messages__visuallyhidden__partner__nino_add")))))
            }
        }
      }
    }
  }
}




