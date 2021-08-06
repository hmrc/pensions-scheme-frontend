/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.racdac.{ContractOrPolicyNumberId, DeclarationId, RACDACNameId}
import models.NormalMode
import utils.annotations.Racdac

class RACDACNavigator @Inject()(@Racdac val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case RACDACNameId => NavigateTo.dontSave(controllers.racdac.routes.ContractOrPolicyNumberController.onPageLoad(NormalMode))
    case ContractOrPolicyNumberId => NavigateTo.dontSave(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(NormalMode, None))
    case DeclarationId => NavigateTo.dontSave(controllers.racdac.routes.SchemeSuccessController.onPageLoad())
    case _ => None
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case RACDACNameId => NavigateTo.dontSave(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(NormalMode, None))
    case ContractOrPolicyNumberId => NavigateTo.dontSave(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(NormalMode, None))
    case _ => None
  }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}
