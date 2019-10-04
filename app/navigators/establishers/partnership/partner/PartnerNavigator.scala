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
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.partnership.partner.routes._
import controllers.routes.SessionExpiredController
import identifiers.Identifier
import identifiers.register.establishers.partnership.partner
import identifiers.register.establishers.partnership.partner._
import models.Mode.journeyMode
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class PartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import PartnerNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnerNameId(estIndex, partnerIndex)                            =>           dobPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                             =>           hasNinoPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                      =>
      booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerNewNinoId(estIndex, partnerIndex)                         =>           hasUtrPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                    =>           hasUtrPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                       =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex)                        =>          postcodeLookupPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                     =>          postcodeLookupPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressPostcodeLookupId(estIndex, partnerIndex)           => addressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressListId(estIndex, partnerIndex)                     => addressPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                         => addressYearsPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                 => partnerAddressYearsRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressPostcodeLookupId(estIndex, partnerIndex)   => paAddressListPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressListId(estIndex, partnerIndex)             => paAddressPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex)                 => emailPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(estIndex, partnerIndex)                           => phonePage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case ConfirmDeletePartnerId(estIndex)                                 => addPartnerPage(mode, estIndex, srn)
  }

  private def checkModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnerNameId(estIndex, partnerIndex)                            => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                             => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                      =>
      booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerNewNinoId(estIndex, partnerIndex)                         => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                    => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                       =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex)                        => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                     => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                         => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                 => partnerAddressYearsRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex)                 => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(estIndex, partnerIndex)                           => cyaPage(mode, estIndex, partnerIndex, srn)
  }

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnerNameId(estIndex, partnerIndex)                                                  => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerDOBId(estIndex, partnerIndex)                                                   => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasNINOId(estIndex, partnerIndex)                                            =>
      booleanNav(id, ua, ninoPage(mode, estIndex, partnerIndex, srn), whyNoNinoPage(mode, estIndex, partnerIndex, srn))
    case PartnerNewNinoId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)   => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerNewNinoId(estIndex, partnerIndex)                                               => anyMoreChangesPage(srn)
    case PartnerNoNINOReasonId(estIndex, partnerIndex)                                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerHasUTRId(estIndex, partnerIndex)                                             =>
      booleanNav(id, ua, utrPage(mode, estIndex, partnerIndex, srn), whyNoUtrPage(mode, estIndex, partnerIndex, srn))
    case PartnerEnterUTRId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)  => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEnterUTRId(estIndex, partnerIndex)                                              => anyMoreChangesPage(srn)
    case PartnerNoUTRReasonId(estIndex, partnerIndex)                                           => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)   => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerAddressId(estIndex, partnerIndex)                                               => isThisPaPage(mode, estIndex, partnerIndex, srn)
    case id@PartnerConfirmPreviousAddressId(estIndex, partnerIndex)                             =>
      booleanNav(id, ua, anyMoreChangesPage(srn), paPostcodeLookupPage(mode, estIndex, partnerIndex, srn))
    case id@PartnerAddressYearsId(estIndex, partnerIndex)                                       => partnerAddressYearsRoutes(mode, ua, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPreviousAddressId(estIndex, partnerIndex)                                       => anyMoreChangesPage(srn)
    case PartnerEmailId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerEmailId(estIndex, partnerIndex)                                                 => anyMoreChangesPage(srn)
    case PartnerPhoneId(estIndex, partnerIndex) if isNewPartner(estIndex, partnerIndex, ua)                          => cyaPage(mode, estIndex, partnerIndex, srn)
    case PartnerPhoneId(estIndex, partnerIndex)                                                 => anyMoreChangesPage(srn)
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

  private def isNewPartner(estIndex: Int, partnerIndex: Int, ua: UserAnswers): Boolean =
    ua.get(IsNewPartnerId(estIndex, partnerIndex)).getOrElse(false)

  private def isThisPaPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerConfirmPreviousAddressController.onPageLoad(estIndex, partnerIndex, srn)

  private def addPartnerPage(mode: Mode, estIndex: Int, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, estIndex, srn)

  private def dobPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerDOBController.onPageLoad(mode, estIndex, partnerIndex, srn)

  private def hasNinoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerHasNINOController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def ninoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerNinoNewController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def whyNoNinoPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerNoNINOReasonController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def hasUtrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerHasUTRController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def utrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerUTRController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def whyNoUtrPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerNoUTRReasonController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def cyaPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    CheckYourAnswersController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def postcodeLookupPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressPostcodeLookupController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def addressListPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressListController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def addressPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def paPostcodeLookupPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressPostcodeLookupController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def paAddressListPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressListController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def paAddressPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPreviousAddressController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def addressYearsPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerAddressYearsController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def emailPage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerEmailController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def phonePage(mode: Mode, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    PartnerPhoneController.onPageLoad(journeyMode(mode), estIndex, partnerIndex, srn)

  private def partnerAddressYearsRoutes(mode: Mode, ua: UserAnswers, estIndex: Int, partnerIndex: Int, srn: Option[String]): Call =
    ua.get(partner.PartnerAddressYearsId(estIndex, partnerIndex)) match {
      case Some(AddressYears.OverAYear) => emailPage(journeyMode(mode), estIndex, partnerIndex, srn)
      case Some(AddressYears.UnderAYear) => paPostcodeLookupPage(journeyMode(mode), estIndex, partnerIndex, srn)
      case _ => SessionExpiredController.onPageLoad()
    }
}








