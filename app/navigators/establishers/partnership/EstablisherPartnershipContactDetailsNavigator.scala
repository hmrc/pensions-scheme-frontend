/*
 * Copyright 2020 HM Revenue & Customs
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

class EstablisherPartnershipContactDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import EstablisherPartnershipContactDetailsNavigator._
  
  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if mode == NormalMode        => phonePage(mode, index, srn)
    case PartnershipEmailId(index)                              => cyaPage(mode, index, srn)
    case PartnershipPhoneNumberId(index)                              => cyaPage(mode, index, srn)
  }

  private def updateModeRoutes(mode: UpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index)        => phonePage(mode, index, srn)
    case PartnershipPhoneNumberId(index)        => cyaPage(mode, index, srn)
  }

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipEmailId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false)          => cyaPage(mode, index, srn)
    case PartnershipEmailId(_)                                                                => anyMoreChangesPage(srn)
    case PartnershipPhoneNumberId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false)          => cyaPage(mode, index, srn)
    case PartnershipPhoneNumberId(_)                                                                => anyMoreChangesPage(srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id )

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object EstablisherPartnershipContactDetailsNavigator {

  private def phonePage(mode: Mode, index: Int, srn: Option[String]): Call =
    PartnershipPhoneNumberController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersPartnershipContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}






