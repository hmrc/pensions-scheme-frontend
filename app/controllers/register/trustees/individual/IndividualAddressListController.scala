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

package controllers.register.trustees.individual

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.trustees.individual._
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class IndividualAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                val userAnswersService: UserAnswersService,
                                                override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                fs: FeatureSwitchManagementService
                                               )(implicit val ec: ExecutionContext) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, index, srn).fold {
          Future.successful(Redirect(routes.IndividualPostCodeLookupController.onPageLoad(mode, index, srn)))
        } {
          get
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, index, srn).fold {
        Future.successful(Redirect(routes.IndividualPostCodeLookupController.onPageLoad(mode, index, srn)))
      } {
        post(_, IndividualAddressListId(index), TrusteeAddressId(index), mode)
      }
  }

  private def viewmodel(mode: Mode,
                        index: Index,
                        srn: Option[String]
                       )(implicit request: DataRequest[AnyContent]): Option[AddressListViewModel] = {

    for {
      addresses <- IndividualPostCodeLookupId(index).retrieve.right.toOption
      name      <- TrusteeNameId(index).retrieve.right.toOption.map(_.fullName)
    } yield AddressListViewModel(
      postCall = routes.IndividualAddressListController.onSubmit(mode, index, srn),
      manualInputCall = routes.TrusteeAddressController.onPageLoad(mode, index, srn),
      addresses = addresses,
      title = Message("messages__trustee__individual__address__title"),
      heading = Message("messages__trustee__individual__address__heading", name),
      srn = srn
    )

  }
}
