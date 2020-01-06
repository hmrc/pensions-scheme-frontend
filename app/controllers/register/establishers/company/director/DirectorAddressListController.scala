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

package controllers.register.establishers.company.director

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.company.director._
import models.address.TolerantAddress
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class DirectorAddressListController @Inject()(
    override val appConfig: FrontendAppConfig,
    val userAnswersService: UserAnswersService,
    @EstablishersCompanyDirector override val navigator: Navigator,
    override val messagesApi: MessagesApi,
    authenticate: AuthAction,
    getData: DataRetrievalAction,
    allowAccess: AllowAccessActionProvider,
    requireData: DataRequiredAction,
    val auditService: AuditService
)(implicit val ec: ExecutionContext)
    extends AddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      (directorName(establisherIndex, directorIndex) and DirectorAddressPostcodeLookupId(establisherIndex, directorIndex)).retrieve.right
        .map {
          case name ~ addresses =>
            get(viewmodel(mode, establisherIndex, directorIndex, srn, name, addresses))
        }
        .left
        .map(_ =>
          Future.successful(
            Redirect(routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))))
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (directorName(establisherIndex, directorIndex) and DirectorAddressPostcodeLookupId(establisherIndex, directorIndex)).retrieve.right
        .map {
          case name ~ addresses =>
            val context = s"Company Director Address: $name"
            post(
              viewmodel(mode, establisherIndex, directorIndex, srn, name, addresses),
              DirectorAddressListId(establisherIndex, directorIndex),
              DirectorAddressId(establisherIndex, directorIndex),
              mode,
              context,
              DirectorAddressPostcodeLookupId(establisherIndex, directorIndex)
            )
        }
        .left
        .map(_ =>
          Future.successful(
            Redirect(routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))))
    }

  private def viewmodel(mode: Mode,
                        establisherIndex: Index,
                        directorIndex: Index,
                        srn: Option[String],
                        name: String,
                        addresses: Seq[TolerantAddress])(implicit request: DataRequest[AnyContent]): AddressListViewModel = {

    AddressListViewModel(
      postCall = routes.DirectorAddressListController.onSubmit(mode, establisherIndex, directorIndex, srn),
      manualInputCall = routes.DirectorAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
      addresses = addresses,
      srn = srn,
      title = Message("messages__dynamic_whatIsAddress", Message("messages__theDirector")),
      heading = Message("messages__dynamic_whatIsAddress", name)
    )

  }

  private val directorName = (establisherIndex: Index, directorIndex: Index) =>
    Retrieval { implicit request =>
      DirectorNameId(establisherIndex, directorIndex).retrieve.right.map(_.fullName)
  }
}
