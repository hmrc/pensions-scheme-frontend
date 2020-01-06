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
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.trustees.individual._
import javax.inject.Inject
import models.address.TolerantAddress
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class TrusteePreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     override val navigator: Navigator,
                                                     val userAnswersService: UserAnswersService,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     val auditService: AuditService)(implicit val ec: ExecutionContext)
    extends AddressListController
    with Retrievals
    with I18nSupport {

  def viewmodel(mode: Mode, index: Index, srn: Option[String], name: String, addresses: Seq[TolerantAddress])(
      implicit request: DataRequest[AnyContent]): AddressListViewModel =
    AddressListViewModel(
      routes.TrusteePreviousAddressListController.onSubmit(mode, index, srn),
      routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn),
      addresses,
      title = Message("messages__trustee__individual__previous__address__heading", Message("messages__theIndividual")),
      heading = Message("messages__trustee__individual__previous__address__heading", name),
      srn = srn
    )

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      (trusteeName(index) and IndividualPreviousAddressPostCodeLookupId(index)).retrieve.right
        .map {
          case name ~ addresses =>
            get(viewmodel(mode, index, srn, name, addresses))
        }
        .left
        .map(_ => Future.successful(Redirect(routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))))
    }

  val trusteeName: Index => Retrieval[String] = (trusteeIndex: Index) =>
    Retrieval { implicit request =>
      TrusteeNameId(trusteeIndex).retrieve.right.map(_.fullName)
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      (trusteeName(index) and IndividualPreviousAddressPostCodeLookupId(index)).retrieve.right
        .map {
          case name ~ addresses =>
            val context = s"Trustee Individual Previous Address: $name"
            post(
              viewmodel(mode, index, srn, name, addresses),
              TrusteePreviousAddressListId(index),
              TrusteePreviousAddressId(index),
              mode,
              context,
              IndividualPreviousAddressPostCodeLookupId(index)
            )
        }
        .left
        .map(_ => Future.successful(Redirect(routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))))

    }
}
