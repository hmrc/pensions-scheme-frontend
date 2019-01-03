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
import controllers.register.routes
import identifiers.register.{DeclarationDutiesId, SchemeEstablishedCountryId, SchemeNameId, SchemeTypeId}
import models.NormalMode
import models.register.SchemeType
import utils.{Navigator, UserAnswers}

class BeforeYouStartNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case SchemeNameId => NavigateTo.dontSave(routes.SchemeTypeController.onPageLoad(NormalMode))
    case SchemeTypeId => schemeTypeRoutes(from.userAnswers)
    case SchemeEstablishedCountryId => NavigateTo.dontSave(controllers.routes.WorkingKnowledgeController.onPageLoad(NormalMode))
    case DeclarationDutiesId => checkYourAnswers
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = ???

  private def schemeTypeRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) =>
        NavigateTo.save(controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode))
      case Some(SchemeType.MasterTrust) =>
        NavigateTo.save(controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode))
      case Some(_) => NavigateTo.dontSave(routes.SchemeEstablishedCountryController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
