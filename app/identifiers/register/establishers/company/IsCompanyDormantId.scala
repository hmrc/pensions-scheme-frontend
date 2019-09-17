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

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.Link
import models.register.DeclarationDormant
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import viewmodels.AnswerRow

case class IsCompanyDormantId(index: Int) extends TypedIdentifier[DeclarationDormant] {
  override def path: JsPath = EstablishersId(index).path \ IsCompanyDormantId.toString
}

object IsCompanyDormantId {
  override def toString: String = "isCompanyDormant"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[IsCompanyDormantId] = {
    new CheckYourAnswersCompany[IsCompanyDormantId] {

      private def label(index: Int, ua: UserAnswers): String =
        dynamicMessage(index, ua, "messages__company__cya__dormant")

      private def hiddenLabel(index: Int, ua: UserAnswers): String =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_company__dormant")

      override def row(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(DeclarationDormant.Yes) =>
            Seq(
              AnswerRow(
                label(id.index, userAnswers),
                Seq("site.yes"),
                answerIsMessageKey = true,
                Some(Link("site.change", changeUrl, Some(hiddenLabel(id.index, userAnswers))))
              )
            )
          case Some(DeclarationDormant.No) =>
            Seq(
              AnswerRow(
                label(id.index, userAnswers),
                Seq("site.no"),
                answerIsMessageKey = true,
                Some(Link("site.change", changeUrl, Some(hiddenLabel(id.index, userAnswers))))
              ))
          case _ => Seq.empty[AnswerRow]
        }

      override def updateRow(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = Nil
    }
  }
}
