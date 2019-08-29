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

package navigators.trustees.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.partnership.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models._
import models.Mode._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesPartnershipDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import TrusteesPartnershipDetailsNavigator._

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_)                                  => addTrusteesPage(mode, srn)
    case id@PartnershipHasUTRId(index)                            => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case PartnershipUTRId(index) if mode == NormalMode            => hasVat(mode, index, srn)
    case PartnershipUTRId(index)                                  => cyaPage(mode, index, srn)
    case PartnershipNoUTRReasonId(index) if mode == NormalMode    => hasVat(mode, index, srn)
    case PartnershipNoUTRReasonId(index)                          => cyaPage(mode, index, srn)
  }

  private def updateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_) => addTrusteesPage(mode, srn)
    case id@PartnershipHasUTRId(index)        => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case PartnershipUTRId(index)              => hasVat(mode, index, srn)
    case PartnershipNoUTRReasonId(index)      => hasVat(mode, index, srn)
  }

  private def checkUpdateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@PartnershipHasUTRId(index)                                  => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case PartnershipUTRId(index) if isNewTrustee(index, ua)             => cyaPage(mode, index, srn)
    case PartnershipUTRId(_)                                            => anyMoreChangesPage(srn)
    case PartnershipNoUTRReasonId(index) if isNewTrustee(index, ua)     => cyaPage(mode, index, srn)
    case PartnershipNoUTRReasonId(_)                                    => anyMoreChangesPage(srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object TrusteesPartnershipDetailsNavigator {

  private def isNewTrustee(index: Int, ua: UserAnswers) = ua.get(IsTrusteeNewId(index)).getOrElse(false)

  private def addTrusteesPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn)

  private def utrPage(mode: Mode, index: Int, srn: Option[String]): Call =
    PartnershipUTRController.onPageLoad(mode, index, srn)

  private def noUtrReasonPage(mode: Mode, index: Int, srn: Option[String]): Call =
    PartnershipNoUTRReasonController.onPageLoad(mode, index, srn)

  private def hasVat(mode: Mode, index: Int, srn: Option[String]): Call =
    PartnershipHasVATController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersPartnershipDetailsController.onPageLoad(journeyMode(mode), index, srn)

}