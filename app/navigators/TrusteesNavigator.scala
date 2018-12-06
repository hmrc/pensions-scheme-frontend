/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.trustees._
import models.NormalMode
import models.register.trustees.TrusteeKind
import utils.{Enumerable, Navigator, UserAnswers}

class TrusteesNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case HaveAnyTrusteesId =>
        haveAnyTrusteesRoutes(from.userAnswers)
      case AddTrusteeId =>
        addTrusteeRoutes(from.userAnswers)
      case MoreThanTenTrusteesId =>
        if (appConfig.isHubEnabled) {
          NavigateTo.dontSave(controllers.register.routes.SchemeTaskListController.onPageLoad())
        } else {
          NavigateTo.save(controllers.register.routes.SchemeReviewController.onPageLoad())
        }
      case TrusteeKindId(index) =>
        trusteeKindRoutes(index, from.userAnswers)
      case ConfirmDeleteTrusteeId =>
        NavigateTo.save(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
      case _ => None
    }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case AddTrusteeId =>
        addTrusteeRoutes(from.userAnswers)
      case _ => None
    }

  private def haveAnyTrusteesRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HaveAnyTrusteesId) match {
      case Some(true) =>
        if(appConfig.isHubEnabled && answers.allTrusteesAfterDelete.isEmpty) {
          NavigateTo.dontSave(controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0))
        } else {
          NavigateTo.save(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
        }
      case Some(false) =>
        if(appConfig.isHubEnabled){
          NavigateTo.dontSave(controllers.register.routes.SchemeTaskListController.onPageLoad())
        } else {
          NavigateTo.save(controllers.register.routes.SchemeReviewController.onPageLoad())
        }
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addTrusteeRoutes(answers: UserAnswers): Option[NavigateTo] = {
    import controllers.register.trustees.routes._
    val trusteesLengthCompare = answers.allTrustees.lengthCompare(appConfig.maxTrustees)

    answers.get(AddTrusteeId) match {
      case Some(false) =>
        if (appConfig.isHubEnabled) {
          NavigateTo.dontSave(controllers.register.routes.SchemeTaskListController.onPageLoad())
        } else {
          NavigateTo.save(controllers.register.routes.SchemeReviewController.onPageLoad())
        }
      case Some(true) =>
        NavigateTo.save(TrusteeKindController.onPageLoad(NormalMode, answers.trusteesCount))
      case None if trusteesLengthCompare >= 0 =>
        NavigateTo.save(MoreThanTenTrusteesController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.save(TrusteeKindController.onPageLoad(NormalMode, answers.trusteesCount))
    }
  }

  private def trusteeKindRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(TrusteeKindId(index)) match {
      case Some(TrusteeKind.Company) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index))
      case Some(TrusteeKind.Individual) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, index))
      case Some(TrusteeKind.Partnership) =>
        NavigateTo.save(controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(NormalMode, index))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

}
