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

import com.google.inject.{Inject, Singleton}
import connectors.UserAnswersCacheConnector
import controllers.register.establishers.company.director.routes
import identifiers.register.establishers.company.director._
import models.{AddressYears, CheckMode, NormalMode}
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersCompanyDirectorNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex, NormalMode, None))

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        NavigateTo.save(controllers.register.establishers.company.director.routes.
          DirectorNinoController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorNinoId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressListController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorAddressId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressYearsController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsRoutes(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case ConfirmDeleteDirectorId(establisherIndex) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex))
      case CheckYourAnswersId(establisherIndex, directorIndex) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex))
      case _ => None
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorNinoId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressListController.onPageLoad(CheckMode, establisherIndex, directorIndex, None))
      case DirectorAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex, None))
      case DirectorAddressId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsEditRoutes(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, establisherIndex, directorIndex, None))
      case DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex, None))
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex)(from.userAnswers)
      case _ => None
    }
  }

  private def addressYearsRoutes(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsEditRoutes(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, establisherIndex, directorIndex, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex, NormalMode, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
