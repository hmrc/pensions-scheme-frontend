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
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.AddPartnersId
import identifiers.register.establishers.partnership.partner._
import models.Mode.journeyMode
import models._
import utils.{Navigator, UserAnswers}

class EstablishersPartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, appConfig: FrontendAppConfig) extends Navigator {
  //scalastyle:off cyclomatic.complexity
  private def checkYourAnswers(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(mode, establisherIndex, partnerIndex, srn))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def exitMiniJourney(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    mode match {
      case CheckMode | NormalMode =>
        checkYourAnswers(establisherIndex, partnerIndex, journeyMode(mode), srn)
      case _ =>
        anyMoreChanges(srn)
    }

  protected def normalRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case AddPartnersId(establisherIndex) =>
      addPartnerRoutes(mode, establisherIndex, from.userAnswers, srn)
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerNinoController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressYearsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsRoutes(establisherIndex, partnerIndex, mode, srn)(from.userAnswers)
    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerContactDetailsId(establisherIndex, partnerIndex) =>
      checkYourAnswers(establisherIndex, partnerIndex, mode, srn)
    case ConfirmDeletePartnerId(establisherIndex) =>
      mode match {
        case CheckMode | NormalMode =>
          NavigateTo.dontSave(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, establisherIndex, srn))
        case _ =>
          anyMoreChanges(srn)
      }
    case CheckYourAnswersId(establisherIndex, partnerIndex) =>
      listOrAnyMoreChange(establisherIndex, mode, srn)(from.userAnswers)
    case _ =>
      None
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case AddPartnersId(establisherIndex) =>
      addPartnerRoutes(mode, establisherIndex, from.userAnswers, srn)
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressListController.onPageLoad(CheckMode, establisherIndex, partnerIndex, srn))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex, srn))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsEditRoutes(establisherIndex, partnerIndex, mode, srn)(from.userAnswers)
    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressListController.onPageLoad(CheckMode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(CheckMode, establisherIndex, partnerIndex, None))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case PartnerContactDetailsId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
    case _ => None
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = normalRoutes(from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = normalRoutes(from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsEditRoutes(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case Some(AddressYears.OverAYear) =>
        exitMiniJourney(establisherIndex, partnerIndex, mode, srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addPartnerRoutes(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    val partners = answers.allPartnersAfterDelete(index)

    if (partners.isEmpty) {
      NavigateTo.save(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(
        mode, index, answers.allPartners(index).size, srn))
    }
    else if (partners.lengthCompare(appConfig.maxPartners) < 0) {
      answers.get(AddPartnersId(index)) match {
        case Some(true) =>
          NavigateTo.save(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(mode,
            index, answers.allPartners(index).size, srn))
        case Some(false) =>answers.get(IsEstablisherNewId(index)) match {
          case Some(true) =>
            anyMoreChanges(srn)
          case _ =>
            NavigateTo.save(controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(mode, index, srn))
        }
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
    else {
      NavigateTo.save(controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, index, srn))
    }
  }

  private def listOrAnyMoreChange(establisherIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(IsEstablisherNewId(establisherIndex)) match {
      case Some(true) =>
        NavigateTo.save(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, establisherIndex, srn))
      case _ =>
        anyMoreChanges(srn)
    }
  }

}
