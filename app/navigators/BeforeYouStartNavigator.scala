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

  override protected def routeMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = from.id match {
    case SchemeNameId => NavigateTo.dontSave(SchemeTypeController.onPageLoad(NormalMode, srn))
    case SchemeTypeId => schemeTypeRoutes(from.userAnswers, srn)
    case HaveAnyTrusteesId => NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode, srn))
    case EstablishedCountryId => NavigateTo.dontSave(WorkingKnowledgeController.onPageLoad(NormalMode, srn))
    case DeclarationDutiesId => checkYourAnswers(srn)
    case _ => None
  }

  private def schemeTypeRoutes(answers: UserAnswers, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) | Some(SchemeType.MasterTrust) =>
        NavigateTo.dontSave(EstablishedCountryController.onPageLoad(NormalMode, srn))
      case Some(_) =>
        NavigateTo.dontSave(HaveAnyTrusteesController.onPageLoad(NormalMode, srn))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }


  private def schemeTypeEditRoutes(answers: UserAnswers, srn: SchemeReferenceNumber): Option[NavigateTo] = {
    answers.get(SchemeTypeId) match {
      case Some(SchemeType.SingleTrust) | Some(SchemeType.MasterTrust) =>
        checkYourAnswers(srn)
      case Some(_) =>
        NavigateTo.dontSave(HaveAnyTrusteesController.onPageLoad(CheckMode, srn))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad)
    }
  }

  private def checkYourAnswers(srn: SchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, srn))

  protected def updateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] = None

  override protected def editRouteMap(from: NavigateFrom, srn: SchemeReferenceNumber): Option[NavigateTo] =  from.id match {
    case SchemeNameId => checkYourAnswers(srn)
    case SchemeTypeId => schemeTypeEditRoutes(from.userAnswers, srn)
    case HaveAnyTrusteesId => checkYourAnswers(srn)
    case EstablishedCountryId => checkYourAnswers(srn)
    case DeclarationDutiesId => checkYourAnswers(srn)
    case _ => None
  }
}
