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

package controllers.register.establishers.company.director

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorPreviousAddressId, DirectorPreviousAddressPostcodeLookupId}
import models.address.Address
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class DirectorPreviousAddressListController @Inject()(
        override val appConfig: FrontendAppConfig,
        override val cacheConnector: DataCacheConnector,
        override val navigator: Navigator,
        override val messagesApi: MessagesApi,
        authenticate: AuthAction,
        getData: DataRetrievalAction,
        requireData: DataRequiredAction) extends AddressListController {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>

      addressListViewModel(mode, establisherIndex, directorIndex) match {
        case Some(viewModel) => get(viewModel)
        case _ => redirectToPostCodeLookup(mode, establisherIndex, directorIndex)
      }

  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>

      addressListViewModel(mode, establisherIndex, directorIndex) match {
        case Some(viewModel) => post(viewModel, DirectorPreviousAddressId(establisherIndex, directorIndex), mode)
        case _ => redirectToPostCodeLookup(mode, establisherIndex, directorIndex)
      }

  }

  private def addressListViewModel(mode: Mode, establisherIndex: Index, directorIndex: Index)
                                  (implicit request: DataRequest[AnyContent]): Option[AddressListViewModel] = {

    retrieve(establisherIndex, directorIndex).map { case (directorName, addresses) =>
      val postCall = routes.DirectorPreviousAddressListController.onSubmit(mode, establisherIndex, directorIndex)
      val manualInputCall = routes.DirectorPreviousAddressController.onPageLoad(establisherIndex, directorIndex)

      AddressListViewModel(
        postCall,
        manualInputCall,
        addresses,
        subHeading = Some(Message(directorName))
      )
    }

  }

  private def retrieve(establisherIndex: Index, directorIndex: Index)(implicit request: DataRequest[AnyContent]): Option[(String, Seq[Address])] = {

    (
      request.userAnswers.get(DirectorDetailsId(establisherIndex, directorIndex)),
      request.userAnswers.get(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex))
    ) match {
      case (Some(directorDetails), Some(addresses)) => Some((directorDetails.directorName, addresses))
      case _ => None
    }

  }

  private def redirectToPostCodeLookup(mode: Mode, establisherIndex: Index, directorIndex: Index): Future[Result] = {
    Future.successful(Redirect(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(establisherIndex, directorIndex)))
  }

}
