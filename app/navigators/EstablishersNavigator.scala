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
import controllers.routes.{AnyMoreChangesController, PsaSchemeTaskListController}
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId, MoreThanTenEstablishersId}
import models.register.establishers.EstablisherKind
import models.{CheckMode, Mode, NormalMode, UpdateMode}
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

class EstablishersNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                      config: FrontendAppConfig
                                     ) extends AbstractNavigator with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] =
    routes(from, UpdateMode, srn)

  protected def routes(from: NavigateFrom,
                       mode: Mode,
                       srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case AddEstablisherId(value) => addEstablisherRoutes(value, from.userAnswers, mode, srn)
      case MoreThanTenEstablishersId => redirectToAnyMoreChanges(PsaSchemeTaskListController.onPageLoad(mode, srn), mode, srn)
      case EstablisherKindId(index) => establisherKindRoutes(index, from.userAnswers, mode, srn)
      case ConfirmDeleteEstablisherId =>
        mode match {
          case CheckMode | NormalMode =>
            NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
          case _ =>
            NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
        }
      case _ => None
    }

  private def redirectToAnyMoreChanges(normalModeRoutes: Call, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    if (mode == CheckMode || mode == NormalMode) {
      NavigateTo.dontSave(normalModeRoutes)
    } else {
      NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
    }
  }

  private def addEstablisherRoutes(value: Option[Boolean],
                                   answers: UserAnswers,
                                   mode: Mode,
                                   srn: Option[String]): Option[NavigateTo] = {
    value match {
      case Some(false) =>
        NavigateTo.dontSave(controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn))
      case Some(true) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode,
          answers.establishersCount, srn))
      case None =>
        NavigateTo.dontSave(controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode,
          answers.establishersCount, srn))
    }
  }

  private def establisherKindRoutes(index: Int,
                                    answers: UserAnswers,
                                    mode: Mode,
                                    srn: Option[String]): Option[NavigateTo] = {
    answers.get(EstablisherKindId(index)) match {
      case Some(EstablisherKind.Company) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes
          .CompanyDetailsController.onPageLoad(mode, srn, index))
      case Some(EstablisherKind.Indivdual) =>
        NavigateTo.dontSave(controllers.register.establishers.individual.routes
          .EstablisherNameController.onPageLoad(mode, index, srn))
      case Some(EstablisherKind.Partnership) =>
        NavigateTo.dontSave(controllers.register.establishers.partnership.routes
          .PartnershipDetailsController.onPageLoad(mode, index, srn))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None
}
