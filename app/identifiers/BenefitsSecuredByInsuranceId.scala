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

package identifiers

import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case object BenefitsSecuredByInsuranceId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "securedBenefits"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) => userAnswers.removeAllOf(List(InsuranceCompanyNameId, InsurancePolicyNumberId,
        InsurerEnterPostCodeId, InsurerSelectAddressId, InsurerConfirmAddressId))
      case _ => super.cleanup(value, userAnswers)
    }
  }

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[self.type] = {

    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        BooleanCYA[self.type]()().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(_) => row(id)(changeUrl, userAnswers)
          case _ => Seq.empty[AnswerRow]
        }
      }
    }
  }
}
