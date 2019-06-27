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
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.company.routes._
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import identifiers.EstablishersOrTrusteesChangedId
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import models.Mode._
import models._
import utils.{Navigator, Toggles, UserAnswers}

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                             appConfig: FrontendAppConfig,
                                             featureSwitchManagementService: FeatureSwitchManagementService) extends Navigator {
  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(CompanyVatController.onPageLoad(mode, index, srn))
      case HasCompanyNumberId(index) => confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case CompanyVatId(index) =>
        NavigateTo.dontSave(CompanyPayeController.onPageLoad(mode, index, srn))
      case CompanyVatVariationsId(index) =>
        navigateOrSessionExpired(from.userAnswers, CompanyVatVariationsId(index), (_:String) =>
          HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case HasCompanyPAYEId(index) => confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyPayeId(index) =>
        NavigateTo.dontSave(CompanyRegistrationNumberController.onPageLoad(mode, srn, index))
      case CompanyRegistrationNumberId(index) =>
        NavigateTo.dontSave(CompanyUniqueTaxReferenceController.onPageLoad(mode, srn, index))
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(CompanyPostCodeLookupController.onPageLoad(mode, srn, index))
      case NoCompanyUTRId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyVATController.onPageLoad(index))
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(CompanyAddressListController.onPageLoad(mode, srn, index))
      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(CompanyAddressController.onPageLoad(mode, srn, index))
      case CompanyAddressId(index) =>
        NavigateTo.dontSave(CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressListController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressId(index) =>
        NavigateTo.dontSave(CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(mode, index, from.userAnswers, srn)
      case OtherDirectorsId(index) =>
        if (mode == CheckMode || mode == NormalMode) {
          NavigateTo.dontSave(CompanyReviewController.onPageLoad(mode, srn, index))
        } else {
          NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
        }
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
      case CheckYourAnswersId(index) =>
        listOrAnyMoreChange(index, mode, srn)(from.userAnswers)
      case _ => None
    }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasCompanyNumberId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasCompanyPAYEId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyVatId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyVatVariationsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeVariationsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyRegistrationNumberId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyUniqueTaxReferenceId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case NoCompanyUTRId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(CompanyAddressListController.onPageLoad(mode, srn, index))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(CompanyAddressController.onPageLoad(mode, srn, index))

      case CompanyAddressId(index) => {
        val isNew = from.userAnswers.get(IsEstablisherNewId(index)).contains(true)
        if (isNew || mode == CheckMode) {
          checkYourAnswers(index, journeyMode(mode), srn)
        } else {
          NavigateTo.dontSave(CompanyAddressYearsController.onPageLoad(mode, srn, index))
        }
      }

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressListController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(CompanyPreviousAddressController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyContactDetailsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case IsCompanyDormantId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)

      case OtherDirectorsId(index) =>
        NavigateTo.dontSave(CompanyReviewController.onPageLoad(journeyMode(mode), srn, index))

      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.dontSave(IsCompanyDormantController.onPageLoad(NormalMode, None, index))
    case IsCompanyDormantId(index) =>
      NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(NormalMode, None, index))
    case _ => routes(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(UpdateMode, srn, index))
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
      case _ => routes(from, UpdateMode, srn)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode) {
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
        checkYourAnswers(index, journeyMode(mode), srn)
      else anyMoreChanges(srn)
    }

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(mode, srn, index))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        if (mode == CheckUpdateMode && featureSwitchManagementService.get(Toggles.isPrevAddEnabled))
          NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
        else
          NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) => exitMiniJourney(index, mode, srn, answers)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    val directors = answers.allDirectorsAfterDelete(index)

    if (directors.isEmpty) {
      NavigateTo.dontSave(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(
        mode, index, answers.allDirectors(index).size, srn))
    }
    else if (directors.lengthCompare(appConfig.maxDirectors) < 0) {
      answers.get(AddCompanyDirectorsId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(controllers.register.establishers.company.director.routes.DirectorDetailsController
            .onPageLoad(mode, index, answers.allDirectors(index).size, srn))
        case Some(false) =>
          mode match {
            case CheckMode | NormalMode =>
              NavigateTo.dontSave(CompanyReviewController.onPageLoad(mode, srn, index))
            case _ => answers.get(IsEstablisherNewId(index)) match {
              case Some(true) =>
                NavigateTo.dontSave(CompanyReviewController.onPageLoad(mode, srn, index))
              case _ =>
                if (answers.get(EstablishersOrTrusteesChangedId).contains(true)) {
                  anyMoreChanges(srn)
                } else {
                  NavigateTo.dontSave(controllers.routes.SchemeTaskListController.onPageLoad(mode, srn))
                }
            }
          }
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.dontSave(OtherDirectorsController.onPageLoad(mode, srn, index))
    }
  }

  private def listOrAnyMoreChange(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    mode match {
      case CheckMode | NormalMode =>
        NavigateTo.dontSave(AddCompanyDirectorsController.onPageLoad(mode, srn, index))
      case _ => answers.get(IsEstablisherNewId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(AddCompanyDirectorsController.onPageLoad(mode, srn, index))
        case _ =>
          anyMoreChanges(srn)
      }
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyNumber(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyNumberId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(NoCompanyNumberController.onPageLoad(index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyPAYE(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    navigateOrSessionExpired(answers, HasCompanyPAYEId(index),
      if(_:Boolean)
        CompanyPayeVariationsController.onPageLoad(mode, index, srn)
      else
        CheckYourAnswersController.onPageLoad(mode, srn, index)
    )
}
