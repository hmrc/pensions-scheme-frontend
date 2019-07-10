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
import identifiers.AnyMoreChangesId
import identifiers.register.establishers.company.director._
import models.Mode.journeyMode
import models._
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersCompanyDirectorNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex, mode, srn))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def exitMiniJourney(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    mode match {
      case CheckMode | NormalMode =>
        checkYourAnswers(establisherIndex, directorIndex, journeyMode(mode), srn)
      case _ =>
        if (answers.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false))
          checkYourAnswers(establisherIndex, directorIndex, journeyMode(mode), srn)
        else anyMoreChanges(srn)
    }

  protected def normalRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(controllers.register.establishers.company.director.routes.
          DirectorNinoController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorHasNINOId(establisherIndex, directorIndex) =>
        hasNinoRoutes(establisherIndex, directorIndex, mode, srn)(from.userAnswers)
      case DirectorNameId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(controllers.register.establishers.company.director.routes.
          DirectorDOBController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorNinoId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorAddressYearsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorContactDetailsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        checkYourAnswers(establisherIndex, directorIndex, mode, srn)
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsRoutes(establisherIndex, directorIndex, mode, srn)(from.userAnswers)
      case _ => commonRoutes(from, mode, srn)
    }

  //scalastyle:off cyclomatic.complexity
  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case DirectorDetailsId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorHasNINOId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorNameId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorNinoId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorNewNinoId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorAddressId(establisherIndex, directorIndex) =>
        val isNew = from.userAnswers.get(IsNewDirectorId(establisherIndex, directorIndex)).contains(true)
        if (isNew || mode == CheckMode) {
          checkYourAnswers(establisherIndex, directorIndex, journeyMode(mode), srn)
        } else {
          NavigateTo.dontSave(routes.DirectorAddressYearsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
        }
      case DirectorConfirmPreviousAddressId(establisherIndex, directorIndex) =>
        confirmPreviousAddressRoutes(establisherIndex, directorIndex, mode, srn)(from.userAnswers)
      case DirectorPreviousAddressId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorContactDetailsId(establisherIndex, directorIndex) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, from.userAnswers)
      case DirectorAddressYearsId(establisherIndex, directorIndex) =>
        addressYearsEditRoutes(establisherIndex, directorIndex, mode, srn)(from.userAnswers)
      case _ => commonRoutes(from, mode, srn)
    }

  protected def commonRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    from.id match {
      case DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorPreviousAddressListController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
        NavigateTo.dontSave(routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case ConfirmDeleteDirectorId(establisherIndex) =>
        mode match {
          case CheckMode | NormalMode =>
            NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, establisherIndex))
          case _ =>
            anyMoreChanges(srn)
        }
      case CheckYourAnswersId(establisherIndex, _) =>
        NavigateTo.dontSave(controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, establisherIndex))
      case AnyMoreChangesId => anyMoreChanges(srn)
      case _ => None
    }
  //scalastyle:on cyclomatic.complexity

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = normalRoutes(from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = normalRoutes(from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(routes.DirectorContactDetailsController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def hasNinoRoutes(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    navigateOrSessionExpired(answers, DirectorHasNINOId(establisherIndex, directorIndex),
      if (_: Boolean)
        routes.DirectorNinoNewController.onPageLoad(mode, establisherIndex, directorIndex, srn)
      else
        routes.DirectorNoNINOReasonController.onPageLoad(mode, establisherIndex, directorIndex, srn)
    )

  private def addressYearsEditRoutes(establisherIndex: Int, directorIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        if (mode == CheckUpdateMode)
          NavigateTo.dontSave(routes.DirectorConfirmPreviousAddressController.onPageLoad(establisherIndex, directorIndex, srn))
        else
          NavigateTo.dontSave(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case Some(AddressYears.OverAYear) =>
        exitMiniJourney(establisherIndex, directorIndex, mode, srn, answers)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmPreviousAddressRoutes(establisherIndex: Int, directorIndex: Int,
                                           mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    answers.get(DirectorConfirmPreviousAddressId(establisherIndex, directorIndex)) match {
      case Some(false) =>
        NavigateTo.dontSave(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
}
