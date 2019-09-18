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

package navigators.establishers.partnership

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models._
import navigators.AbstractNavigator
import play.api.mvc.Call
import utils.UserAnswers

class EstablisherPartnershipDetailsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  import EstablisherPartnershipDetailsNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipDetailsId(_) => addEstablisherPage(mode, srn)
    case PartnershipUniqueTaxReferenceID(index) => vatPage(mode, index, srn)
    case PartnershipVatId(index) => payePage(mode, index, srn)
    case PartnershipPayeId(index) => cyaPage(mode, index, srn)
  }

  private def checkModeRoutes(mode: SubscriptionMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipUniqueTaxReferenceID(index) => cyaPage(mode, index, srn)
    case PartnershipVatId(index) => cyaPage(mode, index, srn)
    case PartnershipPayeId(index) => cyaPage(mode, index, srn)
  }

  private def checkUpdateModeRoutes(mode: VarianceMode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case PartnershipUniqueTaxReferenceID(index) if ua.get(IsEstablisherNewId(index)).getOrElse(false) => cyaPage(mode, index, srn)
    case PartnershipUniqueTaxReferenceID(_) => anyMoreChangesPage(srn)
    case PartnershipVatId(index) => cyaPage(mode, index, srn)
    case PartnershipEnterVATId(index) => anyMoreChangesPage(srn)
    case PartnershipPayeId(index) => cyaPage(mode, index, srn)
    case PartnershipPayeVariationsId(index) => anyMoreChangesPage(srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(normalAndUpdateModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    navigateTo(checkModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(normalAndUpdateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    navigateTo(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object EstablisherPartnershipDetailsNavigator {
  private def addEstablisherPage(mode: Mode, srn: Option[String]): Call =
    controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)

  private def vatPage(mode: Mode, index: Index, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.PartnershipVatController.onPageLoad(mode, index, srn)

  private def payePage(mode: Mode, index: Index, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.PartnershipPayeController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Index, srn: Option[String]): Call =
    controllers.register.establishers.partnership.routes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)
}

