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

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}
import viewmodels.{AnswerRow, Message}

case class CompanyAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = EstablishersId(index).path \ CompanyAddressId.toString
}

object CompanyAddressId {
  override def toString: String = "companyAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages): CheckYourAnswers[CompanyAddressId] =
    new CheckYourAnswers[CompanyAddressId] {

      override def row(id: CompanyAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val label = ua.get(CompanyDetailsId(id.index)).map(details => Message("messages__establisherConfirmAddress__cya_label", details.companyName)).
          getOrElse(Message("messages__common__cya__address"))
        AddressCYA(label, "messages__establisherConfirmAddress__cya_visually_hidden_label")().row(id)(changeUrl, ua)
      }

      override def updateRow(id: CompanyAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, ua)
    }
}
