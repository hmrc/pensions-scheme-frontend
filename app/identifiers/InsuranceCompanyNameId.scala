/*
 * Copyright 2022 HM Revenue & Customs
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

import models.Link
import play.api.libs.json.JsResult
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Message}

case object InsuranceCompanyNameId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "insuranceCompanyName"

  override def cleanup(value: Option[String], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(_) => userAnswers.removeAllOf(
        List(
          InsurancePolicyNumberId,
          InsurerEnterPostCodeId,
          InsurerSelectAddressId,
          InsurerConfirmAddressId
        )
      )
      case _ => super.cleanup(value, userAnswers)
    }
  }

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[self.type] = {

    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        StringCYA[self.type]()().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(_) => row(id)(changeUrl, userAnswers)
          case _ => userAnswers.get(BenefitsSecuredByInsuranceId) match {
            case Some(true) => Seq(AnswerRow(
              Message("insuranceCompanyName.checkYourAnswersLabel"),
              Seq("site.not_entered"),
              answerIsMessageKey = true,
              Some(Link("site.add", changeUrl, Some(Message("messages__visuallyhidden__insuranceCompanyName"))))))
            case _ => Seq.empty[AnswerRow]
          }
        }
      }
    }
  }
}
