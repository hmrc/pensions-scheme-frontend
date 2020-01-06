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

package controllers.register.trustees.company

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.trustees.company.{CompanyAddressId, CompanyAddressListId, CompanyDetailsId, CompanyPostcodeLookupId}
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

class CompanyAddressListController @Inject()(override val appConfig: FrontendAppConfig,
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
      (CompanyDetailsId(index) and CompanyPostcodeLookupId(index)).retrieve.right
        .map {
          case companyDetails ~ addresses =>
            get(viewmodel(mode, index, srn, companyDetails.companyName, addresses))
        }
        .left
        .map(_ => Future.successful(Redirect(routes.CompanyPostCodeLookupController.onPageLoad(mode, index, srn))))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (CompanyDetailsId(index) and CompanyPostcodeLookupId(index)).retrieve.right
        .map {
          case companyDetails ~ addresses =>
            val context = s"Trustee Company Address: ${companyDetails.companyName}"
            post(
              viewmodel(mode, index, srn, companyDetails.companyName, addresses),
              CompanyAddressListId(index),
              CompanyAddressId(index),
              mode,
              context,
              CompanyPostcodeLookupId(index)
            )
        }
        .left
        .map(_ => Future.successful(Redirect(routes.CompanyPostCodeLookupController.onPageLoad(mode, index, srn))))
    }

  private def viewmodel(mode: Mode, index: Index, srn: Option[String], name: String, addresses: Seq[TolerantAddress])(
      implicit request: DataRequest[AnyContent]): AddressListViewModel =
    AddressListViewModel(
      postCall = routes.CompanyAddressListController.onSubmit(mode, index, srn),
      manualInputCall = routes.CompanyAddressController.onPageLoad(mode, index, srn),
      addresses = addresses,
      srn = srn,
      title = Message("messages__dynamic_whatIsAddress", Message("messages__theCompany")),
      heading = Message("messages__dynamic_whatIsAddress", name)
    )
}
