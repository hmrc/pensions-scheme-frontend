/*
 * Copyright 2018 HM Revenue & Customs
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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import controllers.register.establishers.company.routes.CompanyPostCodeLookupController
import identifiers.register.establishers.company.{CompanyAddressId, CompanyDetailsId, CompanyPostCodeLookupId}
import models.{Index, Mode}
import models.address.Address
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class CompanyAddressListController @Inject()(
        override val appConfig: FrontendAppConfig,
        override val cacheConnector: DataCacheConnector,
        override val navigator: Navigator,
        override val messagesApi: MessagesApi,
        authenticate: AuthAction,
        getData: DataRetrievalAction,
        requireData: DataRequiredAction) extends AddressListController {

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async { implicit request =>

    addressListViewModel(mode, index) match {
      case Some(viewModel) => get(viewModel)
      case _ => redirectToPostCodeLookup(mode, index)
    }

  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async { implicit request =>

    addressListViewModel(mode, index) match {
      case Some(viewModel) => post(viewModel, CompanyAddressId(index), mode)
      case _ => redirectToPostCodeLookup(mode, index)
    }

  }

  private def addressListViewModel(mode: Mode, index: Index)(implicit request: DataRequest[AnyContent]): Option[AddressListViewModel] = {

    retrieve(index).map { case (companyName, addresses) =>
        val postCall = routes.CompanyAddressListController.onSubmit(mode, index)
        val manualInputCall = routes.CompanyAddressController.onPageLoad(mode, index)

        AddressListViewModel(
          postCall,
          manualInputCall,
          addresses,
          subHeading = Some(Message(companyName))
        )
    }

  }

  private def retrieve(index: Index)(implicit request: DataRequest[AnyContent]): Option[(String, Seq[Address])] = {

    (request.userAnswers.get(CompanyDetailsId(index)), request.userAnswers.get(CompanyPostCodeLookupId(index))) match {
      case (Some(companyDetails), Some(addresses)) => Some((companyDetails.companyName, addresses))
      case _ => None
    }

  }

  private def redirectToPostCodeLookup(mode: Mode, index: Index): Future[Result] = {
    Future.successful(Redirect(CompanyPostCodeLookupController.onPageLoad(mode, index)))
  }

}
