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

import com.google.inject.{Inject, Singleton}
import controllers.register.establishers.company.director.routes
import identifiers.Identifier
import identifiers.register.establishers.company.director._
import models.{AddressYears, CheckMode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class EstablishersCompanyDirectorNavigator @Inject() extends Navigator {

  private def checkYourAnswers(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Call =
    routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex)

  override protected val routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case  DirectorDetailsId(establisherIndex, directorIndex) =>
      _ => controllers.register.establishers.company.director.routes.DirectorNinoController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorNinoId(establisherIndex, directorIndex) =>
      _ => routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressListController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorAddressListId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorAddressId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressYearsController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorAddressYearsId(establisherIndex, directorIndex) =>
      addressYearsRoutes(establisherIndex, directorIndex)
    case  DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
      _ => routes.DirectorPreviousAddressListController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
      _ => routes.DirectorPreviousAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorPreviousAddressId(establisherIndex, directorIndex) =>
      _ => routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, directorIndex)
    case  DirectorContactDetailsId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
  }
    override protected val editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case  DirectorDetailsId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
    case  DirectorNinoId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
    case  DirectorUniqueTaxReferenceId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
    case  DirectorAddressPostcodeLookupId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressListController.onPageLoad(CheckMode, establisherIndex, directorIndex)
    case  DirectorAddressListId(establisherIndex, directorIndex) =>
      _ => routes.DirectorAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex)
    case  DirectorAddressId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
    case  DirectorAddressYearsId(establisherIndex, directorIndex) =>
      addressYearsEditRoutes(establisherIndex, directorIndex)
    case  DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex) =>
      _ => routes.DirectorPreviousAddressListController.onPageLoad(CheckMode, establisherIndex, directorIndex)
    case  DirectorPreviousAddressListId(establisherIndex, directorIndex) =>
      _ => routes.DirectorPreviousAddressController.onPageLoad(CheckMode, establisherIndex, directorIndex)
    case  DirectorPreviousAddressId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
    case  DirectorContactDetailsId(establisherIndex, directorIndex) =>
      checkYourAnswers(establisherIndex, directorIndex)
  }

  private def addressYearsRoutes(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Call = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, directorIndex)
      case Some(AddressYears.OverAYear) =>
        routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, directorIndex)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def addressYearsEditRoutes(establisherIndex: Int, directorIndex: Int)(answers: UserAnswers): Call = {
    answers.get(DirectorAddressYearsId(establisherIndex, directorIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, establisherIndex, directorIndex)
      case Some(AddressYears.OverAYear) =>
        routes.CheckYourAnswersController.onPageLoad(establisherIndex, directorIndex)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }


}
