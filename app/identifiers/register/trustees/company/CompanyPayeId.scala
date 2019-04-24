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
import models.Paye
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.PayeCYA
import viewmodels.AnswerRow

case class CompanyPayeId(index: Int) extends TypedIdentifier[Paye] {
  override def path: JsPath = TrusteesId(index).path \ CompanyPayeId.toString
}

object CompanyPayeId {
  override def toString: String = "companyPaye"

  val labelYesNo = "messages__checkYourAnswers__trustees__company__paye"
  val hiddenLabelYesNo = "messages__visuallyhidden__trustee__paye_yes_no"
  val hiddenLabelPaye = "messages__visuallyhidden__trustee__paye_number"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[CompanyPayeId] = {
    new CheckYourAnswers[CompanyPayeId] {

      override def row(id: CompanyPayeId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PayeCYA(Some(labelYesNo), hiddenLabelYesNo, hiddenLabelPaye)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyPayeId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PayeCYA(Some(labelYesNo), hiddenLabelYesNo, hiddenLabelPaye, isNew = userAnswers.get(IsTrusteeNewId(id.index)))()
          .updateRow(id)(changeUrl, userAnswers)
    }
  }
}


