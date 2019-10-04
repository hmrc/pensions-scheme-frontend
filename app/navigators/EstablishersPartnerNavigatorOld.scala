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
import controllers.register.establishers.partnership.partner._
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership.AddPartnersId
import identifiers.register.establishers.partnership.partner._
import models.Mode.journeyMode
import models._
import models.requests.IdentifiedRequest
import navigators.establishers.partnership.partner.PartnerNavigator
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Toggles, UserAnswers}

import scala.concurrent.ExecutionContext

class PartnerFeatureSwitchNavigator @Inject()(
                                                              featureSwitchService: FeatureSwitchManagementService,
                                                              oldNavigator: EstablishersPartnerNavigatorOld,
                                                              partnerNavigator: PartnerNavigator
                                                            ) extends Navigator {

  override def nextPageOptional(id: Identifier,
                                mode: Mode,
                                userAnswers: UserAnswers,
                                srn: Option[String])(
                                 implicit ex: IdentifiedRequest,
                                 ec: ExecutionContext,
                                 hc: HeaderCarrier): Option[Call] = {
    if (featureSwitchService.get(Toggles.isHnSEnabled)) {
      partnerNavigator.nextPageOptional(id, mode, userAnswers, srn)
    } else {
      oldNavigator.nextPageOptional(id, mode, userAnswers, srn)
    }
  }
}

class EstablishersPartnerNavigatorOld @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                                appConfig: FrontendAppConfig,
                                                fs: FeatureSwitchManagementService) extends AbstractNavigator {

  private def isHnsEnabled = fs.get(Toggles.isHnSEnabled)
  //scalastyle:off cyclomatic.complexity
  private def checkYourAnswers(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(mode, establisherIndex, partnerIndex, srn))

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  private def exitMiniJourney(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    mode match {
      case CheckMode | NormalMode =>
        checkYourAnswers(establisherIndex, partnerIndex, journeyMode(mode), srn)
      case _ =>
        if (answers.get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false))
          checkYourAnswers(establisherIndex, partnerIndex, journeyMode(mode), srn)
        else anyMoreChanges(srn)
    }

  protected def normalRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case AddPartnersId(establisherIndex) =>
      addPartnerRoutes(mode, establisherIndex, from.userAnswers, srn)
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerNinoController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressYearsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsRoutes(establisherIndex, partnerIndex, mode, srn)(from.userAnswers)
    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerPreviousAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerContactDetailsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
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
      NavigateTo.dontSave(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, establisherIndex, srn))
    case _ =>
      None
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnerDetailsId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case PartnerNinoId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case PartnerNewNinoId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case PartnerUniqueTaxReferenceId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerAddressId(establisherIndex, partnerIndex) =>
      val isNew = from.userAnswers.get(IsNewPartnerId(establisherIndex, partnerIndex)).contains(true)
      if (isNew || mode == CheckMode) {
        checkYourAnswers(establisherIndex, partnerIndex, journeyMode(mode), srn)
      } else if (!from.userAnswers.get(IsNewPartnerId(establisherIndex, partnerIndex)).contains(true) && mode == CheckUpdateMode) {
        NavigateTo.dontSave(routes.PartnerConfirmPreviousAddressController.onPageLoad(establisherIndex, partnerIndex, srn))
      } else {

        NavigateTo.dontSave(routes.PartnerAddressYearsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      }
    case PartnerAddressYearsId(establisherIndex, partnerIndex) =>
      addressYearsEditRoutes(establisherIndex, partnerIndex, mode, srn)(from.userAnswers)

    case PartnerConfirmPreviousAddressId(establisherIndex, partnerIndex) => confirmPreviousAddressRoutes(establisherIndex, partnerIndex, mode, srn)(from.userAnswers)


    case PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressListId(establisherIndex, partnerIndex) =>
      NavigateTo.dontSave(routes.PartnerPreviousAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
    case PartnerPreviousAddressId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case PartnerContactDetailsId(establisherIndex, partnerIndex) =>
      exitMiniJourney(establisherIndex, partnerIndex, mode, srn, from.userAnswers)
    case _ => None
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = normalRoutes(from, NormalMode, None)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = normalRoutes(from, UpdateMode, srn)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(routes.PartnerContactDetailsController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsEditRoutes(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (
      answers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)),
      mode,
      answers.get(ExistingCurrentAddressId(establisherIndex, partnerIndex))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        NavigateTo.dontSave(routes.PartnerConfirmPreviousAddressController.onPageLoad(establisherIndex, partnerIndex, srn))
      case (Some(AddressYears.UnderAYear), _, _) =>
        NavigateTo.dontSave(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case (Some(AddressYears.OverAYear), _, _) =>
        exitMiniJourney(establisherIndex, partnerIndex, mode, srn, answers)
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addPartnerRoutes(mode: Mode, index: Int, answers: UserAnswers, srn: Option[String]): Option[NavigateTo] = {
    val partners = answers.allPartnersAfterDelete(index, isHnsEnabled)

    if (partners.isEmpty) {
      NavigateTo.dontSave(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(
        mode, index, answers.allPartners(index, isHnsEnabled).size, srn))
    }
    else if (partners.lengthCompare(appConfig.maxPartners) < 0) {
      answers.get(AddPartnersId(index)) match {
        case Some(true) =>
          NavigateTo.dontSave(controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(mode,
            index, answers.allPartners(index, isHnsEnabled).size, srn))
        case Some(false) =>
          mode match {
            case CheckMode | NormalMode =>
              NavigateTo.dontSave(controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(mode, index, srn))
            case _ => answers.get(IsEstablisherNewId(index)) match {
              case Some(true) =>
                NavigateTo.dontSave(controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(mode, index, srn))
              case _ =>
                if (answers.get(EstablishersOrTrusteesChangedId).contains(true)) {
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
      NavigateTo.dontSave(controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, index, srn))
    }
  }

  private def confirmPreviousAddressRoutes(establisherIndex: Int, partnerIndex: Int,
                                           mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] =
    answers.get(PartnerConfirmPreviousAddressId(establisherIndex, partnerIndex)) match {
      case Some(false) =>
        NavigateTo.dontSave(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
}
