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
import controllers.register.trustees.partnership.routes
import identifiers.register.trustees.partnership._
import models.{AddressYears, CheckMode, CheckUpdateMode, Mode, NormalMode, UpdateMode}
import models.Mode.journeyMode
import utils.{Navigator, UserAnswers}

class TrusteesPartnershipNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                             appConfig: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int, answers: UserAnswers, mode: Mode, srn: Option[String]): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(mode, index, srn))

  //scalastyle:off cyclomatic.complexity
  protected def commonRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      NavigateTo.save(routes.PartnershipVatController.onPageLoad(mode, index, srn))
    case PartnershipVatId(index) =>
      NavigateTo.save(routes.PartnershipPayeController.onPageLoad(mode, index, srn))
    case PartnershipPayeId(index) =>
      NavigateTo.save(routes.PartnershipUniqueTaxReferenceController.onPageLoad(mode, index, srn))
    case PartnershipUniqueTaxReferenceId(index) =>
      NavigateTo.save(routes.PartnershipPostcodeLookupController.onPageLoad(mode, index, srn))
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.save(routes.PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      NavigateTo.save(routes.PartnershipAddressYearsController.onPageLoad(mode, index, srn))
    case PartnershipAddressYearsId(index) =>
      addressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      NavigateTo.save(routes.PartnershipContactDetailsController.onPageLoad(mode, index, srn))
    case PartnershipContactDetailsId(index) =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(mode, index, srn))
    case CheckYourAnswersId(_) =>
      NavigateTo.dontSave(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn))
    case _ =>
      None
  }

  protected def editRoutes(from: NavigateFrom, mode: Mode, srn: Option[String]): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipVatId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipPayeId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipUniqueTaxReferenceId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipAddressListController.onPageLoad(mode, index, srn))
    case PartnershipAddressListId(index) =>
      NavigateTo.save(routes.PartnershipAddressController.onPageLoad(mode, index, srn))
    case PartnershipAddressId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipAddressYearsId(index) =>
      editAddressYearsRoutes(index, mode, srn)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressListController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn))
    case PartnershipPreviousAddressId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
    case PartnershipContactDetailsId(index) =>
      checkYourAnswers(index, from.userAnswers, journeyMode(mode), srn)
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
        NavigateTo.save(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.PartnershipContactDetailsController.onPageLoad(mode, index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def editAddressYearsRoutes(index: Int, mode: Mode, srn: Option[String])(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(controllers.register.trustees.partnership.routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(journeyMode(mode), index, srn))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
