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

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasBeenTradingFormProvider
import identifiers.register.trustees.company.{CompanyDetailsId, HasBeenTradingCompanyId}
import models.requests.DataRequest
import models.{EmptyOptionalSchemeReferenceNumber, Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class HasBeenTradingCompanyController @Inject()(override val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                override val userAnswersService: UserAnswersService,
                                                override val navigator: Navigator,
                                                authenticate: AuthAction,
                                                allowAccess: AllowAccessActionProvider,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: HasBeenTradingFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: hasReferenceNumber)(
                                                 implicit val executionContext: ExecutionContext) extends
  HasReferenceNumberController {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            get(HasBeenTradingCompanyId(index), form(details.companyName), viewModel(mode, index, srn, details
              .companyName))
        }
    }

  private def viewModel(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber, companyName: String): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.trustees.company.routes.HasBeenTradingCompanyController.onSubmit(mode, index,
        srn),
      title = Message("messages__trustee_company_trading-time__title"),
      heading = Message("messages__hasBeenTrading__h1", companyName),
      hint = None,
      srn = srn
    )

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__tradingAtLeastOneYear__error", companyName)

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map {
          details =>
            post(HasBeenTradingCompanyId(index), mode, form(details.companyName), viewModel(mode, index, srn, details
              .companyName))
        }
    }
}
