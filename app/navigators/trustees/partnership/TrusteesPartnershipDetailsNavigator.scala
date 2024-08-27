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
import controllers.register.trustees.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models.FeatureToggleName.SchemeRegistration
import models.Mode._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class TrusteesPartnershipDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends
  AbstractNavigator {

  import TrusteesPartnershipDetailsNavigator._

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(NormalMode, from.userAnswers, srn), from.id)

  private def normalAndCheckModeRoutes(mode: SubscriptionMode,
                                       ua: UserAnswers,
                                       srn: SchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(index) => // TODO: Remove Json code below when SchemeRegistration toggle is removed
      (ua.json \ SchemeRegistration.asString).asOpt[Boolean] match {
        case Some(true) => trusteeTaskList(index, srn)
        case _ => AddTrusteeController.onPageLoad(mode, srn)
      }
    case id@PartnershipHasUTRId(index) =>
      booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case PartnershipEnterUTRId(index) if mode == NormalMode => hasVat(mode, index, srn)
    case PartnershipEnterUTRId(index) => cyaPage(mode, index, srn)
    case PartnershipNoUTRReasonId(index) if mode == NormalMode => hasVat(mode, index, srn)
    case PartnershipNoUTRReasonId(index) => cyaPage(mode, index, srn)
    case id@PartnershipHasVATId(index) if mode == NormalMode =>
      booleanNav(id, ua, enterVat(mode, index, srn), hasPaye(mode, index, srn))
    case id@PartnershipHasVATId(index) =>
      booleanNav(id, ua, enterVat(mode, index, srn), cyaPage(mode, index, srn))
    case PartnershipEnterVATId(index) if mode == NormalMode => hasPaye(mode, index, srn)
    case PartnershipEnterVATId(index) => cyaPage(mode, index, srn)
    case id@PartnershipHasPAYEId(index) =>
      booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case PartnershipEnterPAYEId(index) => cyaPage(mode, index, srn)
  }

  override protected def editrouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, srn), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(updateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  private def updateModeRoutes(mode: VarianceMode,
                               ua: UserAnswers,
                               srn: SchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_) => addTrusteesPage(mode, srn)
    case id@PartnershipHasUTRId(index) =>
      booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index, srn))
    case PartnershipEnterUTRId(index) => hasVat(mode, index, srn)
    case PartnershipNoUTRReasonId(index) => hasVat(mode, index, srn)
    case id@PartnershipHasVATId(index) => booleanNav(id, ua, enterVat(mode, index, srn), hasPaye(mode, index, srn))
    case PartnershipEnterVATId(index) => hasPaye(mode, index, srn)
    case id@PartnershipHasPAYEId(index) => booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case PartnershipEnterPAYEId(index) => cyaPage(mode, index, srn)
  }

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)

  private def checkUpdateModeRoutes(mode: VarianceMode,
                                    ua: UserAnswers,
                                    srn: SchemeReferenceNumber): PartialFunction[Identifier, Call] = {
    case id@PartnershipHasUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrReasonPage(mode, index,
      srn))
    case PartnershipEnterUTRId(index) if isNewTrustee(index, ua) => cyaPage(mode, index, srn)
    case PartnershipEnterUTRId(_) => anyMoreChangesPage(srn)
    case PartnershipNoUTRReasonId(index) if isNewTrustee(index, ua) => cyaPage(mode, index, srn)
    case PartnershipNoUTRReasonId(_) => anyMoreChangesPage(srn)
    case id@PartnershipHasVATId(index) => booleanNav(id, ua, enterVat(mode, index, srn), cyaPage(mode, index, srn))
    case PartnershipEnterVATId(index) if isNewTrustee(index, ua) => cyaPage(mode, index, srn)
    case PartnershipEnterVATId(_) => anyMoreChangesPage(srn)
    case id@PartnershipHasPAYEId(index) => booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case PartnershipEnterPAYEId(index) if isNewTrustee(index, ua) => cyaPage(mode, index, srn)
    case PartnershipEnterPAYEId(_) => anyMoreChangesPage(srn)
  }

  override protected def editRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =
    navigateTo(normalAndCheckModeRoutes(CheckMode, from.userAnswers, srn), from.id)

}

object TrusteesPartnershipDetailsNavigator {

  private def trusteeTaskList(index: Int, srn: SchemeReferenceNumber): Call =
    controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index, srn)

  private def isNewTrustee(index: Int, ua: UserAnswers): Boolean =
    ua.get(IsTrusteeNewId(index)).getOrElse(false)

  private def addTrusteesPage(mode: Mode, srn: SchemeReferenceNumber): Call =
    AddTrusteeController.onPageLoad(mode, srn)

  private def utrPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipEnterUTRController.onPageLoad(mode, index, srn)

  private def noUtrReasonPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipNoUTRReasonController.onPageLoad(mode, index, srn)

  private def hasVat(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipHasVATController.onPageLoad(mode, index, srn)

  private def enterVat(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipEnterVATController.onPageLoad(mode, index, srn)

  private def hasPaye(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipHasPAYEController.onPageLoad(mode, index, srn)

  private def payePage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    PartnershipEnterPAYEController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: SchemeReferenceNumber): Call =
    CheckYourAnswersPartnershipDetailsController.onPageLoad(journeyMode(mode), index, srn)

}
