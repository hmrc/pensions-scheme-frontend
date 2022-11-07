/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import audit.AuditService
import config.FrontendAppConfig
import controllers.actions._
import controllers.address.AddressListController
import identifiers._
import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.{ExecutionContext, Future}

class InsurerSelectAddressController @Inject()(override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @InsuranceService val userAnswersService: UserAnswersService,
                                               @AboutBenefitsAndInsurance override val navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               val auditService: AuditService,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addressList
                                              )(implicit val ec: ExecutionContext) extends AddressListController with
  Retrievals {


  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      viewModel(mode, srn).map(get)
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewModel(mode, srn).map {
          vm =>
            post(
              viewModel = vm,
              navigatorId = InsurerSelectAddressId,
              dataId = InsurerConfirmAddressId,
              mode = mode,
              context = s"Insurer Address: ${vm.entityName}",
              postCodeLookupIdForCleanup = InsurerEnterPostCodeId
            )
        }
    }

  private def viewModel(mode: Mode, srn: Option[String])(implicit request: DataRequest[AnyContent])
  : Either[Future[Result],
    AddressListViewModel] = {
    (InsurerEnterPostCodeId and InsuranceCompanyNameId).retrieve.map {
      case addresses ~ name =>
        AddressListViewModel(
          postCall = routes.InsurerSelectAddressController.onSubmit(mode, srn),
          manualInputCall = routes.InsurerConfirmAddressController.onPageLoad(mode, srn),
          addresses = addresses,
          srn = srn,
          heading = Message("messages__dynamic_whatIsAddress", name),
          title = Message("messages__dynamic_whatIsAddress", Message("messages__theInsuranceCompany")),
          entityName = name
        )
    }.left.map(_ =>
      Future.successful(Redirect(routes.InsurerEnterPostcodeController.onPageLoad(mode, srn)))
    )
  }
}
