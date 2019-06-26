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
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BoolAnswerStringCYA
import viewmodels.AnswerRow

case class NoCompanyUTRId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ "companyUniqueTaxReference" \ NoCompanyUTRId.toString
}

object NoCompanyUTRId {
  override def toString: String = "reason"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages): CheckYourAnswers[NoCompanyUTRId] = {

    def label(index: Int) = userAnswers.get(CompanyDetailsId(index)) match {
      case Some(name) => messages("messages__vatVariations__heading", name)
      case _ => messages("messages__vatVariations__company_title")
    }

    new CheckYourAnswers[NoCompanyUTRId] {
      override def row(id: NoCompanyUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(HasCompanyUTRId(id.index)) match {
          case Some(bool: Boolean) => BoolAnswerStringCYA(label(id.index), Some(label(id.index)), !bool)().row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }

      override def updateRow(id: NoCompanyUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
    }
  }
}




