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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import identifiers.register.establishers.company._
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.{AddressYears, CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

//scalastyle:off cyclomatic.complexity
class EstablishersCompanyNavigator @Inject()(val dataCacheConnector: DataCacheConnector, appConfig: FrontendAppConfig) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case CompanyDetailsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyRegistrationNumberController.onPageLoad(NormalMode, index))
      case CompanyRegistrationNumberId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(NormalMode, index))
      case CompanyUniqueTaxReferenceId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPostCodeLookupController.onPageLoad(NormalMode, index))
      case CompanyPostCodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(NormalMode, index))
      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(NormalMode, index))
      case CompanyAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressYearsController.onPageLoad(NormalMode, index))
      case CompanyAddressYearsId(index) =>
        addressYearsRoutes(index, from.userAnswers)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(NormalMode, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode, index))
      case CompanyPreviousAddressId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index))
      case CompanyContactDetailsId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index))
      case AddCompanyDirectorsId(index) =>
        addDirectors(NormalMode, index, from.userAnswers)
      case OtherDirectorsId(index)=>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index))
      case CompanyReviewId(_) =>
        NavigateTo.save(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode))
      case CheckYourAnswersId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, index))
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
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressListController.onPageLoad(CheckMode, index))
      case CompanyAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyAddressController.onPageLoad(CheckMode, index))
      case CompanyAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyAddressYearsId(index) =>
        editAddressYearsRoutes(index, from.userAnswers)
      case CompanyPreviousAddressPostcodeLookupId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressListController.onPageLoad(CheckMode, index))
      case CompanyPreviousAddressListId(index) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode, index))
      case CompanyPreviousAddressId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case CompanyContactDetailsId(index) =>
        checkYourAnswers(index, from.userAnswers)
      case AddCompanyDirectorsId(index) =>
        addDirectors(CheckMode, index, from.userAnswers)
      case OtherDirectorsId(index)=>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index))
      case _ => None
    }

  private def checkYourAnswers(index: Int, answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index))

  private def addressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addDirectors(mode: Mode, index: Int, answers: UserAnswers): Option[NavigateTo] = {
    val directors = answers
      .getAllRecursive[DirectorDetails](DirectorDetailsId.collectionPath(index))
      .getOrElse(Nil)

    if (directors.isEmpty) {
      NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, index, 0))
    }
    else if (directors.lengthCompare(appConfig.maxDirectors) < 0) {
      answers.get(AddCompanyDirectorsId(index)) match {
        case Some(true) =>
          NavigateTo.save(controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, index, directors.length))
        case Some(false) =>
          NavigateTo.save(controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(index))
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.save(controllers.register.establishers.company.routes.OtherDirectorsController.onPageLoad(mode, index))
    }
  }

}
