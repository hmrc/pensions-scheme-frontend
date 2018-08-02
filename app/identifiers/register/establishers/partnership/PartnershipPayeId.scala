/*
 * Copyright 2018 HM Revenue & Customs
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
import models.Paye
import play.api.libs.json.{JsPath, Reads}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.AnswerRow

case class PartnershipPayeId(index: Int) extends TypedIdentifier[Paye] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipPayeId.toString
}

object PartnershipPayeId {
  override def toString: String = "partnershipPaye"


  implicit def paye[I <: TypedIdentifier[Paye]](implicit r: Reads[Paye]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map{
          case Paye.Yes(paye) => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__paye",
              Seq("site.yes"),
              true,
              changeUrl
            ),
            AnswerRow(
              "messages__common__cya__paye",
              Seq(paye),
              false,
              changeUrl
            )
          )
          case Paye.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__paye",
              Seq("site.no"),
              true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

}
