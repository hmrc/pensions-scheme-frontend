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

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.{AddressListController => GenericAddressListController}
import identifiers.register.establishers.individual.{AddressId, AddressListId, PostCodeLookupId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel

import scala.concurrent.{ExecutionContext, Future}

class AddressListController @Inject()(val appConfig: FrontendAppConfig,
                                      val messagesApi: MessagesApi,
                                      val userAnswersService: UserAnswersService,
                                      val navigator: Navigator,
                                      authenticate: AuthAction,
                                      getData: DataRetrievalAction,
                                      allowAccess: AllowAccessActionProvider,
                                      requireData: DataRequiredAction,
                                      val auditService: AuditService
                                     )(implicit val ec: ExecutionContext)
    extends GenericAddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async { implicit request =>
      retrieveEstablisherName(index) (viewmodel(mode, index, srn, _).right.map(get))
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) { name =>
        val context = s"Establisher Individual Address: $name"
        viewmodel(mode, index, srn, name).right.map( post(_, AddressListId(index), AddressId(index), mode,context, PostCodeLookupId(index))) }
  }

  private def viewmodel(mode: Mode, index: Index, srn: Option[String], establisherName: String)(
      implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    PostCodeLookupId(index).retrieve.right
      .map { addresses =>
        AddressListViewModel(
          postCall = routes.AddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.AddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          srn = srn,
          heading = Message("messages__dynamic_whatIsAddress", establisherName),
          title = Message("messages__dynamic_whatIsAddress", Message("messages__theIndividual").resolve)
        )
      }
      .left
      .map(_ => Future.successful(Redirect(routes.PostCodeLookupController.onPageLoad(mode, index, srn))))
  }
}
