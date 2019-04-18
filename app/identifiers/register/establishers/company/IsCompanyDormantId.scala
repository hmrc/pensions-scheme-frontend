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

package identifiers.register.establishers.company

import identifiers._
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.Link
import models.register.DeclarationDormant
import models.register.DeclarationDormant._
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, IsDormantCYA}
import viewmodels.AnswerRow

case class IsCompanyDormantId(index: Int) extends TypedIdentifier[DeclarationDormant] {
  override def path: JsPath = EstablishersId(index).path \ IsCompanyDormantId.toString
}

object IsCompanyDormantId {

  override def toString: String = "isCompanyDormant"

  val label: String = "messages__company__cya__dormant"
  val changeIsDormant: String = "messages__visuallyhidden__establisher__dormant"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[IsCompanyDormantId] = {

    new CheckYourAnswers[IsCompanyDormantId] {

      override def row(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        IsDormantCYA()().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(DeclarationDormant.Yes) =>
            userAnswers.get(IsEstablisherNewId(id.index)) match {
              case Some(true) =>  Seq(AnswerRow(label, Seq("site.yes"), answerIsMessageKey = true,
                Some(Link("site.change", changeUrl, Some(changeIsDormant)))))
              case _  =>  Seq(AnswerRow(label, Seq("site.yes"), answerIsMessageKey = true,
                Some(Link("site.change", changeUrl, None))))
            }
          case Some(DeclarationDormant.No) => Seq(AnswerRow(label, Seq("site.no"), answerIsMessageKey = true,
            Some(Link("site.change", changeUrl, Some(changeIsDormant)))))

          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}
