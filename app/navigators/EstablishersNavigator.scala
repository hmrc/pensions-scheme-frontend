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
import connectors.DataCacheConnector
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.{AddEstablisherId, ConfirmDeleteEstablisherId, EstablisherKindId}
import identifiers.register.trustees.HaveAnyTrusteesId
import models.NormalMode
import models.register.establishers.EstablisherKind
import models.register.{SchemeDetails, SchemeType}
import utils.{Enumerable, Navigator, UserAnswers}

class EstablishersNavigator @Inject()(val dataCacheConnector: DataCacheConnector, config: FrontendAppConfig) extends Navigator with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case AddEstablisherId(value) => addEstablisherRoutes(value, from.userAnswers)
      case EstablisherKindId(index) => establisherKindRoutes(index, from.userAnswers)
      case ConfirmDeleteEstablisherId =>
        NavigateTo.save(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode))
      case _ => None
    }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  private def addEstablisherRoutes(value: Option[Boolean], answers: UserAnswers): Option[NavigateTo] = {
    value match {
      case Some(false) =>
        navigateBasedOnSchemeDetails(answers)
      case Some(true) =>
        NavigateTo.save(controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, answers.establishersCount))
      case None =>
        NavigateTo.save(controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, answers.establishersCount))
    }
  }

  private def navigateBasedOnSchemeDetails(answers: UserAnswers): Option[NavigateTo] = {
    val listOfSchemeTypeTrusts: Seq[SchemeType] = Seq(SchemeType.SingleTrust, SchemeType.MasterTrust)
    answers.get(SchemeDetailsId) match {
      case Some(SchemeDetails(_, schemeType)) if listOfSchemeTypeTrusts.contains(schemeType) =>
        NavigateTo.save(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
      case Some(SchemeDetails(_, _)) =>
        answers.allTrusteesAfterDelete.isEmpty match {
          case false =>
            NavigateTo.save(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
          case _ =>
            answers.get(HaveAnyTrusteesId) match {
              case Some(true) =>
                NavigateTo.save(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
              case Some(false) =>
                NavigateTo.save(controllers.register.routes.SchemeReviewController.onPageLoad())
              case _ =>
                NavigateTo.save(controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode))
            }
        }
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def establisherKindRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(EstablisherKindId(index)) match {
      case Some(EstablisherKind.Company) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, index))
      case Some(EstablisherKind.Indivdual) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, index))
      case Some(EstablisherKind.Partnership) =>
        NavigateTo.save(controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, index))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

}
