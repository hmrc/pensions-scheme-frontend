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
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.{CurrentMembersId, FutureMembersId, MembershipPensionRegulatorId}
import models.{CheckMode, Members, NormalMode}
import utils.{Enumerable, UserAnswers}
import models.SchemeReferenceNumber

class AboutMembersNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                      appConfig: FrontendAppConfig
                                     ) extends AbstractNavigator with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case CurrentMembersId =>
        currentMembersNavigationRoutes(from.userAnswers)
      case MembershipPensionRegulatorId =>
        NavigateTo.dontSave(controllers.routes.FutureMembersController.onPageLoad(NormalMode))
      case FutureMembersId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None))
      case _ =>
        None
    }
  }

  private def currentMembersNavigationRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(CurrentMembersId) match {
      case Some(Members.None) | Some(Members.One) =>
        NavigateTo.dontSave(controllers.routes.FutureMembersController.onPageLoad(NormalMode))
      case Some(_) =>
        NavigateTo.dontSave(controllers.routes.MembershipPensionRegulatorController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case CurrentMembersId =>
        currentMembersNavigationEditRoutes(from.userAnswers)
      case MembershipPensionRegulatorId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None))
      case FutureMembersId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None))
      case _ =>
        None
    }
  }

  private def currentMembersNavigationEditRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(CurrentMembersId) match {
      case Some(Members.None) | Some(Members.One) =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None))
      case Some(_) =>
        NavigateTo.dontSave(controllers.routes.MembershipPensionRegulatorController.onPageLoad(CheckMode))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = None
}
