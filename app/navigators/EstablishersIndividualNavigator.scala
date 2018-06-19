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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import identifiers.Identifier
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.individual._
import identifiers.register.trustees.HaveAnyTrusteesId
import models.register.{SchemeDetails, SchemeType}
import models.{AddressYears, CheckMode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersIndividualNavigator @Inject()(appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int)(answers: UserAnswers): Call =
    controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case EstablisherDetailsId(index) =>
      _ => controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(NormalMode, index)
    case EstablisherNinoId(index) =>
      _ => controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index)
    case UniqueTaxReferenceId(index) =>
      _ => controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(NormalMode, index)
    case PostCodeLookupId(index) =>
      _ => controllers.register.establishers.individual.routes.AddressListController.onPageLoad(NormalMode, index)
    case AddressListId(index) =>
      _ => controllers.register.establishers.individual.routes.AddressController.onPageLoad(NormalMode, index)
    case AddressId(index) =>
      _ => controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(NormalMode, index)
    case AddressYearsId(index) =>
      addressYearsRoutes(index)
    case PreviousPostCodeLookupId(index) =>
      _ => controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(NormalMode, index)
    case PreviousAddressListId(index) =>
      _ => controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(NormalMode, index)
    case PreviousAddressId(index) =>
      _ => controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, index)
    case ContactDetailsId(index) =>
      checkYourAnswers(index)
    case CheckYourAnswersId =>
      _ => controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode)
  }

  override protected val editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case EstablisherDetailsId(index) =>
      checkYourAnswers(index)
    case EstablisherNinoId(index) =>
      checkYourAnswers(index)
    case UniqueTaxReferenceId(index) =>
      checkYourAnswers(index)
    case PostCodeLookupId(index) =>
      _ => controllers.register.establishers.individual.routes.AddressListController.onPageLoad(CheckMode, index)
    case AddressListId(index) =>
      _ => controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, index)
    case AddressId(index) =>
      checkYourAnswers(index)
    case AddressYearsId(index) =>
      addressYearsEditRoutes(index)
    case PreviousPostCodeLookupId(index) =>
      _ => controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(CheckMode, index)
    case PreviousAddressListId(index) =>
      _ => controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, index)
    case PreviousAddressId(index) =>
      checkYourAnswers(index)
    case ContactDetailsId(index) =>
      checkYourAnswers(index)
  }

  private def addressYearsRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(AddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index)
      case Some(AddressYears.OverAYear) =>
        controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(NormalMode, index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def addressYearsEditRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(AddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index)
      case Some(AddressYears.OverAYear) =>
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}
