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
import identifiers.register.establishers.company._
import models.{AddressYears, CheckMode, CheckUpdateMode, Index, Mode, NormalMode, UpdateMode}
import models.Mode._
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  protected def routes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyVatController.onPageLoad(mode, index, srn))
      case CompanyVatId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPayeController.onPageLoad(mode, index, srn))
      case CompanyPayeId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, srn, index))
      case CompanyRegistrationNumberId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, srn, index))
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, srn, index))
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, srn, index))
      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, srn, index))
      case CompanyAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(mode, srn, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers, mode, srn)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index))
      case CompanyPreviousAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(mode, index, from.userAnswers, srn)
      case OtherDirectorsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index))
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
      case CheckYourAnswersId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, index))
      case _ => None
    }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>             exitMiniJourney(index, mode, srn)
      case CompanyVatId(index) =>                 exitMiniJourney(index, mode, srn)
      case CompanyPayeId(index) =>                exitMiniJourney(index, mode, srn)
      case CompanyRegistrationNumberId(index) =>  exitMiniJourney(index, mode, srn)
      case CompanyUniqueTaxReferenceId(index) =>  exitMiniJourney(index, mode, srn)

      case CompanyPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(mode, None, index))

      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(mode, None, index))

      case CompanyAddressId(index) =>             exitMiniJourney(index, mode, srn)

      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers, mode, srn)

      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, None, index))

      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(mode, None, index))

      case CompanyPreviousAddressId(index) =>     exitMiniJourney(index, mode, srn)
      case CompanyContactDetailsId(index) =>      exitMiniJourney(index, mode, srn)
      case IsCompanyDormantId(index) =>           exitMiniJourney(index, mode, srn)

      case OtherDirectorsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(journeyMode(mode), None, index))

      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.save(controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(NormalMode, None, index))
    case IsCompanyDormantId(index) =>
      NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))
    case _ => routes(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = from.id match {
    case CompanyContactDetailsId(index) =>
      NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn, index))
    case _ => routes (from, UpdateMode, srn)
  }
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)
  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    if(mode == CheckMode || mode == NormalMode){
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      anyMoreChanges(srn)
    }

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(mode, srn, index))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.save(controllers.vary.routes.AnyMoreChangesController.onPageLoad(srn))

  private def addressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(mode, srn, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))
      case Some(AddressYears.OverAYear) => exitMiniJourney(index, mode, srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    val directors = answers.allDirectorsAfterDelete(index)

    if (directors.isEmpty) {
      NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(
        mode, index, answers.allDirectors(index).size, srn))
    }
    else if (directors.lengthCompare(appConfig.maxDirectors) < 0) {
      answers.get(AddCompanyDirectorsId(index)) match {
        case Some(true) =>
          NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController
            .onPageLoad(mode, index, answers.allDirectors(index).size, srn))
        case Some(false) =>
          NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(mode, srn, index))
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.save(controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, srn, index))
    }
  }
}
