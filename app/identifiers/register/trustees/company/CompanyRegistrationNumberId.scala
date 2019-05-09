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

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.CompanyRegistrationNumber
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CompanyRegistrationNumberCYA}
import viewmodels.AnswerRow

case class CompanyRegistrationNumberId(index: Int) extends TypedIdentifier[CompanyRegistrationNumber] {
  override def path: JsPath = TrusteesId(index).path \ CompanyRegistrationNumberId.toString
}

object CompanyRegistrationNumberId {
  override def toString: String = "companyRegistrationNumber"

  implicit val cya: CheckYourAnswers[CompanyRegistrationNumberId] = {
    val label: String = "messages__checkYourAnswers__trustees__company__crn"
    val reasonLabel: String = "messages__checkYourAnswers__trustees__company__crn_no_reason"
    val changeHasCrn: String = "messages__visuallyhidden__trustee__crn_yes_no"
    val changeCrn: String = "messages__visuallyhidden__trustee__crn"
    val changeNoCrn: String = "messages__visuallyhidden__trustee__crn_no"

    new CheckYourAnswers[CompanyRegistrationNumberId] {
      override def row(id: CompanyRegistrationNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        CompanyRegistrationNumberCYA(label, reasonLabel, changeHasCrn, changeCrn, changeNoCrn)().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyRegistrationNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => CompanyRegistrationNumberCYA(label, reasonLabel, changeHasCrn, changeCrn, changeNoCrn)().row(id)(changeUrl, userAnswers)
          case _ => CompanyRegistrationNumberCYA(label, reasonLabel, changeHasCrn, changeCrn, changeNoCrn)().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
