/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.VariationDeclarationId
import identifiers.register._
import models.NormalMode
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                  appConfig: FrontendAppConfig) extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case ContinueRegistrationId =>
        continueRegistration(from.userAnswers)
      case DeclarationDormantId =>
        NavigateTo.dontSave(controllers.register.routes.DeclarationController.onPageLoad)
      case DeclarationId =>
        NavigateTo.dontSave(controllers.register.routes.SchemeSuccessController.onPageLoad())
      case _ => None
    }

  private def continueRegistration(userAnswers: UserAnswers): Option[NavigateTo] =
    if (userAnswers.isBeforeYouStartCompleted(NormalMode))
      NavigateTo.dontSave(controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None))
    else
      NavigateTo.dontSave(controllers.routes.BeforeYouStartController.onPageLoad())

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case _ => None
    }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = (from.id, srn) match {
    case (VariationDeclarationId, Some(validSrn)) => NavigateTo.dontSave(controllers.register.routes
      .SchemeVariationsSuccessController.onPageLoad(validSrn))
    case _ => None
  }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}
