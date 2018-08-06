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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import identifiers.register.establishers.partnership._
import models.{AddressYears, NormalMode}
import utils.{Navigator, UserAnswers}
import controllers.register.establishers.partnership._

class EstablishersPartnershipNavigator  @Inject()(val dataCacheConnector: DataCacheConnector, appConfig: FrontendAppConfig) extends Navigator {
  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId(index) =>
      NavigateTo.save(routes.PartnershipVatController.onPageLoad(NormalMode, index))
    case PartnershipVatId(index) =>
      NavigateTo.save(routes.PartnershipPayeController.onPageLoad(NormalMode, index))
    case PartnershipPayeId(index) =>
      NavigateTo.save(routes.PartnershipUniqueTaxReferenceController.onPageLoad(NormalMode, index))
    case PartnershipUniqueTaxReferenceID(index) =>
      NavigateTo.save(routes.PartnershipPostcodeLookupController.onPageLoad(NormalMode, index))
    case PartnershipPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipAddressListController.onPageLoad(NormalMode, index))
    case PartnershipAddressListId(index) =>
      NavigateTo.save(routes.PartnershipAddressController.onPageLoad(NormalMode, index))
    case PartnershipAddressId(index) =>
      NavigateTo.save(routes.PartnershipAddressYearsController.onPageLoad(NormalMode, index))
    case PartnershipAddressYearsId(index) =>
      addressYearsRoutes(index)(from.userAnswers)
    case PartnershipPreviousAddressPostcodeLookupId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressListController.onPageLoad(NormalMode, index))
    case PartnershipPreviousAddressListId(index) =>
      NavigateTo.save(routes.PartnershipPreviousAddressController.onPageLoad(NormalMode, index))
    case PartnershipPreviousAddressId(index) =>
      NavigateTo.save(routes.PartnershipContactDetailsController.onPageLoad(NormalMode, index))
    case PartnershipContactDetailsId(index) =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index))
    case CheckYourAnswersId(index) =>
      NavigateTo.save(controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(index))
    case OtherPartnersId(index) =>
      
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case _ => None
  }

  private def addressYearsRoutes(index: Int)(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.PartnershipContactDetailsController.onPageLoad(NormalMode, index))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
