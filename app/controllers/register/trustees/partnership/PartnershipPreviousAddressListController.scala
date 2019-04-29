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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.trustees.partnership._
import javax.inject.Inject
import models._
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.TrusteesPartnership
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class PartnershipPreviousAddressListController @Inject()(
                                                          val appConfig: FrontendAppConfig,
                                                          val messagesApi: MessagesApi,
                                                          val userAnswersService: UserAnswersService,
                                                          @TrusteesPartnership val navigator: Navigator,
                                                          authenticate: AuthAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction
                                                        ) extends AddressListController with Retrievals {


  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).right.map(get)
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).right.map(vm => post(vm, PartnershipPreviousAddressListId(index), PartnershipPreviousAddressId(index), mode))
  }

  private def viewmodel(mode: Mode, index: Index, srn: Option[String])(implicit request: DataRequest[AnyContent]):
  Either[Future[Result], AddressListViewModel] = {
    (PartnershipDetailsId(index) and PartnershipPreviousAddressPostcodeLookupId(index)).retrieve.right.map {
      case partnershipDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnershipPreviousAddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          title = Message("messages__select_the_previous_address__title"),
          heading = Message("messages__select_the_previous_address__heading"),
          subHeading = Some(partnershipDetails.name),
          srn = srn
        )
    }.left.map(_ => Future.successful(Redirect(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn))))
  }
}
