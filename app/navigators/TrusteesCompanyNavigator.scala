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
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.register.trustees.company._
import models.{AddressYears, CheckMode, NormalMode}
import utils.{Navigator, UserAnswers}

class TrusteesCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                         appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onPageLoad(NormalMode, index, None))

      case CompanyRegistrationNumberId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, index, None))

      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, index, None))

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(NormalMode, index, None))

      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(NormalMode, index, None))

      case CompanyAddressId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, index, None))

      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, index, None))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode, index, None))

      case CompanyPreviousAddressId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index, None))

      case CompanyContactDetailsId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))

      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode))
      case _ => None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        checkYourAnswers(index, from.userAnswers)

      case CompanyRegistrationNumberId(index) =>
        checkYourAnswers(index, from.userAnswers)

      case CompanyUniqueTaxReferenceId(index) =>
        checkYourAnswers(index, from.userAnswers)

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(CheckMode, index, None))

      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(CheckMode, index, None))

      case CompanyAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, index, None))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index, None))

      case CompanyPreviousAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)

      case CompanyContactDetailsId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case _ => None
    }
  }

  private def checkYourAnswers(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    NavigateTo.save(controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))
  }

  private def addressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, index, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, index, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
