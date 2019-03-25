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
import models.{AddressYears, CheckMode, NormalMode}
import utils.{Navigator, UserAnswers}

@Singleton
class TrusteesIndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                            appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int)(answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case TrusteeDetailsId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(NormalMode, index, None))
      case TrusteeNinoId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index, None))
      case UniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(NormalMode, index, None))
      case IndividualPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(NormalMode, index, None))
      case IndividualAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(NormalMode, index, None))
      case TrusteeAddressId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(NormalMode, index, None))
      case TrusteeAddressYearsId(index) =>
        addressYearsRoutes(index)(from.userAnswers)
      case IndividualPreviousAddressPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(NormalMode, index, None))
      case TrusteePreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(NormalMode, index, None))
      case TrusteePreviousAddressId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, index, None))
      case TrusteeContactDetailsId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))
      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad())
      case _ =>
        None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case TrusteeDetailsId(index) => checkYourAnswers(index)(from.userAnswers)
      case TrusteeNinoId(index) => checkYourAnswers(index)(from.userAnswers)
      case UniqueTaxReferenceId(index) => checkYourAnswers(index)(from.userAnswers)
      case IndividualPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(CheckMode, index, None))
      case IndividualAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(CheckMode, index, None))
      case TrusteeAddressId(index) => checkYourAnswers(index)(from.userAnswers)
      case TrusteeAddressYearsId(index) => editAddressYearsRoutes(index)(from.userAnswers)
      case IndividualPreviousAddressPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(CheckMode, index, None))
      case TrusteePreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(CheckMode, index, None))
      case TrusteePreviousAddressId(index) => checkYourAnswers(index)(from.userAnswers)
      case TrusteeContactDetailsId(index) => checkYourAnswers(index)(from.userAnswers)
      case _ =>
        None
    }
  }

  private def addressYearsRoutes(index: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
