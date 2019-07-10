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
import controllers.register.establishers.company.director.routes._
import controllers.register.establishers.company.routes._
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.routes._
import identifiers.EstablishersOrTrusteesChangedId
import identifiers.register.establishers.company._
import identifiers.register.establishers.{ExistingCurrentAddressId, IsEstablisherNewId}
import models.Mode._
import models._
import utils.{Navigator, Toggles, UserAnswers}

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                             appConfig: FrontendAppConfig,
                                             featureSwitchManagementService: FeatureSwitchManagementService) extends Navigator {

  private def exitMiniJourney(index: Int,
                              mode: Mode,
                              srn: Option[String],
                              answers: UserAnswers,
                              cyaPage: (Int, Mode, Option[String]) => Option[NavigateTo] = cya): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode)
      cyaPage(index, journeyMode(mode), srn)
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage(index, journeyMode(mode), srn)
    else
      anyMoreChanges(srn)


  private def cyaCompanyDetails(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))

  private def cyaContactDetails(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index))

  private def cyaAddressDetails(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.dontSave(
          if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)
          } else {
            establisherCompanyRoutes.CompanyVatController.onPageLoad(mode, index, srn)
          }
        )
      case HasCompanyNumberId(index) =>
        confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case CompanyRegistrationNumberVariationsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
      case HasCompanyVATId(index) =>
        confirmHasCompanyVat(index, mode, srn)(from.userAnswers)
      case CompanyVatId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPayeController.onPageLoad(mode, index, srn))
      case CompanyVatVariationsId(index) =>
        navigateOrSessionExpired(from.userAnswers, CompanyVatVariationsId(index), (_: ReferenceValue) =>
          establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case HasCompanyPAYEId(index) => confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyPayeId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyRegistrationNumberController.onPageLoad(mode, srn, index))
      case CompanyRegistrationNumberId(index) =>
        if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
          NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
        } else {
          NavigateTo.dontSave(establisherCompanyRoutes.CompanyUniqueTaxReferenceController.onPageLoad(mode, srn, index))
        }
      case NoCompanyNumberId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
      case HasCompanyUTRId(index) =>
        confirmHasCompanyUtr(index, mode, srn)(from.userAnswers)
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPostCodeLookupController.onPageLoad(mode, srn, index))
      case CompanyUTRId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyVATController.onPageLoad(mode, srn, index))
      case NoCompanyUTRId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyVATController.onPageLoad(mode, srn, index))
      case CompanyPayeVariationsId(index) =>
        payeRoutes(index, mode, srn)(from.userAnswers)
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressListController.onPageLoad(mode, srn, index))
      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressController.onPageLoad(mode, srn, index))
      case CompanyAddressId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressId(index) =>
        previousAddressRoutes(index, mode, srn)
      case CompanyPhoneId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index))
      case HasBeenTradingCompanyId(index) =>
        confirmHasBeenTrading(index, mode, srn)(from.userAnswers)
      case CompanyEmailId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPhoneController.onPageLoad(mode, srn, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(mode, index, from.userAnswers, srn)
      case OtherDirectorsId(index) =>
        if (mode == CheckMode || mode == NormalMode) {
          NavigateTo.dontSave(establisherCompanyRoutes.CompanyReviewController.onPageLoad(mode, srn, index))
        } else {
          NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
        }
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
      case CheckYourAnswersId(index) =>
        listOrAnyMoreChange(index, mode, srn)(from.userAnswers)
      case _ => None
    }

  def previousAddressRoutes(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
      NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
    } else {
      NavigateTo.dontSave(establisherCompanyRoutes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
    }
  }

  def previousAddressEditRoutes(index: Int, mode: Mode, srn: Option[String], userAnswers: UserAnswers): Option[NavigateTo] = {
    if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
      exitMiniJourney(index, mode, srn, userAnswers, cyaAddressDetails)
    } else {
      exitMiniJourney(index, mode, srn, userAnswers)
    }
  }


  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>             exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasCompanyNumberId(index) =>           exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasCompanyVATId(index) =>              exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasCompanyPAYEId(index) =>             exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyVatId(index) =>                 exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyVatVariationsId(index) =>       exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeId(index) =>                exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeVariationsId(index) =>      exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyRegistrationNumberId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case NoCompanyNumberId(index) =>            exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasCompanyUTRId(index)  =>             exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyRegistrationNumberVariationsId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyUniqueTaxReferenceId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyUTRId(index) =>                 exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case NoCompanyUTRId(index) =>               exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressListController.onPageLoad(mode, srn, index))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressController.onPageLoad(mode, srn, index))

      case CompanyAddressId(index) =>
        if (from.userAnswers.get(IsEstablisherNewId(index)).contains(true) || mode == CheckMode)
          cya(index, journeyMode(mode), srn)
        else
          NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressId(index) => previousAddressEditRoutes(index, mode, srn, from.userAnswers)
      case CompanyContactDetailsId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPhoneId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaContactDetails)
      case CompanyEmailId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaContactDetails)
      case IsCompanyDormantId(index) => exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasBeenTradingCompanyId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaAddressDetails)

      case OtherDirectorsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyReviewController.onPageLoad(journeyMode(mode), srn, index))

      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(NormalMode, None, index))
    case IsCompanyDormantId(index) =>
      if(featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled))
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, None, index))
      else
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))
    case _ => routes(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersController.onPageLoad(UpdateMode, srn, index))
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
      case _ => routes(from, UpdateMode, srn)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def cya(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersController.onPageLoad(mode, srn, index))

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        if (featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
          NavigateTo.dontSave(establisherCompanyRoutes.HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
        } else {
          NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
        }
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    (
      answers.get(CompanyAddressYearsId(index)),
      featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled),
      answers.get(ExistingCurrentAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), false, Some(_)) =>
        NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
      case (Some(AddressYears.UnderAYear), true, _) =>
        NavigateTo.dontSave(HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
      case (Some(AddressYears.UnderAYear), false, _) =>
        NavigateTo.dontSave(CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case (Some(AddressYears.OverAYear), true, _) =>
        exitMiniJourney(index, mode, srn, answers, cyaAddressDetails)
      case (Some(AddressYears.OverAYear), false, _) =>
        exitMiniJourney(index, mode, srn, answers)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    val directors = answers.allDirectorsAfterDelete(index)

    if (directors.isEmpty) {
      NavigateTo.dontSave(DirectorDetailsController.onPageLoad(mode, index, answers.allDirectors(index).size, srn))
    }

    else if (directors.lengthCompare(appConfig.maxDirectors) < 0) {
      answers.get(AddCompanyDirectorsId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(DirectorDetailsController.onPageLoad(mode, index, answers.allDirectors(index).size, srn))
        case Some(false) =>
          mode match {
            case CheckMode | NormalMode =>
              NavigateTo.dontSave(establisherCompanyRoutes.CompanyReviewController.onPageLoad(mode, srn, index))
            case _ =>
              answers.get(IsEstablisherNewId(index)) match {
                case Some(true) =>
                  NavigateTo.dontSave(establisherCompanyRoutes.CompanyReviewController.onPageLoad(mode, srn, index))
                case _ =>
                  if (answers.get(EstablishersOrTrusteesChangedId).contains(true)) {
                    anyMoreChanges(srn)
                  } else {
                    NavigateTo.dontSave(SchemeTaskListController.onPageLoad(mode, srn))
                  }
              }
          }
        case _ =>
          NavigateTo.dontSave(SessionExpiredController.onPageLoad())
      }
    }

    else {
      NavigateTo.dontSave(establisherCompanyRoutes.OtherDirectorsController.onPageLoad(mode, srn, index))
    }
  }

  private def listOrAnyMoreChange(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    mode match {
      case CheckMode | NormalMode =>
        NavigateTo.dontSave(establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
      case _ => answers.get(IsEstablisherNewId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
        case _ =>
          anyMoreChanges(srn)
      }
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyNumber(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyNumberId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.NoCompanyNumberController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyUtr(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyUTRId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyUTRController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.NoCompanyUTRController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyVat(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasCompanyVATId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyVatVariationsController.onPageLoad(mode, index, srn))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasCompanyPAYE(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(HasCompanyPAYEId(index)), mode) match {
      case (Some(true), _) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPayeVariationsController.onPageLoad(mode, index, srn))
      case (Some(false), NormalMode) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case (Some(false), UpdateMode) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasBeenTrading(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasBeenTradingCompanyId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def payeRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (mode, answers.get(IsEstablisherNewId(index))) match {
      case (_, Some(true)) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case (NormalMode, _) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))
    }
  }
}
