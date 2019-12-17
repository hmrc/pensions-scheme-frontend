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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.partnership.partner._
import models.address.TolerantAddress
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class PartnerPreviousAddressListController @Inject()(
    override val appConfig: FrontendAppConfig,
    val userAnswersService: UserAnswersService,
    override val navigator: Navigator,
    override val messagesApi: MessagesApi,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    allowAccess: AllowAccessActionProvider,
    requireData: DataRequiredAction,
    val auditService: AuditService
)(implicit val ec: ExecutionContext)
    extends AddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      (partnerName(establisherIndex, partnerIndex) and PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)).retrieve.right
        .map {
          case name ~ addresses =>
            get(viewmodel(mode, establisherIndex, partnerIndex, srn, name, addresses))
        }
        .left
        .map(_ =>
          Future.successful(
            Redirect(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))))
    }

  private def viewmodel(mode: Mode,
                        establisherIndex: Index,
                        directorIndex: Index,
                        srn: Option[String],
                        name: String,
                        addresses: Seq[TolerantAddress])(implicit request: DataRequest[AnyContent]): AddressListViewModel = {
    AddressListViewModel(
      postCall = routes.PartnerPreviousAddressListController.onSubmit(mode, establisherIndex, directorIndex, srn),
      manualInputCall = routes.PartnerPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
      addresses = addresses,
      title = Message("messages__select_the_previous_address__heading", Message("messages__thePartner")),
      heading = Message("messages__select_the_previous_address__heading", name),
      srn = srn
    )
  }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (partnerName(establisherIndex, partnerIndex) and PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)).retrieve.right
        .map {
          case name ~ addresses =>
            val context = s"Partnership Partner Previous Address: $name"
            post(
              viewmodel(mode, establisherIndex, partnerIndex, srn, name, addresses),
              PartnerPreviousAddressListId(establisherIndex, partnerIndex),
              PartnerPreviousAddressId(establisherIndex, partnerIndex),
              mode,
              context
            )
        }
        .left
        .map(_ =>
          Future.successful(
            Redirect(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn))))
    }

  private def partnerName =
    (establisherIndex: Index, partnerIndex: Index) =>
      Retrieval { implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map(_.fullName)
    }
}
