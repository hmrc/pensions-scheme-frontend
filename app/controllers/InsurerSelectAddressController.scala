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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.AddressListController
import identifiers._
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.AboutBenefitsAndInsurance
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class InsurerSelectAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val cacheConnector: UserAnswersCacheConnector,
                                               @AboutBenefitsAndInsurance override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction) extends AddressListController with Retrievals {


  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, srn).right.map(get)
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewModel(mode, srn).right.map {
        vm =>
          post(vm, InsurerSelectAddressId, InsurerConfirmAddressId, mode)
      }
  }

  private def viewModel(mode: Mode, srn: Option[String])(implicit request: DataRequest[AnyContent]): Either[Future[Result],
    AddressListViewModel] = {
    (InsurerEnterPostCodeId).retrieve.right.map {
      case addresses =>
        AddressListViewModel(
          postCall = routes.InsurerSelectAddressController.onSubmit(mode, srn),
          manualInputCall = routes.InsurerConfirmAddressController.onPageLoad(mode, srn),
          addresses = addresses,
          srn = srn
        )
    }.left.map(_ => Future.successful(Redirect(routes.InsurerEnterPostcodeController.onPageLoad(mode, srn))))
  }
}
