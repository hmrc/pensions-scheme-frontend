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
import identifiers.register.trustees.individual._
import models.AddressYears._
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
    case TrusteeAddressYearsId(index)                     => trusteeAddressYearsRoutes(mode, ua, index)
    case IndividualPreviousAddressPostCodeLookupId(index) => TrusteePreviousAddressListController.onPageLoad(mode, index, None)
    case TrusteePreviousAddressListId(index)              => TrusteePreviousAddressController.onPageLoad(mode, index, None)
    case TrusteePreviousAddressId(index)                  => CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateOrSessionReset(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateOrSessionReset(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = ???

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = ???
}

object TrusteesIndividualAddressNavigator {
  private def trusteeAddressYearsRoutes(mode: Mode, ua: UserAnswers, index: Int): Call =
    ua.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.OverAYear) => CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, None)
      case Some(AddressYears.UnderAYear) => IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, None)
      case _ => SessionExpiredController.onPageLoad()
    }
}
