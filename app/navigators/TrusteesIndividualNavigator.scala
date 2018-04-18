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
import identifiers.Identifier
import identifiers.register.trustees.individual._
import models.{AddressYears, CheckMode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class TrusteesIndividualNavigator @Inject() extends Navigator {

  private def checkYourAnswers(index: Int)(answers: UserAnswers): Call =
    controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case TrusteeDetailsId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(NormalMode, index)
    case TrusteeNinoId(index) =>
      _ => controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(NormalMode, index)
    case UniqueTaxReferenceId(index) =>
      _ => controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(NormalMode, index)
    case IndividualPostCodeLookupId(index) =>
      _ => controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(NormalMode, index)
    case IndividualAddressListId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(NormalMode, index)
    case TrusteeAddressId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(NormalMode, index)
    case TrusteeAddressYearsId(index) =>
      addressYearsRoutes(index)
    case IndividualPreviousAddressPostCodeLookupId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(NormalMode, index)
    case TrusteePreviousAddressListId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(NormalMode, index)
    case TrusteePreviousAddressId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, index)
    case TrusteeContactDetailsId(index) =>
      _ => controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
  }

  override protected val editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case TrusteeDetailsId(index) => checkYourAnswers(index)
    case TrusteeNinoId(index) => checkYourAnswers(index)
    case UniqueTaxReferenceId(index) => checkYourAnswers(index)
    case IndividualPostCodeLookupId(index) =>
      _ => controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(CheckMode, index)
    case IndividualAddressListId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(CheckMode, index)
    case TrusteeAddressId(index) => checkYourAnswers(index)
    case TrusteeAddressYearsId(index) => editAddressYearsRoutes(index)
    case IndividualPreviousAddressPostCodeLookupId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(CheckMode, index)
    case TrusteePreviousAddressListId(index) =>
      _ => controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(CheckMode, index)
    case TrusteePreviousAddressId(index) => checkYourAnswers(index)
    case TrusteeContactDetailsId(index) => checkYourAnswers(index)
  }

  private def addressYearsRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, index)
      case Some(AddressYears.OverAYear) =>
        controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(NormalMode, index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def editAddressYearsRoutes(index: Int)(answers: UserAnswers): Call = {
    answers.get(TrusteeAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, index)
      case Some(AddressYears.OverAYear) =>
        controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(index)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}
