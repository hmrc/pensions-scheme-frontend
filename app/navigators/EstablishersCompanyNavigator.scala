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
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company._
import identifiers.register.establishers.{ExistingCurrentAddressId, IsEstablisherNewId}
import models.Mode._
import models._
import utils.{Toggles, UserAnswers}
import controllers.register.establishers.company.director.routes._

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                             appConfig: FrontendAppConfig) extends AbstractNavigator {

  private def exitMiniJourney(index: Int,
                              mode: Mode,
                              srn: Option[String],
                              answers: UserAnswers,
                              cyaPage: (Int, Mode, Option[String]) => Option[NavigateTo] = cya): Option[NavigateTo] = {
    if (mode == CheckMode || mode == NormalMode)
      cyaPage(index, journeyMode(mode), srn)
    else if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
      cyaPage(index, journeyMode(mode), srn)
    else
      anyMoreChanges(srn)
  }

  private def cya(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersController.onPageLoad(mode, srn, index))

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
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)
        )
      case HasCompanyNumberId(index) =>
        confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case CompanyRegistrationNumberVariationsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
      case HasCompanyVATId(index) =>
        confirmHasCompanyVat(index, mode, srn)(from.userAnswers)
      case CompanyVatId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPayeController.onPageLoad(mode, index, srn))
      case CompanyEnterVATId(index) =>
        navigateOrSessionExpired(from.userAnswers, CompanyEnterVATId(index), (_: ReferenceValue) =>
          establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case HasCompanyPAYEId(index) => confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyPayeId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyRegistrationNumberController.onPageLoad(mode, srn, index))
      case CompanyRegistrationNumberId(index) =>
          NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyUTRController.onPageLoad(mode, srn, index))
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
      case CheckYourAnswersId(index) =>
        listOrAnyMoreChange(index, mode, srn)(from.userAnswers)
      case _ => None
    }

  def previousAddressRoutes(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
      NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
  }

  def previousAddressEditRoutes(index: Int, mode: Mode, srn: Option[String], userAnswers: UserAnswers): Option[NavigateTo] = {
      exitMiniJourney(index, mode, srn, userAnswers, cyaAddressDetails)
  }


  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>             exitMiniJourney(index, mode, srn, from.userAnswers)
      case HasCompanyNumberId(index) =>           confirmHasCompanyNumber(index, mode, srn)(from.userAnswers)
      case HasCompanyVATId(index) =>              confirmHasCompanyVat(index, mode, srn)(from.userAnswers)
      case HasCompanyPAYEId(index) =>             confirmHasCompanyPAYE(index, mode, srn)(from.userAnswers)
      case CompanyVatId(index) =>                 exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyEnterVATId(index) =>       exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyPayeId(index) =>                exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeVariationsId(index) =>      exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyRegistrationNumberId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case NoCompanyNumberId(index) =>            exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasCompanyUTRId(index)  =>             confirmHasCompanyUtr(index, mode, srn)(from.userAnswers)
      case CompanyRegistrationNumberVariationsId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case CompanyUniqueTaxReferenceId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyUTRId(index) =>                 exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case NoCompanyUTRId(index) =>               exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressListController.onPageLoad(mode, srn, index))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressController.onPageLoad(mode, srn, index))

      case CompanyAddressId(index) =>
        if (from.userAnswers.get(IsEstablisherNewId(index)).contains(true) || mode == CheckMode) {
            cyaAddressDetails(index, journeyMode(mode), srn)
        }
        else if (!from.userAnswers.get(IsEstablisherNewId(index)).contains(true) && mode == CheckUpdateMode) {
          NavigateTo.dontSave(CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
        } else {
          NavigateTo.dontSave(establisherCompanyRoutes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
        }
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
      case IsCompanyDormantId(index) => exitMiniJourney(index, mode, srn, from.userAnswers, cyaCompanyDetails)
      case HasBeenTradingCompanyId(index) => confirmHasBeenTrading(index, mode, srn)(from.userAnswers)
      case OtherDirectorsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyReviewController.onPageLoad(journeyMode(mode), srn, index))

      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(NormalMode, None, index))
    case IsCompanyDormantId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, None, index))
    case _ => routes(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersController.onPageLoad(UpdateMode, srn, index))
      case _ => routes(from, UpdateMode, srn)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(establisherCompanyRoutes.HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    (
      answers.get(CompanyAddressYearsId(index)),
      answers.get(IsEstablisherNewId(index)).getOrElse(false)
    ) match {
      case (Some(AddressYears.UnderAYear), _) =>
        NavigateTo.dontSave(HasBeenTradingCompanyController.onPageLoad(mode, srn, index))
      case (Some(AddressYears.OverAYear), _) =>
        exitMiniJourney(index, mode, srn, answers, cyaAddressDetails)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }


  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    NavigateTo.dontSave(
        if (answers.allDirectorsAfterDelete(index).isEmpty) {
          controllers.register.establishers.company.director.routes.DirectorNameController
            .onPageLoad(mode, index, answers.allDirectors(index).size, srn)
        } else if (answers.allDirectorsAfterDelete(index).length < appConfig.maxDirectors) {
          answers.get(AddCompanyDirectorsId(index)).map { addCompanyDirectors =>
            if (addCompanyDirectors) {
              controllers.register.establishers.company.director.routes.DirectorNameController
                .onPageLoad(mode, index, answers.allDirectors(index).size, srn)
            } else {
              controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)
            }
          }.getOrElse(controllers.routes.SessionExpiredController.onPageLoad())
        } else {
          establisherCompanyRoutes.OtherDirectorsController.onPageLoad(mode, srn, index)
        }
    )
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
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyEnterVATController.onPageLoad(mode, index, srn))
      case Some(false) if Seq(NormalMode, UpdateMode).contains(mode)=>
        NavigateTo.dontSave(establisherCompanyRoutes.HasCompanyPAYEController.onPageLoad(mode, srn, index))
      case Some(false) =>
        cyaCompanyDetails(index, journeyMode(mode), srn)
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
        cyaCompanyDetails(index, mode, srn)
      case (Some(false), CheckMode | CheckUpdateMode) =>
        exitMiniJourney(index, mode, srn, answers, cyaCompanyDetails)
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmHasBeenTrading(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(HasBeenTradingCompanyId(index)) match {
      case Some(true) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(false) =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(journeyMode(mode), srn, index))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def payeRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (mode, answers.get(IsEstablisherNewId(index))) match {
      case (NormalMode, _) =>
        NavigateTo.dontSave(establisherCompanyRoutes.IsCompanyDormantController.onPageLoad(mode, srn, index))
      case _ =>
        NavigateTo.dontSave(establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index))
    }
  }
}
