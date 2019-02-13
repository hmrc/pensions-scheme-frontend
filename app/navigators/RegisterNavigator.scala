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
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.register._
import identifiers.{IsBeforeYouStartCompleteId, UserResearchDetailsId}
import models.{CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                  appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case ContinueRegistrationId =>
        continueRegistration(from.userAnswers)
      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad())
      case DeclarationDormantId =>
        NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
      case DeclarationId =>
        NavigateTo.dontSave(controllers.register.routes.SchemeSuccessController.onPageLoad())
      case DeclarationDutiesId =>
        declarationDutiesRoutes(NormalMode, from.userAnswers)
      case UserResearchDetailsId => NavigateTo.dontSave(appConfig.managePensionsSchemeOverviewUrl)
      case _ => None
    }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case DeclarationDutiesId =>
        declarationDutiesRoutes(CheckMode, from.userAnswers)
      case _ => None
    }


  private def declarationDutiesRoutes(mode: Mode, userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(DeclarationDutiesId) match {
      case Some(true) =>
        NavigateTo.save(controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad())
      case Some(false) =>
        NavigateTo.save(controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def continueRegistration(userAnswers: UserAnswers): Option[NavigateTo] =
    userAnswers.get(IsBeforeYouStartCompleteId) match {
      case Some(true) =>
        NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad())
      case _ =>
        NavigateTo.dontSave(controllers.routes.BeforeYouStartController.onPageLoad())
    }
}
