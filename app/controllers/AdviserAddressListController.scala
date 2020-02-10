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

package controllers

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers._
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import utils.annotations.WorkingKnowledge
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class AdviserAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             val userAnswersService: UserAnswersService,
                                             @WorkingKnowledge override val navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             val auditService: AuditService,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: addressList
                                            )(implicit val ec: ExecutionContext) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      viewModel(mode).right.map(get)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      viewModel(mode).right.map {
        vm =>
          post(vm, AdviserAddressListId, AdviserAddressId, mode,"Adviser Address",AdviserAddressPostCodeLookupId)
      }
  }

  private def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]): Either[Future[Result],
    AddressListViewModel] = {
      (AdviserAddressPostCodeLookupId and AdviserNameId).retrieve.right.map {
        case addresses ~ name =>
        AddressListViewModel(
          postCall = routes.AdviserAddressListController.onSubmit(mode),
          manualInputCall = routes.AdviserAddressController.onPageLoad(mode),
          addresses = addresses,
          heading = Message("messages__dynamic_whatIsAddress", name),
          title = Message("messages__dynamic_whatIsAddress", Message("messages__theAdviser"))
        )
    }.left.map(_ => Future.successful(Redirect(routes.AdviserPostCodeLookupController.onPageLoad(mode))))
  }
}
