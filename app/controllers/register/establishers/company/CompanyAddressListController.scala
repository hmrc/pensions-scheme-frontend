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

package controllers.register.establishers.company

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import controllers.register.establishers.company.routes._
import identifiers.register.establishers.company.{CompanyAddressId, CompanyAddressListId, CompanyDetailsId, CompanyPostCodeLookupId}
import models.requests.DataRequest
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.Future

class CompanyAddressListController @Inject()(
                                              override val appConfig: FrontendAppConfig,
                                              val userAnswersService: UserAnswersService,
                                              @EstablishersCompany override val navigator: Navigator,
                                              override val messagesApi: MessagesApi,
                                              authenticate: AuthAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction
                                            ) extends AddressListController with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>
      viewmodel(mode, srn, index).right.map(get)
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async { implicit request =>

      viewmodel(mode, srn, index).right.map {
        vm =>
          post(vm, CompanyAddressListId(index), CompanyAddressId(index), mode)
      }
    }

  private def viewmodel(mode: Mode, srn: Option[String], index: Index)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (CompanyDetailsId(index) and CompanyPostCodeLookupId(index)).retrieve.right.map {
      case companyDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.CompanyAddressListController.onSubmit(mode, srn, index),
          manualInputCall = routes.CompanyAddressController.onClick(mode, srn, index),
          addresses = addresses,
          subHeading = Some(Message(companyDetails.companyName)),
          srn = srn
        )
    }.left.map(_ => Future.successful(Redirect(CompanyPostCodeLookupController.onPageLoad(mode, srn, index))))
  }
}
