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

package controllers
import identifiers.TypedIdentifier
import models.person.PersonName
import models.requests.DataRequest
import models.{CompanyDetails, Mode, PartnershipDetails}
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import viewmodels.Message

trait CheckYourAnswersControllerCommon extends FrontendController {

  def personName(id: TypedIdentifier[PersonName])(implicit request: DataRequest[AnyContent], messages: Messages, reads:Reads[PersonName]): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.fullName
      case _ => Message("messages__thePerson").resolve
    }

  def companyName(id: TypedIdentifier[CompanyDetails])(implicit request: DataRequest[AnyContent], messages: Messages, reads:Reads[PersonName]): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.companyName
      case _ => Message("messages__theCompany").resolve
    }

  def partnershipName(id: TypedIdentifier[PartnershipDetails])(implicit request: DataRequest[AnyContent], messages: Messages, reads:Reads[PersonName]): String =
    request.userAnswers.get(id) match {
      case Some(name) => name.name
      case _ => Message("messages__thePartnership").resolve
    }

  def titleCompanyDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

  def titleCompanyContactDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theCompany").resolve)

  def titleCompanyAddressDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theCompany").resolve)

  def titleIndividualDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePerson").resolve)

  def titleIndividualContactDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePerson").resolve)

  def titleIndividualAddressDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePerson").resolve)

  def titlePartnershipDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipContactDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipAddressDetails(mode:Mode, isNew: Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePartnership").resolve)

  def headingDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)

  def headingAddressDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)

  def headingContactDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)

  def isNewItem(mode:Mode, ua:UserAnswers, id: TypedIdentifier[Boolean]):Boolean = mode.isRegistrationJourney || ua.get(id).getOrElse(false)
}
