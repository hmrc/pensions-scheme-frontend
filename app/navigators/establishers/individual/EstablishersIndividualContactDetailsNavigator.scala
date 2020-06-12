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

package navigators.establishers.individual

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.individual.routes.{CheckYourAnswersContactDetailsController,
  EstablisherPhoneController}
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual.{EstablisherEmailId, EstablisherPhoneId}
import models.Mode.journeyMode
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualContactDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator {

  import EstablishersIndividualContactDetailsNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String])
  : PartialFunction[Identifier, Call] = {
    case EstablisherEmailId(index) if mode == NormalMode => phonePage(mode, index, srn)
    case EstablisherEmailId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
    case EstablisherPhoneId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type, ua: UserAnswers, srn: Option[String])
  : PartialFunction[Identifier, Call] = {
    case EstablisherEmailId(index) => phonePage(mode, index, srn)
    case EstablisherPhoneId(index) => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: Option[String])
  : PartialFunction[Identifier, Call] = {
    case EstablisherEmailId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) =>
      cyaIndividualContactDetailsPage(mode, index, srn)
    case EstablisherEmailId(_) => anyMoreChangesPage(srn)
    case EstablisherPhoneId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) =>
      cyaIndividualContactDetailsPage(mode, index, srn)
    case EstablisherPhoneId(_) => anyMoreChangesPage(srn)
  }
}

object EstablishersIndividualContactDetailsNavigator {
  private def phonePage(mode: Mode, index: Int, srn: Option[String]): Call =
    EstablisherPhoneController.onPageLoad(mode, index, srn)

  private def cyaIndividualContactDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}



