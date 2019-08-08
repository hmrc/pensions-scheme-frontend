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

package navigators

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.individual.routes._
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models.Mode._
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import TrusteesIndividualNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeDOBId(index) if mode == NormalMode => hasNinoPage(mode, index, srn)
    case TrusteeDOBId(index) if mode == CheckMode => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasNINOId(index) => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case TrusteeNewNinoId(index) if mode == NormalMode => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNewNinoId(index) if mode == CheckMode => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case TrusteeNoNINOReasonId(index) if mode == NormalMode => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNoNINOReasonId(index) if mode == CheckMode => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index) => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) => cyaIndividualDetailsPage(mode, index, srn)
  }


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = navigateOrSessionReset(normalAndUpdateModeRoutes(NormalMode, from.userAnswers, None), from.id )

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = navigateOrSessionReset(normalAndUpdateModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}

object TrusteesIndividualNavigator {
  private def hasNinoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeHasNINOController.onPageLoad(mode, index, srn)

  private def ninoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNinoNewController.onPageLoad(mode, index, srn)

  private def trusteeHasUtrPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeHasUTRController.onPageLoad(mode, index, None)

  private def noNinoReasonPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNoNINOReasonController.onPageLoad(mode, index, srn)

  private def utrPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeUTRController.onPageLoad(mode, index, None)
  private def noUtrReasonPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNoUTRReasonController.onPageLoad(mode, index, None)


  private def cyaIndividualDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
}
