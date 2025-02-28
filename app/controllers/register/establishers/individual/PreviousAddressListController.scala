/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.establishers.individual

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.{AddressListController => GenericAddressListController}
import identifiers.register.establishers.individual._
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                              override val messagesApi: MessagesApi,
                                              val userAnswersService: UserAnswersService,
                                              val navigator: Navigator,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              allowAccess: AllowAccessActionProvider,
                                              requireData: DataRequiredAction,
                                              val auditService: AuditService,
                                              val view: addressList,
                                              val controllerComponents: MessagesControllerComponents
                                             )(implicit val ec: ExecutionContext) extends
  GenericAddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).map(get)
    }

  private def viewModel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    (EstablisherNameId(index) and PreviousPostCodeLookupId(index)).retrieve.map {
      case name ~ addresses =>
        AddressListViewModel(
          postCall = routes.PreviousAddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.PreviousAddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          title = Message("messages__dynamic_whatWasPreviousAddress", Message("messages__theIndividual")),
          heading = Message("messages__dynamic_whatWasPreviousAddress", name.fullName),
          srn = srn,
          entityName = name.fullName
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, index, srn)))
    )
  }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).map(
          vm => post(
            viewModel = vm,
            navigatorId = PreviousAddressListId(index),
            dataId = PreviousAddressId(index),
            mode = mode,
            context = s"Establisher Individual Previous Address: ${vm.entityName}",
            postCodeLookupIdForCleanup = PreviousPostCodeLookupId(index))
        )
    }
}
