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

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case CurrentMembersId =>
        currentMembersNavigationRoutes(from.userAnswers, srn)
      case MembershipPensionRegulatorId =>
        NavigateTo.dontSave(controllers.routes.FutureMembersController.onPageLoad(NormalMode, srn))
      case FutureMembersId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, srn))
      case _ =>
        None
    }
  }

  private def currentMembersNavigationRoutes(userAnswers: UserAnswers, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(CurrentMembersId) match {
      case Some(Members.None) | Some(Members.One) =>
        NavigateTo.dontSave(controllers.routes.FutureMembersController.onPageLoad(NormalMode, srn))
      case Some(_) =>
        NavigateTo.dontSave(controllers.routes.MembershipPensionRegulatorController.onPageLoad(NormalMode, srn))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case CurrentMembersId =>
        currentMembersNavigationEditRoutes(from.userAnswers, srn)
      case MembershipPensionRegulatorId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, srn))
      case FutureMembersId =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, srn))
      case _ =>
        None
    }
  }

  private def currentMembersNavigationEditRoutes(userAnswers: UserAnswers, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(CurrentMembersId) match {
      case Some(Members.None) | Some(Members.One) =>
        NavigateTo.dontSave(controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, srn))
      case Some(_) =>
        NavigateTo.dontSave(controllers.routes.MembershipPensionRegulatorController.onPageLoad(CheckMode, srn))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None
}
