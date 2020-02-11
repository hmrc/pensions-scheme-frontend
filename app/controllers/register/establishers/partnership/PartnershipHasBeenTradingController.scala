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

package controllers.register.establishers.partnership

import config.FrontendAppConfig
import controllers.HasReferenceNumberController
import controllers.actions._
import forms.HasBeenTradingFormProvider
import identifiers.register.establishers.partnership.{PartnershipDetailsId, PartnershipHasBeenTradingId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

import scala.concurrent.ExecutionContext

class PartnershipHasBeenTradingController @Inject()(override val appConfig: FrontendAppConfig,
                                                    override val messagesApi: MessagesApi,
                                                    override val userAnswersService: UserAnswersService,
                                                    override val navigator: Navigator,
                                                    authenticate: AuthAction,
                                                    allowAccess: AllowAccessActionProvider,
                                                    getData: DataRetrievalAction,
                                                    requireData: DataRequiredAction,
                                                    formProvider: HasBeenTradingFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    val view: hasReferenceNumber,
                                                    implicit val executionContext: ExecutionContext) extends HasReferenceNumberController {

  private def viewModel(mode: Mode, index: Index, srn: Option[String], partnershipName: String)
                       (implicit request: DataRequest[AnyContent]): CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = controllers.register.establishers.partnership.routes.PartnershipHasBeenTradingController.onSubmit(mode, index, srn),
      title = Message("messages__partnership_trading_time__title"),
      heading = Message("messages__hasBeenTrading__h1", partnershipName),
      hint = None,
      srn = srn
    )

  private def form(partnershipName: String)(implicit request: DataRequest[AnyContent]) =
    formProvider("messages__tradingAtLeastOneYear__error", partnershipName)

  def onPageLoad(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            get(PartnershipHasBeenTradingId(index), form(details.name), viewModel(mode, index, srn, details.name))
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String] = None): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnershipDetailsId(index).retrieve.right.map {
          details =>
            post(PartnershipHasBeenTradingId(index), mode, form(details.name), viewModel(mode, index, srn, details.name))
        }
    }
}
