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

package identifiers.register.establishers.individual

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.UniqueTaxReference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class UniqueTaxReferenceId(index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(index).path \ UniqueTaxReferenceId.toString
}

object UniqueTaxReferenceId {
  override def toString: String = "uniqueTaxReference"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[UniqueTaxReferenceId] = {
    new CheckYourAnswers[UniqueTaxReferenceId] {
      override def row(id: UniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        UniqueTaxReferenceCYA()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: UniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        UniqueTaxReferenceCYA(isNew = userAnswers.get(IsEstablisherNewId(id.index)))()
          .updateRow(id)(changeUrl, userAnswers)
    }
  }
}
