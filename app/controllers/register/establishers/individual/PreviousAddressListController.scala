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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.{AddressListController => GenericAddressListController}
import identifiers.register.establishers.individual._
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersIndividual
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class PreviousAddressListController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               val userAnswersService: UserAnswersService,
                                               @EstablishersIndividual override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction
                                             ) extends GenericAddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        println( "\n>>" + mode)
        viewmodel(Mode.journeyMode(mode), index, srn).right.map(get)
    }

  private def viewmodel(mode: Mode, index: Index, srn: Option[String])(implicit request: DataRequest[AnyContent]):
  Either[Future[Result], AddressListViewModel] = {
    (EstablisherDetailsId(index) and PreviousPostCodeLookupId(index)).retrieve.right.map {
      case establisherDetails ~ addresses => AddressListViewModel(
        postCall = routes.PreviousAddressListController.onSubmit(mode, index, srn),
        manualInputCall = routes.PreviousAddressController.onPageLoad(mode, index, srn),
        addresses = addresses,
        title = Message("messages__select_the_previous_address__title"),
        heading = Message("messages__select_the_previous_address__title"),
        subHeading = Some(Message(establisherDetails.fullName)),
        srn = srn,
        mode = mode
      )
    }.left.map(_ =>
      Future.successful(Redirect(routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, index, srn))))
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(Mode.journeyMode(mode), index, srn).right.map {
        vm =>
          post(vm, PreviousAddressListId(index), PreviousAddressId(index), mode)
      }
  }
}
