/*
 * Copyright 2024 HM Revenue & Customs
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

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.company.director._
import models.requests.DataRequest
import models.{Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import utils.annotations.EstablishersCompanyDirector
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class DirectorPreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                      val userAnswersService: UserAnswersService,
                                                      @EstablishersCompanyDirector override val navigator: Navigator,
                                                      override val messagesApi: MessagesApi,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      allowAccess: AllowAccessActionProvider,
                                                      requireData: DataRequiredAction,
                                                      val auditService: AuditService,
                                                      val view: addressList,
                                                      val controllerComponents: MessagesControllerComponents
                                                     )(implicit val ec: ExecutionContext) extends
  AddressListController with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, directorIndex, srn).map(get)
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, directorIndex, srn).map {
          vm =>
            post(
              viewModel = vm,
              navigatorId = DirectorPreviousAddressListId(establisherIndex, directorIndex),
              dataId = DirectorPreviousAddressId(establisherIndex, directorIndex),
              mode = mode,
              context = s"Company Director Previous Address: ${vm.entityName}",
              postCodeLookupIdForCleanup = DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex)
            )
        }
    }

  private def viewModel(mode: Mode, establisherIndex: Index, directorIndex: Index, srn: OptionalSchemeReferenceNumber)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] =
    (DirectorNameId(establisherIndex, directorIndex) and DirectorPreviousAddressPostcodeLookupId(establisherIndex,
      directorIndex)).retrieve.map {
      case name ~ addresses =>
        AddressListViewModel(
          postCall = routes.DirectorPreviousAddressListController.onSubmit(mode, establisherIndex, directorIndex, srn),
          manualInputCall = routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex,
            directorIndex, srn),
          addresses = addresses,
          title = Message("messages__select_the_previous_address__heading", Message("messages__theDirector")),
          heading = Message("messages__select_the_previous_address__heading", name.fullName),
          srn = srn,
          entityName = name.fullName
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode,
        establisherIndex, directorIndex, srn)))
    )
}
