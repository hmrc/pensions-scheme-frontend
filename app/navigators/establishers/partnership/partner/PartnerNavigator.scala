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

package navigators.establishers.partnership.partner

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.partnership.partner.routes._
import controllers.routes.SessionExpiredController
import identifiers.Identifier
import identifiers.register.establishers.partnership.{AddPartnersId, OtherPartnersId, partner}
import identifiers.register.establishers.partnership.partner._
import models.Mode.journeyMode
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class PartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                 appConfig: FrontendAppConfig) extends AbstractNavigator {

  import PartnerNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case AddPartnersId(estIndex)                                          => addPartnerRoutes(mode, ua, estIndex, srn)
    case PartnerNameId(estIndex, partnerIndex)                            =>           dobPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                             =>           hasNinoPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                      =>
      booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterNINOId(estIndex, partnerIndex)                         =>           hasUtrPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                    =>           hasUtrPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                       =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex)                        =>          postcodeLookupPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                     =>          postcodeLookupPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressPostcodeLookupId(estIndex, partnerIndex)           => addressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressListId(estIndex, partnerIndex)                     => addressYearsPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                         => addressYearsPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                 => partnerAddressYearsRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressPostcodeLookupId(estIndex, partnerIndex)   =>
      paAddressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressListId(estIndex, partnerIndex)             => paAddressPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex)                 => emailPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(estIndex, partnerIndex)                           => phonePage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case ConfirmDeletePartnerId(estIndex)                                 => addPartnerPage(mode, estIndex, srn)
    case OtherPartnersId(_) if mode == NormalMode                         => taskListPage(mode, srn)
    case OtherPartnersId(_)                                               => anyMoreChangesPage(srn)
  }

  private def checkModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnerNameId(estIndex, partnerIndex)                            => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                             => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                      => booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterNINOId(estIndex, partnerIndex)                         => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                    => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                       =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex)                        => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                     => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                         => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                 => partnerAddressYearsEditRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressPostcodeLookupId(estIndex, partnerIndex)   => paAddressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressListId(estIndex, partnerIndex)             => paAddressPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex)                 => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
  }

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnerNameId(estIndex, partnerIndex)                                                  => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                                                   => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                                            =>
      booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterNINOId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)   => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEnterNINOId(_, _)                                               => anyMoreChangesPage(srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                                             =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)  => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEnterUTRId(_, _)                                              => anyMoreChangesPage(srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)   => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                                               => isThisPaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerConfirmPreviousAddressId(estIndex, partnerIndex)                             =>
      booleanNav(id, ua, anyMoreChangesPage(srn), paPostcodeLookupPage(mode, estIndex, partnerIndex, srn))
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                                       => partnerAddressYearsRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressPostcodeLookupId(estIndex, partnerIndex)   => paAddressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressListId(estIndex, partnerIndex)             => paAddressPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(_, _)                                       => anyMoreChangesPage(srn)
    case PartnerEmailId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(_, _)                                                 => anyMoreChangesPage(srn)
    case PartnerPhoneId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(_, _)                                                 => anyMoreChangesPage(srn)
  }

  private def addPartnerRoutes(mode: Mode, ua: UserAnswers, estIndex: Int, srn: Option[String]): Call = {
    (mode, ua.get(AddPartnersId(estIndex))) match {
      case (UpdateMode, Some(false)) =>
        controllers.routes.AnyMoreChangesController.onPageLoad(srn)
      case (NormalMode, Some(false)) =>
        controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)
      case _ if ua.allPartnersAfterDelete(estIndex).lengthCompare(appConfig.maxPartners) >= 0 =>
        controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, estIndex, srn)
      case _ =>
        controllers.register.establishers.partnership.partner.routes.PartnerNameController.onPageLoad(
          mode, estIndex, ua.allPartners(estIndex).size, srn)
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndUpdateModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(checkModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(normalAndUpdateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object PartnerNavigator {

  def taskListPage(mode: Mode, srn: Option[String]): Call = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  private def isNewPartner(estIndex: Int, partnerIndex: Int, ua: UserAnswers): Boolean =
    ua.get(IsNewPartnerId(estIndex, partnerIndex)).getOrElse(false)

  private def isThisPaPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerConfirmPreviousAddressController.onPageLoad(estIndex, partnerIndex, srn)

  private def addPartnerPage(mode: Mode, estIndex: Int, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, estIndex, srn)

  private def dobPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerDOBController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def hasNinoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerHasNINOController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def ninoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerEnterNINOController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def whyNoNinoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerNoNINOReasonController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def hasUtrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerHasUTRController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def utrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerEnterUTRController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def whyNoUtrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerNoUTRReasonController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def cyaPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    CheckYourAnswersController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def postcodeLookupPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressPostcodeLookupController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def addressListPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressListController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def addressPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def paPostcodeLookupPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def paAddressListPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressListController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def paAddressPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def addressYearsPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressYearsController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def emailPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerEmailController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def phonePage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPhoneController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def partnerAddressYearsRoutes(mode: Mode, ua: UserAnswers, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    ua.get(partner.PartnerAddressYearsId(estIndex, partnerIndex)) match {
      case Some(AddressYears.OverAYear) => emailPage(mode, estIndex, partnerIndex, srn)
      case Some(AddressYears.UnderAYear) => paPostcodeLookupPage(mode, estIndex, partnerIndex, srn)
      case _ => SessionExpiredController.onPageLoad()
    }

  private def partnerAddressYearsEditRoutes(mode: Mode, ua: UserAnswers, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    ua.get(partner.PartnerAddressYearsId(estIndex, partnerIndex)) match {
      case Some(AddressYears.OverAYear) => cyaPage(mode, estIndex, partnerIndex, srn)
      case Some(AddressYears.UnderAYear) => paPostcodeLookupPage(mode, estIndex, partnerIndex, srn)
      case _ => SessionExpiredController.onPageLoad()
    }


}








