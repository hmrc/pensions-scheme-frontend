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
import controllers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.AddPartnersId
import identifiers.register.establishers.partnership.partner._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import utils.{Navigator, UserAnswers}

class EstablishersPartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {
  //scalastyle:off cyclomatic.complexity
  private def checkYourAnswers(establisherIndex: Int, partnerIndex: Int)(answers: UserAnswers): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddPartnersId(establisherIndex) =>
      addPartnerRoutes(NormalMode, establisherIndex, from.userAnswers)
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerNinoController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerUniqueTaxReferenceController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressListController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressYearsController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsRoutes(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressListController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
    case PartnerContactDetailsId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case ConfirmDeletePartnerId(establisherIndex) =>
      NavigateTo.dontSave(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(NormalMode, establisherIndex, None))
    case CheckYourAnswersId(establisherIndex, partnerIndex) =>
      NavigateTo.save(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(NormalMode, establisherIndex, None))
    case _ =>
      None
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddPartnersId(establisherIndex) =>
      addPartnerRoutes(CheckMode, establisherIndex, from.userAnswers)
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressListController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsEditRoutes(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressListController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case PartnerContactDetailsId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex)(from.userAnswers)
    case _ => None
  }

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = None

  private def addressYearsRoutes(establisherIndex: Int, partnerIndex: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsEditRoutes(establisherIndex: Int, partnerIndex: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addPartnerRoutes(mode: Mode, index: Int, answers: UserAnswers): Option[NavigateTo] = {
    val partners = answers.allPartnersAfterDelete(index)

    if (partners.isEmpty) {
      NavigateTo.save(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(
        mode, index, answers.allPartners(index).size, None))
    }
    else if (partners.lengthCompare(appConfig.maxPartners) < 0) {
      answers.get(AddPartnersId(index)) match {
        case Some(true) =>
          NavigateTo.save(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(mode,
            index, answers.allPartners(index).size, None))
        case Some(false) =>
          NavigateTo.save(controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, index, None))
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.save(controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, index, None))
    }
  }
}
