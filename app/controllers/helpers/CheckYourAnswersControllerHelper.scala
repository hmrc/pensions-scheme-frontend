/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.helpers

import identifiers.TypedIdentifier
import models._
import models.person.PersonName
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import utils.UserAnswers
import viewmodels.Message

object CheckYourAnswersControllerHelper {

  def personName(id: TypedIdentifier[PersonName])(implicit request: DataRequest[AnyContent],
                                                  messages: Messages, reads: Reads[PersonName]): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.fullName
      case _ => Message("messages__thePerson")
    }

  def companyName(id: TypedIdentifier[CompanyDetails])(implicit request: DataRequest[AnyContent],
                                                       messages: Messages): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.companyName
      case _ => Message("messages__theCompany")
    }

  def partnershipName(id: TypedIdentifier[PartnershipDetails])(implicit request: DataRequest[AnyContent],
                                                               messages: Messages): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.name
      case _ => Message("messages__thePartnership")
    }

  def headingDetails(mode: Mode, name: => String, isNew: Boolean): Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)

  def headingAddressDetails(mode: Mode, name: => String, isNew: Boolean): Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)

  def headingContactDetails(mode: Mode, name: => String, isNew: Boolean): Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)

  def isNewItem(mode: Mode, ua: UserAnswers, id: TypedIdentifier[Boolean]): Boolean =
    mode match {
      case NormalMode => true
      case CheckMode => true
      case _ => ua.get(id).getOrElse(false)
    }
}
