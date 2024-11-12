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

package navigators

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import identifiers.AnyMoreChangesId
import models.UpdateMode
import utils.Enumerable
import models.SchemeReferenceNumber

class VariationsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator
    with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = None

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case AnyMoreChangesId => from.userAnswers.get(AnyMoreChangesId) match {
        case Some(true) => NavigateTo.dontSave(controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, srn))
        case Some(false) =>
          if (from.userAnswers.areVariationChangesCompleted)
            NavigateTo.dontSave(controllers.routes.VariationDeclarationController.onPageLoad(srn))
          else
            NavigateTo.dontSave(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn))
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
      }
      case _ => None
    }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = None

}
