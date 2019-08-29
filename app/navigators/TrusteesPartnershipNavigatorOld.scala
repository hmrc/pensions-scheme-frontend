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
import controllers.register.trustees.partnership.routes
import controllers.register.trustees.partnership.routes._
import identifiers.Identifier
import identifiers.register.trustees.partnership._
import identifiers.register.trustees.{ExistingCurrentAddressId, IsTrusteeNewId}
import models.Mode.journeyMode
import models._
import models.requests.IdentifiedRequest
import navigators.trustees.partnership.TrusteesPartnershipDetailsNavigator
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Toggles, UserAnswers}

import scala.concurrent.ExecutionContext

class TrusteesPartnershipFeatureSwitchNavigator @Inject() (
                                                           featureSwitchService: FeatureSwitchManagementService,
                                                           oldNavigator: TrusteesPartnershipNavigatorOld,
                                                           navigator: TrusteesPartnershipDetailsNavigator
                                                         ) extends Navigator {

  override def nextPageOptional(id: Identifier,
                                mode: Mode,
                                userAnswers: UserAnswers,
                                srn: Option[String])(
                                 implicit ex: IdentifiedRequest,
                                 ec: ExecutionContext,
                                 hc: HeaderCarrier): Option[Call] =
    if (featureSwitchService.get(Toggles.isEstablisherCompanyHnSEnabled)) {
      navigator.nextPageOptional(id, mode, userAnswers, srn)
    } else {
      oldNavigator.nextPageOptional(id, mode, userAnswers, srn)
    }
}

class TrusteesPartnershipNavigatorOld @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                                appConfig: FrontendAppConfig) extends AbstractNavigator {

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(mode, index, srn))

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode) {
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      if (answers.get(IsTrusteeNewId(index)).getOrElse(false)) checkYourAnswers(index, journeyMode(mode), srn)
      else anyMoreChanges(srn)
    }

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))


  //scalastyle:off cyclomatic.complexity
  protected def commonRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      NavigateTo.dontSave(routes.PartnershipVatController.onPageLoad(mode, index, srn))
    case PartnershipVatId(index) =>
      NavigateTo.dontSave(routes.PartnershipPayeController.onPageLoad(mode, index, srn))
    case PartnershipPayeId(index) =>
      NavigateTo.dontSave(routes.PartnershipUniqueTaxReferenceController.onPageLoad(mode, index, srn))
    case PartnershipUniqueTaxReferenceId(index) =>
      NavigateTo.dontSave(routes.PartnershipPostcodeLookupController.onPageLoad(mode, index, srn))
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.dontSave(routes.PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.dontSave(routes.PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      NavigateTo.dontSave(routes.PartnershipAddressYearsController.onPageLoad(mode, index, srn))
    case PartnershipAddressYearsId(index) =>
      addressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.dontSave(routes.PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.dontSave(routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      NavigateTo.dontSave(routes.PartnershipContactDetailsController.onPageLoad(mode, index, srn))
    case PartnershipContactDetailsId(index) =>
      NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(mode, index, srn))
    case CheckYourAnswersId(_) =>
      NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn))
    case _ =>
      None
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipVatId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipEnterVATId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipPayeId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipPayeVariationsId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipUniqueTaxReferenceId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.dontSave(routes.PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.dontSave(routes.PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      if (from.userAnswers.get(IsTrusteeNewId(index)).contains(true) || mode == CheckMode) {
        checkYourAnswers(index, journeyMode(mode), srn)
      } else if (!from.userAnswers.get(IsTrusteeNewId(index)).contains(true) && mode == CheckUpdateMode) {
        NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad(index, srn))
      } else {
        NavigateTo.dontSave(routes.PartnershipAddressYearsController.onPageLoad(mode, index, srn))
      }
    case PartnershipAddressYearsId(index) =>
      editAddressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipConfirmPreviousAddressId(index) =>
      confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.dontSave(routes.PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.dontSave(routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipContactDetailsId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case _ =>
      None
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = commonRoutes(from, NormalMode, None)

  protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = commonRoutes(from, UpdateMode, srn)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(routes.PartnershipContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (
      answers.get(PartnershipAddressYearsId(index)),
      mode,
      answers.get(ExistingCurrentAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        NavigateTo.dontSave(routes.PartnershipConfirmPreviousAddressController.onPageLoad(index, srn))
      case (Some(AddressYears.UnderAYear), _, _) =>
        NavigateTo.dontSave(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case (Some(AddressYears.OverAYear), _, _) =>
        exitMiniJourney(index, mode, srn, answers)
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
