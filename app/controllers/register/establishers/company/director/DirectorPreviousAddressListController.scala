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

package controllers.register.establishers.company.director

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.company.director._
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import utils.Navigator
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class DirectorPreviousAddressListController @Inject()(
                                                       override val appConfig: FrontendAppConfig,
                                                       override val cacheConnector: UserAnswersCacheConnector,
                                                       @EstablishersCompanyDirector override val navigator: Navigator,
                                                       override val messagesApi: MessagesApi,
                                                       authenticate: AuthAction,
                                                       getData: DataRetrievalAction,
                                                       requireData: DataRequiredAction
                                                     ) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, directorIndex, srn).right.map(get)
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async { implicit request =>
      viewmodel(mode, establisherIndex, directorIndex, srn).right.map {
        vm =>
          post(vm, DirectorPreviousAddressListId(establisherIndex, directorIndex), DirectorPreviousAddressId(establisherIndex, directorIndex), mode)
      }
    }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (DirectorDetailsId(establisherIndex, directorIndex) and DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex))
      .retrieve.right.map {
      case directorDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.DirectorPreviousAddressListController.onSubmit(mode, establisherIndex, directorIndex, srn),
          manualInputCall = routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex, srn),
          addresses = addresses,
          title = Message("messages__select_the_previous_address__title"),
          heading = Message("messages__select_the_previous_address__heading"),
          subHeading = Some(Message(directorDetails.fullName)),
          srn  = srn
        )
    }.left.map(_ => Future.successful(Redirect(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex, srn))))
  }
}
