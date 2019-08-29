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
import controllers.register.establishers.partnership.routes._
import controllers.routes._
import identifiers.register.establishers.partnership._
import identifiers.register.establishers.{ExistingCurrentAddressId, IsEstablisherNewId}
import models.Mode._
import models._
import utils.UserAnswers

class EstablishersPartnershipNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                                 appConfig: FrontendAppConfig) extends AbstractNavigator {

  private def checkYourAnswers(index: Int, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(mode, index, srn))

  private def exitMiniJourney(index: Index, mode: Mode, srn: Option[String], answers: UserAnswers): Option[NavigateTo] =
    if (mode == CheckMode || mode == NormalMode) {
      checkYourAnswers(index, journeyMode(mode), srn)
    } else {
      if (answers.get(IsEstablisherNewId(index)).getOrElse(false))
        checkYourAnswers(index, journeyMode(mode), srn)
      else anyMoreChanges(srn)
    }

  private def anyMoreChanges(srn: Option[String]): Option[NavigateTo] =
    NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))

  //scalastyle:off cyclomatic.complexity
  protected def route(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      NavigateTo.dontSave(PartnershipVatController.onPageLoad(mode, index, srn))
    case PartnershipVatId(index) =>
      NavigateTo.dontSave(PartnershipPayeController.onPageLoad(mode, index, srn))
    case PartnershipPayeId(index) =>
      NavigateTo.dontSave(PartnershipUniqueTaxReferenceController.onPageLoad(mode, index, srn))
    case PartnershipUniqueTaxReferenceID(index) =>
      NavigateTo.dontSave(PartnershipPostcodeLookupController.onPageLoad(mode, index, srn))
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.dontSave(PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.dontSave(PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      NavigateTo.dontSave(PartnershipAddressYearsController.onPageLoad(mode, index, srn))
    case PartnershipAddressYearsId(index) =>
      addressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.dontSave(PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      NavigateTo.dontSave(PartnershipContactDetailsController.onPageLoad(mode, index, srn))
    case CheckYourAnswersId(index) =>
      NavigateTo.dontSave(AddPartnersController.onPageLoad(mode, index, srn))
    case OtherPartnersId(index) =>
      if (mode == CheckMode || mode == NormalMode) {
        NavigateTo.dontSave(PartnershipReviewController.onPageLoad(mode, index, srn))
      } else {
        NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
      }
    case PartnershipReviewId(_) =>
      NavigateTo.dontSave(controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn))
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
    case PartnershipUniqueTaxReferenceID(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.dontSave(PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.dontSave(PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      val isNew = from.userAnswers.get(IsEstablisherNewId(index)).contains(true)
      if (isNew || mode == CheckMode) {
        checkYourAnswers(index, journeyMode(mode), srn)
      } else if (!from.userAnswers.get(IsEstablisherNewId(index)).contains(true) && mode == CheckUpdateMode) {
          NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad(index, srn))
        } else {
        NavigateTo.dontSave(PartnershipAddressYearsController.onPageLoad(mode, index, srn))
      }
    case PartnershipAddressYearsId(index) =>
      editAddressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipConfirmPreviousAddressId(index) =>
      confirmPreviousAddressRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.dontSave(PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case PartnershipContactDetailsId(index) =>
      exitMiniJourney(index, mode, srn, from.userAnswers)
    case OtherPartnersId(index) =>
      NavigateTo.dontSave(controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(journeyMode(mode), index, srn))
    case _ =>
      None
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case PartnershipContactDetailsId(index) =>
      NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(NormalMode, index, None))
    case _ => route(from, NormalMode, None)
  }

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipContactDetailsId(index) =>
      NavigateTo.dontSave(CheckYourAnswersController.onPageLoad(UpdateMode, index, srn))
    case PartnershipReviewId(_) =>
      NavigateTo.dontSave(AnyMoreChangesController.onPageLoad(srn))
    case _ => route(from, UpdateMode, srn)
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = editRoutes(from, CheckMode, None)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = editRoutes(from, CheckUpdateMode, srn)

  private def addressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.dontSave(PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.dontSave(PartnershipContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    (
      answers.get(PartnershipAddressYearsId(index)),
      mode,
      answers.get(ExistingCurrentAddressId(index))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad(index, srn))
      case (Some(AddressYears.UnderAYear), _, _) =>
        NavigateTo.dontSave(PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case (Some(AddressYears.OverAYear), _, _) =>
        exitMiniJourney(index, mode, srn, answers)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def confirmPreviousAddressRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipConfirmPreviousAddressId(index)) match {
      case Some(false) =>
        NavigateTo.dontSave(PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(true) =>
        anyMoreChanges(srn)
      case None =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }
}
