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
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class HasBeenTradingCompanyId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ HasBeenTradingCompanyId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers
          .remove(CompanyPreviousAddressPostcodeLookupId(this.index))
          .flatMap(_.remove(CompanyPreviousAddressId(this.index)))
          .flatMap(_.remove(CompanyPreviousAddressListId(this.index)))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object HasBeenTradingCompanyId {
  override def toString: String = "hasBeenTrading"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[HasBeenTradingCompanyId] = {

    new CheckYourAnswersCompany[HasBeenTradingCompanyId] {

      private def label(index: Int, ua: UserAnswers): String = {
        dynamicMessage(index, ua, "messages__hasBeenTrading__h1")
      }

        private def hiddenLabel(index: Int, ua: UserAnswers): String = {
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic__hasBeenTrading")
      }




      override def row(id: HasBeenTradingCompanyId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        BooleanCYA(Some(label(id.index, ua)), Some(hiddenLabel(id.index, ua)))().row(id)(changeUrl, ua)
      }

      override def updateRow(id: HasBeenTradingCompanyId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _          => Seq.empty[AnswerRow]
        }
    }
  }
  }

