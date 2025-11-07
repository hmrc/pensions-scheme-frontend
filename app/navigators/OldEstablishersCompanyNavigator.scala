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
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.company.routes as establisherCompanyRoutes
import controllers.routes.*
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.*
import models.*
import models.Mode.*
import utils.UserAnswers

//scalastyle:off cyclomatic.complexity
class OldEstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {

  private def exitMiniJourney(
                               index: Int,
                               mode: Mode,
                               srn: OptionalSchemeReferenceNumber,
                               answers: UserAnswers,
                               cyaPage: (Int, Mode, OptionalSchemeReferenceNumber) => Option[NavigateTo]
                             ): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode)
      cyaPage(index, journeyMode(mode), srn)
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage(index, journeyMode(mode), srn)
    else
      anyMoreChanges(srn)


  private def cyaCompanyDetails(index: Int, mode: Mode, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))
  
  private def anyMoreChanges(srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))

  protected def routes(from: NavigateFrom, mode: Mode, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
      case _ =>
        None
    }
  
  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case _ =>
        None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    routes(from, NormalMode, EmptyOptionalSchemeReferenceNumber)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    editRoutes(from, CheckMode, EmptyOptionalSchemeReferenceNumber)

  override protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    routes(from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    editRoutes(from, CheckUpdateMode, srn)

}
