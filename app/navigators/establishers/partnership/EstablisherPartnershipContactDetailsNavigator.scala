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

package navigators.establishers.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.partnership.routes._
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.{PartnershipEmailId, PartnershipPhoneNumberId}
import models.Mode.journeyMode
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class EstablisherPartnershipContactDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator {

  import EstablisherPartnershipContactDetailsNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: OptionalSchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if mode == NormalMode => phonePage(mode, index, srn)
    case PartnershipEmailId(index) => cyaPage(mode, index, srn)
    case PartnershipPhoneNumberId(index) => cyaPage(mode, index, srn)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type, ua: UserAnswers, srn: OptionalSchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) => phonePage(mode, index, srn)
    case PartnershipPhoneNumberId(index) => cyaPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: OptionalSchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) => cyaPage(mode, index, srn)
    case PartnershipEmailId(_) => anyMoreChangesPage(srn)
    case PartnershipPhoneNumberId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) => cyaPage(mode,
      index, srn)
    case PartnershipPhoneNumberId(_) => anyMoreChangesPage(srn)
  }
}

object EstablisherPartnershipContactDetailsNavigator {

  private def phonePage(mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    PartnershipPhoneNumberController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    CheckYourAnswersPartnershipContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}






