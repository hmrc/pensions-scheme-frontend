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
import models.Vat
import play.api.libs.json.{JsPath, Reads}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import viewmodels.AnswerRow

case class PartnershipVatId(index: Int) extends TypedIdentifier[Vat] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipVatId.toString
}

object PartnershipVatId {
  override def toString: String = "partnershipVat"

  implicit def vat[I <: TypedIdentifier[Vat]](implicit r: Reads[Vat]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map{
          case Vat.Yes(vat) => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__vat",
              Seq("site.yes"),
              true,
              changeUrl
            ),
            AnswerRow(
              "messages__common__cya__vat",
              Seq(vat),
              false,
              changeUrl
            )
          )
          case Vat.No => Seq(
            AnswerRow(
              "messages__partnership__checkYourAnswers__vat",
              Seq("site.no"),
              true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

}


