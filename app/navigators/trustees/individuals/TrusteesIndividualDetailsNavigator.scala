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

package navigators.trustees.individuals

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.individual.routes._
import controllers.register.trustees.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual.{TrusteeDOBId, _}
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator {

  import TrusteesIndividualDetailsNavigator._

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode,
                                       ua: UserAnswers,
                                       srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeNameId(_) =>
      AddTrusteeController.onPageLoad(mode, srn)
    case TrusteeDOBId(index) if mode == NormalMode =>
      hasNinoPage(mode, index, srn)
    case TrusteeDOBId(index) =>
      CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasNINOId(index) =>
      booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case TrusteeEnterNINOId(index) if mode == NormalMode =>
      trusteeHasUtrPage(mode, index, srn)
    case TrusteeEnterNINOId(index) =>
      CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case TrusteeNoNINOReasonId(index) if mode == NormalMode =>
      trusteeHasUtrPage(mode, index, srn)
    case TrusteeNoNINOReasonId(index) =>
      CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasUTRId(index) =>
      booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index) =>
      cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) =>
      cyaIndividualDetailsPage(mode, index, srn)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type,
                               ua: UserAnswers,
                               srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeNameId(_) => AddTrusteeController.onPageLoad(mode, srn)
    case TrusteeDOBId(index) => hasNinoPage(mode, index, srn)
    case id@TrusteeHasNINOId(index) =>
      booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case TrusteeEnterNINOId(index) => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNoNINOReasonId(index) => trusteeHasUtrPage(mode, index, srn)
    case id@TrusteeHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index) => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) => cyaIndividualDetailsPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type,
                                   ua: UserAnswers,
                                   srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeDOBId(index) => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeEnterNINOId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) =>
      cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeEnterNINOId(index) => anyMoreChangesPage(srn)
    case TrusteeNoNINOReasonId(index) => cyaIndividualDetailsPage(mode, index, srn)
    case id@TrusteeHasNINOId(index) =>
      booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case id@TrusteeHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index) => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false) =>
      cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) => anyMoreChangesPage(srn)
  }
}

object TrusteesIndividualDetailsNavigator {
  private def hasNinoPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeHasNINOController.onPageLoad(mode, index, srn)

  private def ninoPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeEnterNINOController.onPageLoad(mode, index, srn)

  private def trusteeHasUtrPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeHasUTRController.onPageLoad(mode, index, srn)

  private def noNinoReasonPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeNoNINOReasonController.onPageLoad(mode, index, srn)

  private def utrPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeEnterUTRController.onPageLoad(mode, index, srn)

  private def noUtrReasonPage(mode: Mode, index: Int, srn: Option[String]): Call =
    TrusteeNoUTRReasonController.onPageLoad(mode, index, srn)

  private def cyaIndividualDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, srn)
}
