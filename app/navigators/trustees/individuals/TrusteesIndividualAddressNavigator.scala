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

package navigators.trustees.individuals

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.individual.routes._
import controllers.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models.AddressYears._
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualAddressNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import TrusteesIndividualAddressNavigator._

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case IndividualPostCodeLookupId(index)                => IndividualAddressListController.onPageLoad(mode, index, None)
    case IndividualAddressListId(index)                   => TrusteeAddressController.onPageLoad(mode, index, None)
    case TrusteeAddressId(index)                          => TrusteeAddressYearsController.onPageLoad(mode, index, None)
    case TrusteeAddressYearsId(index)                     => trusteeAddressYearsRoutes(mode, ua, index, None)
    case IndividualPreviousAddressPostCodeLookupId(index) => TrusteePreviousAddressListController.onPageLoad(mode, index, None)
    case TrusteePreviousAddressListId(index)              => TrusteePreviousAddressController.onPageLoad(mode, index, None)
    case TrusteePreviousAddressId(index)                  => CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, None)
  }

  private def updateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case IndividualPostCodeLookupId(index)                => IndividualAddressListController.onPageLoad(mode, index, srn)
    case IndividualAddressListId(index)                   => TrusteeAddressController.onPageLoad(mode, index, srn)
    case TrusteeAddressId(index)                          => trusteeAddressRoute(ua, mode, index, srn)
    case TrusteeAddressYearsId(index)                     => trusteeAddressYearsRoutes(mode, ua, index, srn)
    case IndividualPreviousAddressPostCodeLookupId(index) => TrusteePreviousAddressListController.onPageLoad(mode, index, srn)
    case TrusteePreviousAddressListId(index)              => TrusteePreviousAddressController.onPageLoad(mode, index, srn)
    case id@IndividualConfirmPreviousAddressId(index)     => booleanNav(id, ua, moreChanges(srn), previousAddressLookup(mode, index, srn))
    case TrusteePreviousAddressId(index)                  => cyaOrMoreChanges(ua, journeyMode(mode), index, srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object TrusteesIndividualAddressNavigator {
  private def moreChanges(srn: Option[String]): Call = AnyMoreChangesController.onPageLoad(srn)
  private def previousAddressLookup(mode: Mode, index: Index, srn: Option[String]): Call =
    IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)

  private def cyaOrMoreChanges(ua: UserAnswers, mode: Mode, index: Int, srn: Option[String]): Call =
    ua.get(IsTrusteeNewId(index)) match {
      case Some(true) => CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn)
      case _ => moreChanges(srn)
    }

  private def trusteeAddressRoute(ua: UserAnswers, mode: Mode, index: Int, srn: Option[String]): Call = {
    println("\n\n\n here1..")
    ua.get(IsTrusteeNewId(index)) match {
      case Some(true) => TrusteeAddressYearsController.onPageLoad(mode, index, srn)
      case _ => IndividualConfirmPreviousAddressController.onPageLoad(index, srn)
    }
  }

  private def trusteeAddressYearsRoutes(mode: Mode, ua: UserAnswers, index: Int, srn: Option[String]): Call =
    ua.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.OverAYear) => CheckYourAnswersIndividualAddressController.onPageLoad(journeyMode(mode), index, srn)
      case Some(AddressYears.UnderAYear) => previousAddressLookup(mode, index, srn)
      case _ => SessionExpiredController.onPageLoad()
    }
}
