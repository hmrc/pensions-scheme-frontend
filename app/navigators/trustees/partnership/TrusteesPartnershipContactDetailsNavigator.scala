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

package navigators.trustees.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.partnership.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership.{PartnershipEmailId, PartnershipPhoneId}
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class TrusteesPartnershipContactDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends
  AbstractNavigator {

  import TrusteesPartnershipContactDetailsNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, EmptyOptionalSchemeReferenceNumber), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode,
                                       ua: UserAnswers,
                                       srn: OptionalSchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if mode == NormalMode => phonePage(mode, index, srn)
    case PartnershipEmailId(index) => cyaPage(mode, index, srn)
    case PartnershipPhoneId(index) => cyaPage(mode, index, srn)
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, EmptyOptionalSchemeReferenceNumber), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type,
                               ua: UserAnswers,
                               srn: OptionalSchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) => phonePage(mode, index, srn)
    case PartnershipPhoneId(index) => cyaPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type,
                                   ua: UserAnswers,
                                   srn: OptionalSchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) => cyaPage(mode, index, srn)
    case PartnershipEmailId(_) => anyMoreChangesPage(srn)
    case PartnershipPhoneId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) => cyaPage(mode, index, srn)
    case PartnershipPhoneId(_) => anyMoreChangesPage(srn)
  }
}

object TrusteesPartnershipContactDetailsNavigator {

  private def phonePage(mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    PartnershipPhoneNumberController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: OptionalSchemeReferenceNumber): Call =
    CheckYourAnswersPartnershipContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}



