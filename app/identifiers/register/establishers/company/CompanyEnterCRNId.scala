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
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyEnterCRNId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = EstablishersId(index).path \ CompanyEnterCRNId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(CompanyNoCRNReasonId(this.index))
}

object CompanyEnterCRNId {
  override def toString: String = "companyRegistrationNumber"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyEnterCRNId] = {

    val label: String = "messages__checkYourAnswers__establishers__company__number"
    val changeCrn: String = "messages__visuallyhidden__companyNumber"

    new CheckYourAnswers[CompanyEnterCRNId] {
      override def row(id: CompanyEnterCRNId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[CompanyEnterCRNId](label, changeCrn)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyEnterCRNId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) =>
            row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[CompanyEnterCRNId](label, changeCrn)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
