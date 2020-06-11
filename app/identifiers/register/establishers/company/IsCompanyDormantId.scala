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

package identifiers.register.establishers.company

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.register.DeclarationDormant
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswersCompany, IsDormantCYA}
import viewmodels.{AnswerRow, Message}

case class IsCompanyDormantId(index: Int) extends TypedIdentifier[DeclarationDormant] {
  override def path: JsPath = EstablishersId(index).path \ IsCompanyDormantId.toString
}

object IsCompanyDormantId {
  override def toString: String = "isCompanyDormant"

  implicit def cya(implicit userAnswers: UserAnswers): CheckYourAnswersCompany[IsCompanyDormantId] = {
    new CheckYourAnswersCompany[IsCompanyDormantId] {

      override def row(id: IsCompanyDormantId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(id.index, ua, messageKey = "messages__company__cya__dormant")

        def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(id.index, ua, messageKey = "messages__visuallyhidden__dynamic_company__dormant")

        IsDormantCYA(label(id.index, userAnswers), hiddenLabel(id.index, userAnswers))()
          .row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = Nil
    }
  }
}
