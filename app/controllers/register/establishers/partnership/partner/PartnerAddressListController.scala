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

package controllers.register.establishers.partnership.partner

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.partnership.partner.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostcodeLookupId, PartnerDetailsId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.EstablishersPartner
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class PartnerAddressListController @Inject()(
                                              override val appConfig: FrontendAppConfig,
                                              override val cacheConnector: UserAnswersCacheConnector,
                                              @EstablishersPartner override val navigator: Navigator,
                                              override val messagesApi: MessagesApi,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction
                                            ) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, partnerIndex).right.map(get)
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, partnerIndex).right.map {
        vm =>
          post(vm, PartnerAddressListId(establisherIndex, partnerIndex), PartnerAddressId(establisherIndex, partnerIndex), mode)
      }
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (PartnerDetailsId(establisherIndex, partnerIndex) and PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex))
      .retrieve.right.map {
      case partnerDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnerAddressListController.onSubmit(mode, establisherIndex, partnerIndex),
          manualInputCall = routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex),
          addresses = addresses,
          subHeading = Some(Message(partnerDetails.fullName))
        )
    }.left.map(_ => Future.successful(Redirect(routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex))))
  }
}
