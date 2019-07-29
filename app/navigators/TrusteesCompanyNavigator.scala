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
import controllers.register.trustees.company.routes._
import controllers.register.trustees.routes._
import controllers.routes._
import identifiers.register.trustees.company._
import identifiers.register.trustees.{ExistingCurrentAddressId, IsTrusteeNewId}
import models.Mode.journeyMode
import models._
import utils.UserAnswers
import utils.{Toggles, UserAnswers}

class TrusteesCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                         appConfig: FrontendAppConfig) extends AbstractNavigator {

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    if(mode == CheckMode || mode == NormalMode){
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      if(answers.get(IsTrusteeNewId(index)).getOrElse(false)) checkYourAnswers(index, journeyMode(mode), srn)
      else anyMoreChanges(srn)
    }

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))

  //scalastyle:off cyclomatic.complexity
  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(CompanyVatController.onPageLoad(mode, index, srn))

      case CompanyVatId(index) =>
        NavigateTo.dontSave(CompanyPayeController.onPageLoad(mode, index, srn))

      case CompanyPayeId(index) =>
        NavigateTo.dontSave(CompanyRegistrationNumberController.onPageLoad(mode, srn, index))

      case CompanyRegistrationNumberId(index) =>
        NavigateTo.dontSave(CompanyUniqueTaxReferenceController.onPageLoad(mode, index, srn))

      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(CompanyPostCodeLookupController.onPageLoad(mode, index, srn))

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyAddressListController.onPageLoad(mode, index, srn))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(CompanyAddressController.onPageLoad(mode, index, srn))

      case CompanyAddressId(index) =>
        NavigateTo.dontSave(CompanyAddressYearsController.onPageLoad(mode, index, srn))

      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressListController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressId(index) =>
        NavigateTo.dontSave(CompanyContactDetailsController.onPageLoad(mode, index, srn))

      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(mode, index, srn))

      case CheckYourAnswersId =>
        NavigateTo.dontSave(AddTrusteeController.onPageLoad(mode, srn))

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case _ => None
    }
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyDetailsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyVatId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyVatVariationsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyPayeId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyPayeVariationsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyRegistrationNumberId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyRegistrationNumberVariationsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyUniqueTaxReferenceId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyAddressListController.onPageLoad(mode, index, srn))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(CompanyAddressController.onPageLoad(mode, index, srn))

      case CompanyAddressId(index) =>
        val isNew = from.userAnswers.get(IsTrusteeNewId(index)).contains(true)
        if(isNew || mode == CheckMode) {
          checkYourAnswers(index, journeyMode(mode), srn)
        } else {
          NavigateTo.dontSave(CompanyAddressYearsController.onPageLoad(mode, index, srn))
        }

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressListController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressController.onPageLoad(mode, index, srn))

      case CompanyPreviousAddressId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyContactDetailsId(index) =>
        exitMiniJourney(index, mode, srn, from.userAnswers)

      case _ => None
    }
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = routes(from, NormalMode, None)

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = routes(from, UpdateMode, srn)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(mode, index, srn))
  }

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(CompanyContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    (
      answers.get(CompanyAddressYearsId(index)),
      mode,
      answers.get(ExistingCurrentAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
      case (Some(AddressYears.UnderAYear), _, _) =>
        NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case (Some(AddressYears.OverAYear), _, _) =>
        exitMiniJourney(index, mode, srn, answers)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    if (mode == CheckUpdateMode) {
      answers.get(CompanyConfirmPreviousAddressId(index)) match {
        case Some(false) =>
          NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
        case Some(true) =>
          anyMoreChanges(srn)
        case None =>
          NavigateTo.dontSave(SessionExpiredController.onPageLoad())
      }
    } else {
      NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
}
