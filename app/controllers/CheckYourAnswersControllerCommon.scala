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
import models.Mode
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import viewmodels.Message

trait CheckYourAnswersControllerCommon extends FrontendController {

  private def establisherCompanyName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.company.CompanyDetailsId(index)).map(_.companyName).getOrElse(Message("messages__theCompany").resolve)

  private def establisherPartnershipName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.partnership.PartnershipDetailsId(index)).map(_.name).getOrElse(Message("messages__thePartnership").resolve)

  private def establisherIndividualName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.establishers.individual.EstablisherNameId(index)).map(_.fullName).getOrElse(Message("messages__theIndividual").resolve)

  private def trusteeCompanyName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.company.CompanyDetailsId(index)).map(_.companyName).getOrElse(Message("messages__theCompany").resolve)

  private def trusteePartnershipName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.partnership.PartnershipDetailsId(index)).map(_.name).getOrElse(Message("messages__thePartnership").resolve)

  private def trusteeIndividualName(index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):String =
    request.userAnswers.get(identifiers.register.trustees.individual.TrusteeNameId(index)).map(_.fullName).getOrElse(Message("messages__theIndividual").resolve)

  def titleCompanyDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

  def titleCompanyContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theCompany").resolve)

  def titleCompanyAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theCompany").resolve)



  def titleIndividualDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theIndividual").resolve)

  def titleIndividualContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theIndividual").resolve)

  def titleIndividualAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theIndividual").resolve)



  def titlePartnershipDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePartnership").resolve)
  
  
  private def headingDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)
  }

  private def headingAddressDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)
  }

  private def headingContactDetails(mode:Mode, name: => String)(implicit messages:Messages):Message = {
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)
  }

  def headingEstablisherCompanyDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, establisherCompanyName(index))

  def headingEstablisherCompanyContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, establisherCompanyName(index))

  def headingEstablisherCompanyAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, establisherCompanyName(index))

  def headingEstablisherPartnershipDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, establisherPartnershipName(index))

  def headingEstablisherPartnershipContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, establisherPartnershipName(index))

  def headingEstablisherPartnershipAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, establisherPartnershipName(index))

  def headingEstablisherIndividualDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, establisherIndividualName(index))

  def headingEstablisherIndividualContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, establisherIndividualName(index))

  def headingEstablisherIndividualAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, establisherIndividualName(index))


  def headingTrusteeCompanyDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, trusteeCompanyName(index))

  def headingTrusteeCompanyContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, trusteeCompanyName(index))

  def headingTrusteeCompanyAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, trusteeCompanyName(index))

  def headingTrusteePartnershipDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, trusteePartnershipName(index))

  def headingTrusteePartnershipContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, trusteePartnershipName(index))

  def headingTrusteePartnershipAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, trusteePartnershipName(index))

  def headingTrusteeIndividualDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingDetails(mode, trusteeIndividualName(index))

  def headingTrusteeIndividualContactDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingContactDetails(mode, trusteeIndividualName(index))

  def headingTrusteeIndividualAddressDetails(mode:Mode, index:Int)(implicit request: DataRequest[AnyContent], messages:Messages):Message = headingAddressDetails(mode, trusteeIndividualName(index))


}
