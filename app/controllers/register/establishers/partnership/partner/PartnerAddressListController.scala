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

package controllers.register.establishers.partnership.partner

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.partnership.partner.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostcodeLookupId, PartnerDetailsId, PartnerNameId}
import identifiers.register.establishers.partnership.partner._
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class PartnerAddressListController @Inject()(
                                              override val appConfig: FrontendAppConfig,
                                              val userAnswersService: UserAnswersService,
                                              override val navigator: Navigator,
                                              override val messagesApi: MessagesApi,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction
                                            )(implicit val ec: ExecutionContext) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, partnerIndex, srn).right.map(get)
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (PartnerNameId(establisherIndex, partnerIndex) and PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex))
      .retrieve.right.map {
      case partnerDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnerAddressListController.onSubmit(mode, establisherIndex, partnerIndex, srn),
          manualInputCall = routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn),
          addresses = addresses,
          srn = srn,
          title = Message("messages__dynamic_whatIsAddress", Message("messages__thePartner").resolve),
          heading = Message("messages__dynamic_whatIsAddress", partnerDetails.fullName)
        )
    }.left.map(_ => Future.successful(Redirect(
      routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))))
  }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, partnerIndex, srn).right.map {
        vm =>
          post(vm, PartnerAddressListId(establisherIndex, partnerIndex), PartnerAddressId(establisherIndex, partnerIndex), mode)
      }
    }
}
