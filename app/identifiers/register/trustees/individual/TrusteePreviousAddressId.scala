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

package identifiers.register.trustees.individual

import config.FeatureSwitchManagementService
import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers, PreviousAddressCYA}
import utils.{CountryOptions, Toggles, UserAnswers}
import viewmodels.AnswerRow

case class TrusteePreviousAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ TrusteePreviousAddressId.toString
}

object TrusteePreviousAddressId {
  override def toString: String = "trusteePreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages,
                   featureSwitchManagementService: FeatureSwitchManagementService): CheckYourAnswers[TrusteePreviousAddressId] = {

    def getLabel(index: Int, ua: UserAnswers): (String, String) = {
      val name = if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled))
        ua.get(TrusteeNameId(index)).map(_.fullName)
      else
        ua.get(TrusteeDetailsId(index)).map(_.fullName)
      (messages("messages__trusteePreviousAddress", name.getOrElse(messages("messages__theTrustee"))),
        messages("messages__visuallyhidden__dynamic_previousAddress", name.getOrElse(messages("messages__theTrustee"))))
    }

    new CheckYourAnswers[TrusteePreviousAddressId] {
      override def row(id: TrusteePreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, changeAddress) = getLabel(id.index, ua)
        AddressCYA(label, changeAddress)().row(id)(changeUrl, ua)
      }

      override def updateRow(id: TrusteePreviousAddressId)(changeUrl: String, ua: UserAnswers): Seq[AnswerRow] = {
        val (label, changeAddress) = getLabel(id.index, ua)
        PreviousAddressCYA(label,
          changeAddress,
          ua.get(IsTrusteeNewId(id.index)),
          ua.get(IndividualConfirmPreviousAddressId(id.index))
        )().updateRow(id)(changeUrl, ua)
      }
    }
  }
}
