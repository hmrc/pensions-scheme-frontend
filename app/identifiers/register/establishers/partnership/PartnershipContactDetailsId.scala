/*
 * Copyright 2018 HM Revenue & Customs
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

package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.ContactDetails
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.ContactDetailsCYA

case class PartnershipContactDetailsId(index: Int) extends TypedIdentifier[ContactDetails] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipContactDetailsId.toString
}

object PartnershipContactDetailsId {
  override def toString: String = "partnershipContactDetails"

  implicit val cya: CheckYourAnswers[PartnershipContactDetailsId] =
    ContactDetailsCYA("messages__visuallyhidden__partnership__email_address",
                      "messages__visuallyhidden__partnership__phone_number")()
}

