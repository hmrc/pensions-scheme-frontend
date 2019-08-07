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
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.UserAnswersCacheConnector
import identifiers.EstablishersOrTrusteesChangedId
import identifiers.register.trustees._
import models.register.trustees.TrusteeKind
import models._
import play.api.mvc.Call
import utils.{Enumerable, Toggles, UserAnswers}

class TrusteesNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig,
                                  featureSwitchManagementService: FeatureSwitchManagementService) extends AbstractNavigator with Enumerable.Implicits {

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case HaveAnyTrusteesId =>
        haveAnyTrusteesRoutes(from.userAnswers)
      case AddTrusteeId =>
        addTrusteeRoutes(from.userAnswers, mode, srn)
      case MoreThanTenTrusteesId =>
        redirectToAnyMoreChanges(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn), mode, srn)
      case TrusteeKindId(index) =>
        trusteeKindRoutes(index, from.userAnswers, mode, srn)
      case ConfirmDeleteTrusteeId =>
        redirectToAnyMoreChanges(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn), mode, srn)
      case _ => None
    }

  private def redirectToAnyMoreChanges(normalModeRoutes: Call, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    if(mode == CheckMode || mode == NormalMode){
      NavigateTo.dontSave(normalModeRoutes)
    } else {
      NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = routes(from, UpdateMode, srn)

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  private def toggled:Boolean = featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)

  private def haveAnyTrusteesRoutes(answers: UserAnswers): Option[NavigateTo] = {

    answers.get(HaveAnyTrusteesId) match {
      case Some(true) =>
        if (answers.allTrusteesAfterDelete(toggled).isEmpty) {
          NavigateTo.dontSave(controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, answers.allTrustees(toggled).size, None))
        } else {
          NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None))
        }
      case Some(false) =>
        NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addTrusteeRoutes(answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    import controllers.register.trustees.routes._
    //TODO PODS-2940 Needs attention to trustees count
    val trusteesLengthCompare = answers.allTrustees(toggled).lengthCompare(appConfig.maxTrustees)

    answers.get(AddTrusteeId) match {
      case Some(false) =>
        if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
          NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
        } else {
          mode match {
            case UpdateMode | CheckUpdateMode if answers.get(EstablishersOrTrusteesChangedId).contains(true) =>
              NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
            case _ =>
              NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
          }
        }
      case Some(true) =>
        NavigateTo.dontSave(TrusteeKindController.onPageLoad(mode, answers.trusteesCount, srn))
      case None if trusteesLengthCompare >= 0 =>
        NavigateTo.dontSave(MoreThanTenTrusteesController.onPageLoad(mode, srn))
      case None =>
        NavigateTo.dontSave(TrusteeKindController.onPageLoad(mode, answers.trusteesCount, srn))
    }
  }

  private def trusteeKindRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(TrusteeKindId(index)) match {
      case Some(TrusteeKind.Company) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(mode, index, srn))
      case Some(TrusteeKind.Individual) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(mode, index, srn))
      case Some(TrusteeKind.Partnership) =>
        NavigateTo.dontSave(controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(mode, index, srn))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
