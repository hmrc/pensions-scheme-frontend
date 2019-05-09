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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.register.trustees.individual._
import models.{AddressYears, CheckMode, CheckUpdateMode, Mode, NormalMode, UpdateMode}
import models.Mode.journeyMode
import utils.{Navigator, UserAnswers}

@Singleton
class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                            appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(mode, index, srn))

  //scalastyle:off cyclomatic.complexity
  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case TrusteeDetailsId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(mode, index, srn))
      case TrusteeNinoId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, index, srn))
      case UniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(mode, index, srn))
      case IndividualPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(mode, index, srn))
      case IndividualAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(mode, index, srn))
      case TrusteeAddressId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(mode, index, srn))
      case TrusteeAddressYearsId(index) =>
        addressYearsRoutes(index, mode, srn)(from.userAnswers)
      case IndividualPreviousAddressPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(mode, index, srn))
      case TrusteePreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn))
      case TrusteePreviousAddressId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(mode, index, srn))
      case TrusteeContactDetailsId(index) =>
        checkYourAnswers(index, mode, srn)(from.userAnswers)
      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn))
      case _ =>
        None
    }
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case TrusteeDetailsId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case TrusteeNinoId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case UniqueTaxReferenceId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case IndividualPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(mode, index, srn))
      case IndividualAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(mode, index, srn))
      case TrusteeAddressId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case TrusteeAddressYearsId(index) => editAddressYearsRoutes(index, mode, srn)(from.userAnswers)
      case IndividualPreviousAddressPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(mode, index, srn))
      case TrusteePreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn))
      case TrusteePreviousAddressId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case TrusteeContactDetailsId(index) => checkYourAnswers(index, journeyMode(mode), srn)(from.userAnswers)
      case _ =>
        None
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = routes(from, UpdateMode, srn)

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(journeyMode(mode), index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
