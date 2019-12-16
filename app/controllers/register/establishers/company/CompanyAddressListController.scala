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

package controllers.register.establishers.company

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import controllers.register.establishers.company.routes._
import identifiers.register.establishers.company.{CompanyAddressId, CompanyAddressListId, CompanyDetailsId, CompanyPostCodeLookupId}
import models.address.TolerantAddress
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressListController @Inject()(
    override val appConfig: FrontendAppConfig,
    val userAnswersService: UserAnswersService,
    @EstablishersCompany override val navigator: Navigator,
    override val messagesApi: MessagesApi,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    allowAccess: AllowAccessActionProvider,
    requireData: DataRequiredAction,
    val auditService: AuditService
)(implicit val ec: ExecutionContext)
    extends AddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      (CompanyDetailsId(index) and CompanyPostCodeLookupId(index)).retrieve.right
        .map {
          case companyDetails ~ addresses =>
            get(viewmodel(mode, srn, index, companyDetails.companyName, addresses))
        }
        .left
        .map(_ => Future.successful(Redirect(CompanyPostCodeLookupController.onPageLoad(mode, srn, index))))
    }

  private def viewmodel(mode: Mode, srn: Option[String], index: Index, companyName: String, addresses: Seq[TolerantAddress])(
      implicit request: DataRequest[AnyContent]): AddressListViewModel =
    AddressListViewModel(
      postCall = routes.CompanyAddressListController.onSubmit(mode, srn, index),
      manualInputCall = routes.CompanyAddressController.onPageLoad(mode, srn, index),
      addresses = addresses,
      title = Message("messages__establisherSelectAddress__title"),
      heading = Message("messages__establisherSelectAddress__h1", companyName),
      srn = srn
    )

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (CompanyDetailsId(index) and CompanyPostCodeLookupId(index)).retrieve.right
        .map {
          case companyDetails ~ addresses =>
            val context = s"Establisher Company Address: ${companyDetails.companyName}"
            post(viewmodel(mode, srn, index, companyDetails.companyName, addresses),
              CompanyAddressListId(index), CompanyAddressId(index), mode, context)
        }
        .left
        .map(_ => Future.successful(Redirect(CompanyPostCodeLookupController.onPageLoad(mode, srn, index))))
    }
}
