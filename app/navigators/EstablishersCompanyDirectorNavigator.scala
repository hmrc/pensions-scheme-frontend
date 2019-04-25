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
import models.Mode.journeyMode
import models._
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersCompanyDirectorNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex, NormalMode, None))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def exitMiniJourney(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    mode match {
      case CheckMode | NormalMode =>
        checkYourAnswers(establisherIndex, directorIndex, journeyMode(mode), srn)
      case _ =>
        anyMoreChanges(srn)
    }

  protected def localRroutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        NavigateTo.save(controllers.register.establishers.company.director.routes.
          DirectorNinoController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorNinoId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressYearsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsRoutes(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorContactDetailsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case ConfirmDeleteDirectorId(establisherIndex) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, establisherIndex))
      case CheckYourAnswersId(establisherIndex, directorIndex) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, establisherIndex))
    }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorNinoId(establisherIndex, directorIndex)  =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorAddressId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsEditRoutes(establisherIndex, directorIndex)(from.userAnswers)
      case DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.save(routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case _ => None
    }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, NormalMode, None)
      case ConfirmDeleteDirectorId(establisherIndex) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex))
      case CheckYourAnswersId(establisherIndex, directorIndex) =>
        NavigateTo.save(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex))
      case _ => localRroutes(from, NormalMode, None)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = localRroutes(from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

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
