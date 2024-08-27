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
import controllers.register.establishers.routes._
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.individual._
import models.Mode._
import models._
import navigators.AbstractNavigator
import navigators.establishers.individual.OldEstablishersIndividualDetailsNavigator.{ninoPage, noNinoReasonPage, noUtrReasonPage, utrPage}
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class OldEstablishersIndividualDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, srn), from.id)

  protected def editRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, srn), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: SchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case EstablisherNameId(_) => AddEstablisherController.onPageLoad(mode, srn)
    case EstablisherDOBId(index) if mode == NormalMode => EstablisherHasNINOController.onPageLoad(mode, index, srn)
    case EstablisherDOBId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case id@EstablisherHasNINOId(index) => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode,
      index, srn))
    case EstablisherEnterNINOId(index) if mode == NormalMode => EstablisherHasUTRController.onPageLoad(mode, index, srn)
    case EstablisherEnterNINOId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherNoNINOReasonId(index) if mode == NormalMode => EstablisherHasUTRController.onPageLoad(mode,
      index, srn)
    case EstablisherNoNINOReasonId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case id@EstablisherHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index,
      srn))
    case EstablisherNoUTRReasonId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherUTRId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: UpdateMode.type, ua: UserAnswers, srn: SchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case EstablisherNameId(_) => AddEstablisherController.onPageLoad(mode, srn)
    case EstablisherDOBId(index) => EstablisherHasNINOController.onPageLoad(mode, index, srn)
    case id@EstablisherHasNINOId(index) => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode,
      index, srn))
    case EstablisherEnterNINOId(index) => EstablisherHasUTRController.onPageLoad(mode, index, srn)
    case EstablisherNoNINOReasonId(index) => EstablisherHasUTRController.onPageLoad(mode, index, srn)
    case id@EstablisherHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index,
      srn))
    case EstablisherNoUTRReasonId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherUTRId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoutes(mode: CheckUpdateMode.type, ua: UserAnswers, srn: SchemeReferenceNumber)
  : PartialFunction[Identifier, Call] = {
    case EstablisherDOBId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherEnterNINOId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) =>
      CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherEnterNINOId(_) => anyMoreChangesPage(srn)
    case EstablisherNoNINOReasonId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case id@EstablisherHasNINOId(index) => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode,
      index, srn))
    case id@EstablisherHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index,
      srn))
    case EstablisherNoUTRReasonId(index) => CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherUTRId(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) =>
      CheckYourAnswersDetailsController.onPageLoad(journeyMode(mode), index, srn)
    case EstablisherUTRId(_) => anyMoreChangesPage(srn)
  }

}
object OldEstablishersIndividualDetailsNavigator {
  private def ninoPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call = EstablisherEnterNINOController
    .onPageLoad(mode, index, srn)

  private def noNinoReasonPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call = EstablisherNoNINOReasonController
    .onPageLoad(mode, index, srn)

  private def utrPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call = EstablisherEnterUTRController
    .onPageLoad(mode, index, srn)

  private def noUtrReasonPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call = EstablisherNoUTRReasonController
    .onPageLoad(mode, index, srn)
}


