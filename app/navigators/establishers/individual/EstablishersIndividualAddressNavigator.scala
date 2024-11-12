/*
 * Copyright 2024 HM Revenue & Customs
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

package navigators.establishers.individual

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.individual.routes._
import controllers.routes.{AnyMoreChangesController, SessionExpiredController}
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class EstablishersIndividualAddressNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends
  AbstractNavigator {

  import EstablishersIndividualAddressNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, EmptyOptionalSchemeReferenceNumber), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, EmptyOptionalSchemeReferenceNumber), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: OptionalSchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case PostCodeLookupId(index) => AddressListController.onPageLoad(mode, index, EmptyOptionalSchemeReferenceNumber)
    case AddressListId(index) if mode == NormalMode => AddressYearsController.onPageLoad(mode, index, EmptyOptionalSchemeReferenceNumber)
    case AddressListId(index) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, EmptyOptionalSchemeReferenceNumber)
    case AddressId(index) if mode == NormalMode => AddressYearsController.onPageLoad(mode, index, EmptyOptionalSchemeReferenceNumber)
    case AddressId(index) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, EmptyOptionalSchemeReferenceNumber)
    case AddressYearsId(index) => establisherAddressYearsRoutes(mode, ua, index, EmptyOptionalSchemeReferenceNumber)
    case PreviousPostCodeLookupId(index) => PreviousAddressListController.onPageLoad(mode, index, EmptyOptionalSchemeReferenceNumber)
    case PreviousAddressListId(index) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, EmptyOptionalSchemeReferenceNumber)
    case PreviousAddressId(index) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, EmptyOptionalSchemeReferenceNumber)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: OptionalSchemeReferenceNumber): PartialFunction[Identifier,
    Call] = {
    case PostCodeLookupId(index) => AddressListController.onPageLoad(mode, index, srn)
    case AddressListId(index) if mode == UpdateMode => AddressYearsController.onPageLoad(mode, index, srn)
    case AddressListId(index) => establisherAddressRoute(ua, mode, index, srn)
    case AddressId(index) if mode == UpdateMode => AddressYearsController.onPageLoad(mode, index, srn)
    case AddressId(index) => establisherAddressRoute(ua, mode, index, srn)
    case AddressYearsId(index) => establisherAddressYearsRoutes(mode, ua, index, srn)
    case PreviousPostCodeLookupId(index) => PreviousAddressListController.onPageLoad(mode, index, srn)
    case PreviousAddressListId(index) => cyaOrMoreChanges(ua, journeyMode(mode), index, srn)
    case id@IndividualConfirmPreviousAddressId(index) => booleanNav(id, ua, moreChanges(srn), previousAddressLookup
    (mode, index, srn))
    case PreviousAddressId(index) => cyaOrMoreChanges(ua, journeyMode(mode), index, srn)
  }
}

object EstablishersIndividualAddressNavigator {
  private def cyaOrMoreChanges(ua: UserAnswers, mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    ua.get(IsEstablisherNewId(index)) match {
      case Some(true) => CheckYourAnswersAddressController.onPageLoad(mode, index, srn)
      case _ => moreChanges(srn)
    }

  private def moreChanges(srn: OptionalSchemeReferenceNumber): Call = AnyMoreChangesController.onPageLoad(srn)

  private def establisherAddressRoute(ua: UserAnswers, mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call = {
    ua.get(IsEstablisherNewId(index)) match {
      case Some(true) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, srn)
      case _ => IndividualConfirmPreviousAddressController.onPageLoad(index, srn)
    }
  }

  private def establisherAddressYearsRoutes(mode: Mode, ua: UserAnswers, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    ua.get(AddressYearsId(index)) match {
      case Some(AddressYears.OverAYear) => CheckYourAnswersAddressController.onPageLoad(journeyMode(mode), index, srn)
      case Some(AddressYears.UnderAYear) => previousAddressLookup(mode, index, srn)
      case _ => SessionExpiredController.onPageLoad
    }

  private def previousAddressLookup(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Call =
    PreviousAddressPostCodeLookupController.onPageLoad(mode, index, srn)
}



