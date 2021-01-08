/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersCompany}
import viewmodels.{AnswerRow, Message}

case class HasCompanyPAYEId(index: Int) extends TypedIdentifier[Boolean] {
  override def path: JsPath = EstablishersId(index).path \ HasCompanyPAYEId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(CompanyEnterPAYEId(this.index))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}

object HasCompanyPAYEId {
  override def toString: String = "hasPaye"

  implicit def cya: CheckYourAnswers[HasCompanyPAYEId] = {

    new CheckYourAnswersCompany[HasCompanyPAYEId] {

      private def label(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__hasPAYE")

      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(index, ua, "messages__visuallyhidden__dynamic_hasPaye")

      override def row(id: HasCompanyPAYEId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA(Some(label(id.index, userAnswers)),
          Some(hiddenLabel(id.index, userAnswers)))()
          .row(id)(changeUrl, userAnswers)

      override def updateRow(id: HasCompanyPAYEId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}




