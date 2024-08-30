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

package controllers.register.trustees.company

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressListController
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPreviousAddressId, CompanyPreviousAddressListId, CompanyPreviousAddressPostcodeLookupId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode, SchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class CompanyPreviousAddressListController @Inject()(override val appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     val userAnswersService: UserAnswersService,
                                                     override val navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     requireData: DataRequiredAction,
                                                     val auditService: AuditService,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: addressList
                                                    )(implicit val ec: ExecutionContext) extends
  AddressListController with Retrievals {

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] = {
    println(s"**********************************************")
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, index, srn).map(get)
    }
  }

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async { implicit request =>
      viewModel(mode, index, srn).map {
        vm =>
          post(
            viewModel = vm,
            navigatorId = CompanyPreviousAddressListId(index),
            dataId = CompanyPreviousAddressId(index),
            mode = mode,
            context = s"Trustee Company Previous Address: ${vm.entityName}",
            postCodeLookupIdForCleanup = CompanyPreviousAddressPostcodeLookupId(index)
          )
      }
    }

  private def viewModel(mode: Mode, index: Index, srn: SchemeReferenceNumber)
                       (implicit request: DataRequest[AnyContent]): Either[Future[Result], AddressListViewModel] = {

    (CompanyDetailsId(index) and CompanyPreviousAddressPostcodeLookupId(index)).retrieve.map {
      case companyDetails ~ addresses =>
        AddressListViewModel(
          postCall = routes.CompanyPreviousAddressListController.onSubmit(mode, index, srn),
          manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(mode, index, srn),
          addresses = addresses,
          title = Message("messages__select_the_previous_address__heading", Message("messages__theCompany")),
          heading = Message("messages__select_the_previous_address__heading", companyDetails.companyName),
          srn = srn,
          entityName = companyDetails.companyName
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)))
    )
  }
}
