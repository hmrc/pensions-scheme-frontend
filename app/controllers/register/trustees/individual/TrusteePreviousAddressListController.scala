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

package controllers.register.trustees.individual

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.trustees.individual.{IndividualPreviousAddressPostCodeLookupId, TrusteeDetailsId, TrusteePreviousAddressId, TrusteePreviousAddressListId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, Call}
import utils.Navigator
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class TrusteePreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         override val navigator: Navigator,
                                         override val cacheConnector: DataCacheConnector,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction) extends AddressListController with Retrievals with I18nSupport {

  def viewmodel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]) = {
    (TrusteeDetailsId(index) and IndividualPreviousAddressPostCodeLookupId(index)).retrieve.right.map {
      case trusteeDetails ~ addresses => AddressListViewModel(
        routes.TrusteePreviousAddressListController.onSubmit(mode, index),
        routes.TrusteePreviousAddressController.onPageLoad(mode, index),
        addresses,
        heading = "messages__select_the_previous_address__title",
        subHeading = Some(trusteeDetails.fullName)
      )
    }.left.map(_ => Future.successful(Redirect(routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index))))
  }

  def onPageLoad(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
       viewmodel(mode, index).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, index).right.map(vm => post(vm, TrusteePreviousAddressListId(index), TrusteePreviousAddressId(index), mode))
  }
}
