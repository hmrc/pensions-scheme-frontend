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

  def titleCompanyDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__theCompany").resolve)

  def titleCompanyContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__theCompany").resolve)

  def titleCompanyAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__theCompany").resolve)

  def titleIndividualDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePerson").resolve)

  def titleIndividualContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePerson").resolve)

  def titleIndividualAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePerson").resolve)

  def titlePartnershipDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__detailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipContactDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__contactDetailsFor", Message("messages__thePartnership").resolve)

  def titlePartnershipAddressDetails(mode:Mode)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.title") else Message("messages__addressFor", Message("messages__thePartnership").resolve)

  def headingDetails(mode:Mode, name: => String)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__detailsFor", name)

  def headingAddressDetails(mode:Mode, name: => String)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__addressFor", name)

  def headingContactDetails(mode:Mode, name: => String)(implicit messages:Messages):Message =
    if (mode.isSubscription) Message("checkYourAnswers.hs.heading") else Message("messages__contactDetailsFor", name)
}
