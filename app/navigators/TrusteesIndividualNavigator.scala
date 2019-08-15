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
import controllers.register.trustees.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual.{TrusteeDOBId, _}
import models.Mode._
import models._
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import TrusteesIndividualNavigator._

  private def normalAndCheckModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeNameId(_)                                   => AddTrusteeController.onPageLoad(mode, srn)
    case TrusteeDOBId(index) if mode == NormalMode          => hasNinoPage(mode, index, srn)
    case TrusteeDOBId(index) if mode == CheckMode           => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasNINOId(index)                         => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case TrusteeNewNinoId(index) if mode == NormalMode      => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNewNinoId(index) if mode == CheckMode       => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case TrusteeNoNINOReasonId(index) if mode == NormalMode => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNoNINOReasonId(index) if mode == CheckMode  => CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, None)
    case id@TrusteeHasUTRId(index)                          => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index)                        => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index)                                => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeEmailId(index) if mode == NormalMode        => phonePage(mode, index, srn)
    case TrusteeEmailId(index)                              => cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteePhoneId(index)                              => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  private def updateModeRoutes(mode: UpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeNameId(index)         => AddTrusteeController.onPageLoad(mode, srn)
    case TrusteeDOBId(index)          => hasNinoPage(mode, index, srn)
    case id@TrusteeHasNINOId(index)   => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case TrusteeNewNinoId(index)      => trusteeHasUtrPage(mode, index, srn)
    case TrusteeNoNINOReasonId(index) => trusteeHasUtrPage(mode, index, srn)
    case id@TrusteeHasUTRId(index)    => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index)  => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index)          => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeEmailId(index)        => phonePage(mode, index, srn)
    case TrusteePhoneId(index)        => cyaIndividualContactDetailsPage(mode, index, srn)
  }

  private def checkUpdateModeRoute(mode: CheckUpdateMode.type, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case TrusteeDOBId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)            => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeNewNinoId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)        => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeNewNinoId(index) if !ua.get(IsTrusteeNewId(index)).getOrElse(false)       => anyMoreChangesPage(srn)
    case TrusteeNoNINOReasonId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)   => cyaIndividualDetailsPage(mode, index, srn)
    case id@TrusteeHasNINOId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)     => booleanNav(id, ua, ninoPage(mode, index, srn), noNinoReasonPage(mode, index, srn))
    case id@TrusteeHasUTRId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)      => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case TrusteeNoUTRReasonId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)    => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeUTRId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)            => cyaIndividualDetailsPage(mode, index, srn)
    case TrusteeEmailId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)          => cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteeEmailId(_)                                                                => anyMoreChangesPage(srn)
    case TrusteePhoneId(index) if ua.get(IsTrusteeNewId(index)).getOrElse(false)          => cyaIndividualContactDetailsPage(mode, index, srn)
    case TrusteePhoneId(_)                                                                => anyMoreChangesPage(srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateOrSessionReset(normalAndCheckModeRoutes(NormalMode, from.userAnswers, None), from.id )

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateOrSessionReset(normalAndCheckModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateOrSessionReset(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateOrSessionReset(checkUpdateModeRoute(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object TrusteesIndividualNavigator {
  private def hasNinoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeHasNINOController.onPageLoad(mode, index, srn)

  private def ninoPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNinoNewController.onPageLoad(mode, index, srn)

  private def trusteeHasUtrPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeHasUTRController.onPageLoad(mode, index, srn)

  private def noNinoReasonPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNoNINOReasonController.onPageLoad(mode, index, srn)

  private def utrPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeUTRController.onPageLoad(mode, index, srn)

  private def noUtrReasonPage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteeNoUTRReasonController.onPageLoad(mode, index, srn)

  private def cyaIndividualDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersIndividualDetailsController.onPageLoad(journeyMode(mode), index, srn)

  private def phonePage(mode: Mode, index: Int, srn: Option[String]): Call = TrusteePhoneController.onPageLoad(mode, index, srn)

  private def cyaIndividualContactDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersIndividualContactDetailsController.onPageLoad(journeyMode(mode), index, srn)
}
