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

package controllers

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers._
import javax.inject.Inject
import models.Mode
import models.address.Address
import navigators.Navigator
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import services.UserAnswersService
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

import scala.concurrent.ExecutionContext

class InsurerConfirmAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                val messagesApi: MessagesApi,
                                                @InsuranceService val userAnswersService: UserAnswersService,
                                                @AboutBenefitsAndInsurance val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                val formProvider: AddressFormProvider,
                                                val countryOptions: CountryOptions,
                                                val auditService: AuditService
                                               )(implicit val ec: ExecutionContext) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.InsurerConfirmAddressController.onSubmit _
  private[controllers] val title: Message = "messages__insurer_confirm_address__title"
  private[controllers] val heading: Message = "messages__insurer_confirm_address__h1"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(mode: Mode, srn: Option[String]): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, srn),
      countryOptions.options,
      title = Message(title),
      heading = Message(heading),
      secondaryHeader = None,
      srn = srn
    )

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
    implicit request =>
      get(InsurerConfirmAddressId, InsurerSelectAddressId, viewmodel(mode, srn))
  }

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      post(InsurerConfirmAddressId, InsurerSelectAddressId, viewmodel(mode, srn), mode, "Insurer Address", InsurerEnterPostCodeId)
  }

  def onClick(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData).async {
    implicit request =>
      clear(InsurerConfirmAddressId, InsurerSelectAddressId, mode, srn, routes.InsurerConfirmAddressController.onPageLoad(mode, srn))
  }
}
