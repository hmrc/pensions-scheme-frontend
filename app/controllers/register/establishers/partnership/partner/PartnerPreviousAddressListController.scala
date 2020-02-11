/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers.partnership.partner

import audit.AuditService
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressListController
import identifiers.register.establishers.partnership.partner._
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Redirect}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class PartnerPreviousAddressListController @Inject()(
                                                      override val appConfig: FrontendAppConfig,
                                                      val userAnswersService: UserAnswersService,
                                                      override val navigator: Navigator,
                                                      override val messagesApi: MessagesApi,
                                                      authenticate: AuthAction,
                                                      getData: DataRetrievalAction,
                                                      allowAccess: AllowAccessActionProvider,
                                                      requireData: DataRequiredAction,
                                                      val auditService: AuditService,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      val view: addressList
                                                    )(implicit val ec: ExecutionContext)
  extends AddressListController
    with Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, partnerIndex, srn).right.map(get)

    }

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String])
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (PartnerNameId(establisherIndex, partnerIndex) and PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)).retrieve.right.map {
      case name ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnerPreviousAddressListController.onSubmit(mode, establisherIndex, partnerIndex, srn),
          manualInputCall = routes.PartnerPreviousAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn),
          addresses = addresses,
          title = Message("messages__select_the_previous_address__heading", Message("messages__thePartner")),
          heading = Message("messages__select_the_previous_address__heading", name.fullName),
          srn = srn,
          entityName = name.fullName
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, partnerIndex, srn)))
    )
  }


    def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, establisherIndex, partnerIndex, srn).right.map { vm =>
          post(
            viewModel = vm,
            navigatorId = PartnerPreviousAddressListId(establisherIndex, partnerIndex),
            dataId = PartnerPreviousAddressId(establisherIndex, partnerIndex),
            mode = mode,
            context = s"Partnership Partner Previous Address: ${vm.entityName}",
            postCodeLookupIdForCleanup = PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)
          )
        }
    }
}
