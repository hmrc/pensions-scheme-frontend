/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.establishers.partnership

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.partnership.{PartnershipAddressId, PartnershipAddressListId,
  PartnershipDetailsId, PartnershipPostcodeLookupId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class PartnershipAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 val userAnswersService: UserAnswersService,
                                                 override val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 requireData: DataRequiredAction,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: addressList,
                                                 val auditService: AuditService
                                                )(implicit val ec: ExecutionContext) extends AddressListController
  with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).right.map(get)
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).right.map {
          vm =>
            post(
              viewModel = vm,
              navigatorId = PartnershipAddressListId(index),
              dataId = PartnershipAddressId(index),
              mode = mode,
              context = s"Partnership Address: ${vm.entityName}",
              postCodeLookupIdForCleanup = PartnershipPostcodeLookupId(index)
            )
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] =

    (PartnershipDetailsId(index) and PartnershipPostcodeLookupId(index)).retrieve.right.map {
      case partnershipDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnershipAddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.PartnershipAddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          srn = srn,
          title = Message("messages__establisherSelectAddress__h1", Message("messages__thePartnership")),
          heading = Message("messages__establisherSelectAddress__h1", partnershipDetails.name),
          entityName = partnershipDetails.name
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.PartnershipPostcodeLookupController.onPageLoad(mode, index, srn)))
    )
}
