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
import identifiers.register.establishers.individual._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersIndividualNavigator @Inject()(
                                                 appConfig: FrontendAppConfig,
                                                 val dataCacheConnector: UserAnswersCacheConnector
                                               ) extends Navigator {

  private def checkYourAnswers(index: Int)(answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case EstablisherDetailsId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(NormalMode, index, None))
      case EstablisherNinoId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index, None))
      case UniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(NormalMode, index, None))
      case PostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.AddressListController.onPageLoad(NormalMode, index, None))
      case AddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.AddressController.onPageLoad(NormalMode, index, None))
      case AddressId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(NormalMode, index, None))
      case AddressYearsId(index) =>
        addressYearsRoutes(index)(from.userAnswers)
      case PreviousPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(NormalMode, index, None))
      case PreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(NormalMode, index, None))
      case PreviousAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, index, None))
      case ContactDetailsId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None))
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case EstablisherDetailsId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case EstablisherNinoId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case UniqueTaxReferenceId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case PostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.AddressListController.onPageLoad(CheckMode, index, None))
      case AddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, index, None))
      case AddressId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case AddressYearsId(index) =>
        addressYearsEditRoutes(index)(from.userAnswers)
      case PreviousPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(CheckMode, index, None))
      case PreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, index, None))
      case PreviousAddressId(index) =>
        checkYourAnswers(index)(from.userAnswers)
      case ContactDetailsId(index) =>
        checkYourAnswers(index)(from.userAnswers)
    }
  }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  private def addressYearsRoutes(index: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsEditRoutes(index: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
