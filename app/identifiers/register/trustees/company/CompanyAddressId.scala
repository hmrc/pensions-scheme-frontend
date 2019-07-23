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
import identifiers.register.trustees.TrusteesId
import models.address.Address
import play.api.i18n.Messages
import play.api.libs.json._
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{AddressCYA, CheckYourAnswers}

case class CompanyAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ CompanyAddressId.toString
}

object CompanyAddressId {
  override def toString: String = "companyAddress"

  implicit def cya(implicit countryOptions: CountryOptions, messages: Messages, ua:UserAnswers): CheckYourAnswers[CompanyAddressId] = {

//    def label(index: Int) = ua.get(CompanyDetailsId(index)) match {
//      case Some(details) => messages("messages__visuallyhidden__trustee__address", details.companyName)
//      case _ => "messages__checkYourAnswers__trustees__company__address_years"
//    }
//
//    def changeAddressYears(index: Int) = ua.get(CompanyDetailsId(index)) match {
//      case Some(details) => messages("messages__visuallyhidden__trustee__address", details.companyName)
//      case _ => messages("messages__visuallyhidden__trustee__address_years")
//    }

//    AddressCYA(
//      label(id.index), changeAddressYears(id.index)
//    )()

    AddressCYA(
      label = messages("messages__visuallyhidden__trustee__address", "aaa"),
      changeAddress = messages("", "aa")
    )()
  }
}
