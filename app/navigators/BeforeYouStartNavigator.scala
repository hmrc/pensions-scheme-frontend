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
import controllers.routes._
import identifiers.register.{DeclarationDutiesId, SchemeEstablishedCountryId}
import identifiers.{EstablishedCountryId, HaveAnyTrusteesId, SchemeNameId, SchemeTypeId}
import models.NormalMode
import models.register.SchemeType
import utils.{Navigator, UserAnswers}

class BeforeYouStartNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.save(controllers.register.routes.CheckYourAnswersController.onPageLoad())


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case SchemeNameId => NavigateTo.dontSave(SchemeTypeController.onPageLoad(NormalMode))
    case SchemeTypeId => schemeTypeRoutes(from.userAnswers)
    case HaveAnyTrusteesId => NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode))
    case EstablishedCountryId => NavigateTo.dontSave(WorkingKnowledgeController.onPageLoad(NormalMode))
    case DeclarationDutiesId => checkYourAnswers
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case SchemeNameId => checkYourAnswers
    case SchemeTypeId => schemeTypeRoutes(from.userAnswers)
    case HaveAnyTrusteesId => checkYourAnswers
    case SchemeEstablishedCountryId => checkYourAnswers
    case DeclarationDutiesId => checkYourAnswers
  }

  private def schemeTypeRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) | Some(SchemeType.MasterTrust) =>
        NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode))
      case Some(_) =>
        NavigateTo.dontSave(HaveAnyTrusteesController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }
}
