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

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.establishers.company._
import javax.inject.Inject
import models._
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class CompanyPreviousAddressListController @Inject()(
                                                      val appConfig: FrontendAppConfig,
                                                      val messagesApi: MessagesApi,
                                                      val userAnswersService: UserAnswersService,
                                                      @EstablishersCompany val navigator: Navigator,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      allowAccess: AllowAccessActionProvider,
                                                      requireData: DataRequiredAction
                                                    )(implicit val ec: ExecutionContext) extends AddressListController with Retrievals {


  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, srn, index).right.map(get)
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(mode, srn, index).right.map(vm => post(vm, CompanyPreviousAddressListId(index), CompanyPreviousAddressId(index), mode))
  }

  private def viewmodel(mode: Mode, srn: Option[String], index: Index)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    (CompanyDetailsId(index) and CompanyPreviousAddressPostcodeLookupId(index)).retrieve.right.map {
      case companyDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.CompanyPreviousAddressListController.onSubmit(mode, srn, index),
          manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(mode, srn, index),
          addresses = addresses,
          title = Message("messages__establisherPreviousSelectAddress__title"),
          heading = Message("messages__common__selectPreviousAddress__h1", companyDetails.companyName),
          srn = srn
        )
    }.left.map(_ => Future.successful(Redirect(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, srn, index))))
  }
}
