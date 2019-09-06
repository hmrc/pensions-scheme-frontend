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

import models.Link
import models.address.Address
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case object InsurerConfirmAddressId extends TypedIdentifier[Address] {
  self =>
  override def toString: String = "insurerAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[self.type] = {

    val label = "messages__insurer_confirm_address_cya_label"
    val hiddenLabel = "messages__visuallyhidden__insurer_confirm_address"

    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        AddressCYA[self.type](label, hiddenLabel)().row(id)(changeUrl, userAnswers)
      }

      override def updateRow(id: self.type)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(_) => row(id)(changeUrl, userAnswers)
          case _ => userAnswers.get(BenefitsSecuredByInsuranceId) match{
            case Some(true) => Seq(AnswerRow(label,
              Seq("site.not_entered"),
              answerIsMessageKey = true,
              Some(Link("site.add", changeUrl,Some(hiddenLabel)))))
            case _ => Seq.empty[AnswerRow]
          }
        }
      }
    }
  }
}
