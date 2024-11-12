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
import controllers.routes._
import identifiers._
import models.register.SchemeType
import models.{CheckMode, NormalMode}
import utils.UserAnswers
import models.SchemeReferenceNumber

class BeforeYouStartNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                        frontendAppConfig: FrontendAppConfig) extends AbstractNavigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case SchemeNameId => NavigateTo.dontSave(SchemeTypeController.onPageLoad(NormalMode))
    case SchemeTypeId => schemeTypeRoutes(from.userAnswers)
    case HaveAnyTrusteesId => NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode))
    case EstablishedCountryId => NavigateTo.dontSave(WorkingKnowledgeController.onPageLoad(NormalMode))
    case DeclarationDutiesId => checkYourAnswers
    case _ => None
  }

  private def schemeTypeRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) | Some(SchemeType.MasterTrust) =>
        NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode))
      case Some(_) =>
        NavigateTo.dontSave(HaveAnyTrusteesController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case SchemeNameId => checkYourAnswers
    case SchemeTypeId => schemeTypeEditRoutes(from.userAnswers)
    case HaveAnyTrusteesId => checkYourAnswers
    case EstablishedCountryId => checkYourAnswers
    case DeclarationDutiesId => checkYourAnswers
    case _ => None
  }

  private def schemeTypeEditRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) | Some(SchemeType.MasterTrust) =>
        checkYourAnswers
      case Some(_) =>
        NavigateTo.dontSave(HaveAnyTrusteesController.onPageLoad(CheckMode))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, None))

  protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = None
}
