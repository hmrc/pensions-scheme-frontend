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
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyVatController.onPageLoad(mode, index, srn))
      case CompanyVatId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPayeController.onPageLoad(mode, index, srn))
      case CompanyPayeId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, srn, index))
      case CompanyRegistrationNumberId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, srn, index))
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, srn, index))
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, srn, index))
      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, srn, index))
      case CompanyAddressId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(mode, index, from.userAnswers, srn)
      case OtherDirectorsId(index) =>
        if(mode == CheckMode || mode == NormalMode){
          NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index))
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
      case CompanyDetailsId(index) =>             exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyVatId(index) =>                 exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyVatVariationsId(index) =>       exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeId(index) =>                exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyPayeVariationsId(index) =>      exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyRegistrationNumberId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyUniqueTaxReferenceId(index) =>  exitMiniJourney(index, mode, srn, from.userAnswers)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, srn, index))

      case CompanyAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, srn, index))

      case CompanyAddressId(index) => {
        val isNew = from.userAnswers.get(IsEstablisherNewId(index)).contains(true)
        if (isNew || mode == CheckMode) {
          checkYourAnswers(index, journeyMode(mode), srn)
        } else {
          NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
        }
      }

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index))

      case CompanyPreviousAddressId(index) =>     exitMiniJourney(index, mode, srn, from.userAnswers)
      case CompanyContactDetailsId(index) =>      exitMiniJourney(index, mode, srn, from.userAnswers)
      case IsCompanyDormantId(index) =>           exitMiniJourney(index, mode, srn, from.userAnswers)

      case OtherDirectorsId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(journeyMode(mode), srn, index))

      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.dontSave(controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(NormalMode, None, index))
    case IsCompanyDormantId(index) =>
      NavigateTo.dontSave(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))
    case _ => routes(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = {
    from.id match {
      case CompanyContactDetailsId(index) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn, index))
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))
      case _ => routes (from, UpdateMode, srn)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    if(mode == CheckMode || mode == NormalMode){
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      if(answers.get(IsEstablisherNewId(index)).getOrElse(false))
        checkYourAnswers(index, journeyMode(mode), srn)
      else anyMoreChanges(srn)
    }

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(mode, srn, index))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        if (mode == CheckUpdateMode && featureSwitchManagementService.get(Toggles.isPrevAddEnabled))
          NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyConfirmPreviousAddressController.onPageLoad(index, srn))
        else
          NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
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
              NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index))
            case _ => answers.get(IsEstablisherNewId(index)) match {
              case Some(true) =>
                NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index))
              case _ =>
                if(answers.get(EstablishersOrTrusteesChangedId).contains(true)){
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
      NavigateTo.dontSave(controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, srn, index))
    }
  }

  private def listOrAnyMoreChange(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    mode match {
      case CheckMode | NormalMode =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
      case _ => answers.get(IsEstablisherNewId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
        case _ =>
          anyMoreChanges(srn)
      }
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
