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

package navigators.trustees.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.partnership.routes._
import identifiers.Identifier
import identifiers.register.trustees.partnership._
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers
import controllers.routes.{AnyMoreChangesController, SessionExpiredController}
import identifiers.register.trustees.IsTrusteeNewId

//scalastyle:off cyclomatic.complexity
class TrusteesPartnershipAddressNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator {

  import TrusteesPartnershipAddressNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode,
                                       ua: UserAnswers,
                                       srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipPostcodeLookupId(index) => PartnershipAddressListController.onPageLoad(mode, index, None)
    case PartnershipAddressListId(index) if mode == NormalMode =>
      PartnershipAddressYearsController.onPageLoad(mode, index, None)
    case PartnershipAddressListId(index) => cyaAddress(journeyMode(mode), index, None)
    case PartnershipAddressId(index) if mode == NormalMode =>
      PartnershipAddressYearsController.onPageLoad(mode, index, None)
    case PartnershipAddressId(index) => cyaAddress(journeyMode(mode), index, None)
    case PartnershipAddressYearsId(index) => trusteeAddressYearsRoutes(mode, ua, index, None)
    case id@PartnershipHasBeenTradingId(index) =>
      booleanNav(id, ua, previousAddressLookup(mode, index, None), cyaAddress(journeyMode(mode), index, None))
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      PartnershipPreviousAddressListController.onPageLoad(mode, index, None)
    case PartnershipPreviousAddressListId(index) => cyaAddress(journeyMode(mode), index, None)
    case PartnershipPreviousAddressId(index) => cyaAddress(journeyMode(mode), index, None)
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: VarianceMode,
                               ua: UserAnswers,
                               srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipPostcodeLookupId(index) => PartnershipAddressListController.onPageLoad(mode, index, srn)
    case PartnershipAddressListId(index) if mode == UpdateMode =>
      PartnershipAddressYearsController.onPageLoad(mode, index, srn)
    case PartnershipAddressListId(index) => trusteeAddressRoute(ua, mode, index, srn)
    case PartnershipAddressId(index) if mode == UpdateMode =>
      PartnershipAddressYearsController.onPageLoad(mode, index, srn)
    case PartnershipAddressId(index) => trusteeAddressRoute(ua, mode, index, srn)
    case PartnershipAddressYearsId(index) => trusteeAddressYearsRoutes(mode, ua, index, srn)
    case id@PartnershipHasBeenTradingId(index) =>
      booleanNav(id, ua, previousAddressLookup(mode, index, srn), cyaAddress(journeyMode(mode), index, srn))
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      PartnershipPreviousAddressListController.onPageLoad(mode, index, srn)
    case PartnershipPreviousAddressListId(index) if isNewTrustee(index, ua) => cyaAddress(journeyMode(mode), index, srn)
    case PartnershipPreviousAddressListId(_) => moreChanges(srn)
    case id@PartnershipConfirmPreviousAddressId(index) =>
      booleanNav(id, ua, moreChanges(srn), previousAddressLookup(mode, index, srn))
    case PartnershipPreviousAddressId(index) if isNewTrustee(index, ua) => cyaAddress(journeyMode(mode), index, srn)
    case PartnershipPreviousAddressId(_) => moreChanges(srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object TrusteesPartnershipAddressNavigator {
  private def moreChanges(srn: Option[String]): Call = AnyMoreChangesController.onPageLoad(srn)

  private def previousAddressLookup(mode: Mode, index: Index, srn: Option[String]): Call =
    PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)

  private def cyaAddress(mode: Mode, index: Index, srn: Option[String]): Call =
    CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)

  private def trusteeAddressYearsRoutes(mode: Mode, ua: UserAnswers, index: Int, srn: Option[String]): Call =
    ua.get(PartnershipAddressYearsId(index)) match {
      case Some(AddressYears.OverAYear) =>
        CheckYourAnswersPartnershipAddressController.onPageLoad(journeyMode(mode), index, srn)
      case Some(AddressYears.UnderAYear) => hasBeenTrading(mode, index, srn)
      case _ => SessionExpiredController.onPageLoad()
    }

  private def hasBeenTrading(mode: Mode, index: Index, srn: Option[String]): Call =
    PartnershipHasBeenTradingController.onPageLoad(mode, index, srn)

  private def trusteeAddressRoute(ua: UserAnswers, mode: Mode, index: Int, srn: Option[String]): Call = {
    if (isNewTrustee(index, ua)) {
      CheckYourAnswersPartnershipAddressController.onPageLoad(journeyMode(mode), index, srn)
    } else {
      PartnershipConfirmPreviousAddressController.onPageLoad(index, srn)
    }
  }

  private def isNewTrustee(index: Int, ua: UserAnswers): Boolean =
    ua.get(IsTrusteeNewId(index)).getOrElse(false)
}


