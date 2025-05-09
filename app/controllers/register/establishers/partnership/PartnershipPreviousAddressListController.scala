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

package controllers.register.establishers.partnership

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.establishers.partnership._
import models._
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipPreviousAddressListController @Inject()(val appConfig: FrontendAppConfig,
                                                         override val messagesApi: MessagesApi,
                                                         val userAnswersService: UserAnswersService,
                                                         val navigator: Navigator,
                                                         authenticate: AuthAction,
                                                         getData: DataRetrievalAction,
                                                         allowAccess: AllowAccessActionProvider,
                                                         requireData: DataRequiredAction,
                                                         val auditService: AuditService,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         val view: addressList
                                                        )(implicit val ec: ExecutionContext) extends
  AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).map(get)
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).map {
          vm =>
            post(
              viewModel = vm,
              navigatorId = PartnershipPreviousAddressListId(index),
              dataId = PartnershipPreviousAddressId(index),
              mode = mode,
              context = s"Establisher Partnership Previous Address: ${vm.entityName}",
              postCodeLookupIdForCleanup = PartnershipPreviousAddressPostcodeLookupId(index)
            )
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {
    
    PartnershipDetailsId(index).and(PartnershipPreviousAddressPostcodeLookupId(index)).retrieve.map {
      case partnershipDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.PartnershipPreviousAddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.PartnershipPreviousAddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          title = Message("messages__common__selectPreviousAddress__h1", Message("messages__thePartnership")),
          heading = Message("messages__common__selectPreviousAddress__h1", partnershipDetails.name),
          srn = srn,
          entityName = partnershipDetails.name
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, index,
        srn)))
    )
  }
}
