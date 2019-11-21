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
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director.IsNewDirectorId
import identifiers.register.establishers.partnership.partner.IsNewPartnerId
import identifiers.register.trustees.IsTrusteeNewId
import models.Mode
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers
import viewmodels.Message

trait CheckYourAnswersControllerCommon extends FrontendController {

  def establisherCompanyDirectorName(companyIndex: Int, directorIndex: Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.company.director.DirectorNameId(companyIndex, directorIndex)).map(_.fullName).getOrElse(Message("messages__thePerson").resolve)

  def establisherPartnershipPartnerName(partnershipIndex: Int, partnerIndex: Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.partnership.partner.PartnerNameId(partnershipIndex, partnerIndex)).map(_.fullName).getOrElse(Message("messages__thePerson").resolve)

  def establisherCompanyName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.company.CompanyDetailsId(index)).map(_.companyName).getOrElse(Message("messages__theCompany").resolve)

  def establisherPartnershipName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.partnership.PartnershipDetailsId(index)).map(_.name).getOrElse(Message("messages__thePartnership").resolve)

  def establisherIndividualName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.individual.EstablisherNameId(index)).map(_.fullName).getOrElse(Message("messages__thePerson").resolve)

  def trusteeCompanyName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.company.CompanyDetailsId(index)).map(_.companyName).getOrElse(Message("messages__theCompany").resolve)

  def trusteePartnershipName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.partnership.PartnershipDetailsId(index)).map(_.name).getOrElse(Message("messages__thePartnership").resolve)

  def trusteeIndividualName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.individual.TrusteeNameId(index)).map(_.fullName).getOrElse(Message("messages__thePerson").resolve)

  def titleCompanyDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

  def titleCompanyContactDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theCompany").resolve)

  def titleCompanyAddressDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theCompany").resolve)

  def titleIndividualDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePerson").resolve)

  def titleIndividualContactDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePerson").resolve)

  def titleIndividualAddressDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePerson").resolve)

  def titlePartnershipDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipContactDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipAddressDetails(mode:Mode, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePartnership").resolve)

  def headingDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)

  def headingAddressDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)

  def headingContactDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)

  def headingEstablisherCompanyDirectorOrPartnerDetails(mode:Mode, name: => String, isNew: => Boolean)(implicit messages:Messages):Message =
    if (isNew) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)

  def isNewEstablisher(mode:Mode, ua:UserAnswers, index:Int):Boolean = mode.isRegistrationJourney || ua.get(IsEstablisherNewId(index)).getOrElse(false)

  def isNewEstablisherCompanyDirector(mode:Mode, ua:UserAnswers, establisherIndex:Int, directorIndex:Int):Boolean =
    mode.isRegistrationJourney || ua.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)

  def isNewEstablisherPartnershipPartner(mode:Mode, ua:UserAnswers, establisherIndex:Int, partnerIndex:Int):Boolean =
    mode.isRegistrationJourney || ua.get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false)

  def isNewTrustee(mode:Mode, ua:UserAnswers, index:Int):Boolean = mode.isRegistrationJourney || ua.get(IsTrusteeNewId(index)).getOrElse(false)
}
