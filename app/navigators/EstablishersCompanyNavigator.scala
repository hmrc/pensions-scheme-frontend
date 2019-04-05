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
import models.{AddressYears, CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(NormalMode, None, index))
      case CompanyRegistrationNumberId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, None, index))
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, None, index))
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(NormalMode, None, index))
      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(NormalMode, None, index))
      case CompanyAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, None, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, None, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode, None, index))
      case CompanyPreviousAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, None, index))
      case CompanyContactDetailsId(index) =>
          NavigateTo.save(controllers.register.establishers.company.routes.IsCompanyDormantController.onPageLoad(NormalMode, None, index))
      case IsCompanyDormantId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(NormalMode, index, from.userAnswers)
      case OtherDirectorsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(NormalMode, None, index))
      case CompanyReviewId(_) =>
        NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None))
      case CheckYourAnswersId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, index))
      case _ => None
    }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyRegistrationNumberId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyUniqueTaxReferenceId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(CheckMode, None, index))
      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, None, index))
      case CompanyAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, None, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, None, index))
      case CompanyPreviousAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyContactDetailsId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case IsCompanyDormantId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case AddCompanyDirectorsId(index) =>
        addDirectors(CheckMode, index, from.userAnswers)
      case OtherDirectorsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(NormalMode, None, index))
      case _ => None
    }

  private def checkYourAnswers(index: Int, answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))

  private def addressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, None, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, None, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, None, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers): Option[NavigateTo] = {
    val directors = answers.allDirectorsAfterDelete(index)

    if (directors.isEmpty) {
      NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(
        mode, index, answers.allDirectors(index).size, None))
    }
    else if (directors.lengthCompare(appConfig.maxDirectors) < 0) {
      answers.get(AddCompanyDirectorsId(index)) match {
        case Some(true) =>
          NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, index, answers.allDirectors(index).size, None))
        case Some(false) =>
          NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(NormalMode, None, index))
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.save(controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, None, index))
    }
  }
}
