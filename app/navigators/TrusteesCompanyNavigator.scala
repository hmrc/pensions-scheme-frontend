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
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import models._
import models.Mode.journeyMode
import utils.{Navigator, UserAnswers}

class TrusteesCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                         appConfig: FrontendAppConfig) extends Navigator {
  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    NavigateTo.dontSave(controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(mode, index, srn))
  }
  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode) {
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      anyMoreChanges(srn)
    }
  
  //scalastyle:off cyclomatic.complexity
  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyVatController.onPageLoad(mode, index, srn))

      case CompanyVatId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPayeController.onPageLoad(mode, index, srn))

      case CompanyPayeId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, index, srn))

      case CompanyRegistrationNumberId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, index, srn))

      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, index, srn))

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(mode, index, srn))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(mode, index, srn))

      case CompanyAddressId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(mode, index, srn))

      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(mode, index, srn))

      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(mode, index, srn))

      case CheckYourAnswersId =>
        NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn))
      case _ => None
    }
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)

      case CompanyVatId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)

      case CompanyPayeId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)

      case CompanyRegistrationNumberId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)

      case CompanyUniqueTaxReferenceId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(mode, index, srn))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(mode, index, srn))

      case CompanyAddressId(index) =>
        val isNew = from.userAnswers.get(IsTrusteeNewId(index)).contains(true)
        if(isNew || mode == CheckMode) {
          checkYourAnswers(index, journeyMode(mode), srn)
        } else {
          NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(mode, index, srn))
        }

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressId(index) =>
        exitMiniJourney(index, mode, srn)

      case CompanyContactDetailsId(index) =>
        checkYourAnswers(index, journeyMode(mode), srn)
      case _ => None
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)
  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = routes(from, UpdateMode, srn)
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)
  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        exitMiniJourney(index, mode, srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
