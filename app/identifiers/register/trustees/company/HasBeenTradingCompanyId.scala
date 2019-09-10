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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class HasBeenTradingCompanyId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = TrusteesId(index).path \ HasBeenTradingCompanyId.toString

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

  implicit def cya(implicit messages: Messages): CheckYourAnswers[HasBeenTradingCompanyId] =
    new CheckYourAnswers[HasBeenTradingCompanyId] {

      override def row(id: HasBeenTradingCompanyId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val trusteeName = ua.get(CompanyDetailsId(id.index)).fold(messages("messages__theTrustee"))(_.companyName)
        val label = messages("messages__hasBeenTrading__h1", trusteeName)
        val hiddenLabel = messages("messages__visuallyhidden__dynamic__hasBeenTrading", trusteeName)

        BooleanCYA(Some(label), Some(hiddenLabel))().row(id)(changeUrl, ua)
      }

      override def updateRow(id: HasBeenTradingCompanyId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] =
        ua.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, ua)
          case _ => Seq.empty[AnswerRow]
        }
    }
}





