/*
 * Copyright 2020 HM Revenue & Customs
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

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.trustees.individual._
import javax.inject.Inject
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

class IndividualAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                val userAnswersService: UserAnswersService,
                                                override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                val auditService: AuditService)(implicit val ec: ExecutionContext)
    extends AddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      (TrusteeNameId(index) and IndividualPostCodeLookupId(index)).retrieve.right
        .map {
          case pn ~ addresses =>
            get(viewmodel(mode, index, srn, pn.fullName, addresses))
        }
        .left
        .map(_ => Future.successful(Redirect(routes.IndividualPostCodeLookupController.onPageLoad(mode, index, srn))))

    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (TrusteeNameId(index) and IndividualPostCodeLookupId(index)).retrieve.right
        .map {
          case pn ~ addresses =>
            post(
              viewmodel(mode, index, srn, pn.fullName, addresses),
              IndividualAddressListId(index),
              TrusteeAddressId(index),
              mode,
              context(pn.fullName),
              IndividualPostCodeLookupId(index)
            )
        }
        .left
        .map(_ => Future.successful(Redirect(routes.IndividualPostCodeLookupController.onPageLoad(mode, index, srn))))

    }

  private def viewmodel(mode: Mode, index: Index, srn: Option[String], name: String, addresses: Seq[TolerantAddress])(
      implicit request: DataRequest[AnyContent]): AddressListViewModel =
    AddressListViewModel(
      postCall = routes.IndividualAddressListController.onSubmit(mode, index, srn),
      manualInputCall = routes.TrusteeAddressController.onPageLoad(mode, index, srn),
      addresses = addresses,
      title = Message("messages__trustee__individual__address__heading", Message("messages__theIndividual")),
      heading = Message("messages__trustee__individual__address__heading", name),
      srn = srn
    )

  private def context(fullName: String): String = s"Trustee Individual Address: $fullName"
}
