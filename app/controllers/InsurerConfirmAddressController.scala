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

package controllers

import audit.AuditService
import config.FrontendAppConfig
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
import utils.CountryOptions
import utils.annotations.{AboutBenefitsAndInsurance, InsuranceService}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

import scala.concurrent.ExecutionContext

class InsurerConfirmAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                override val messagesApi: MessagesApi,
                                                @InsuranceService val userAnswersService: UserAnswersService,
                                                @AboutBenefitsAndInsurance val navigator: Navigator,
                                                authenticate: AuthAction,
                                                getData: DataRetrievalAction,
                                                allowAccess: AllowAccessActionProvider,
                                                requireData: DataRequiredAction,
                                                val formProvider: AddressFormProvider,
                                                val countryOptions: CountryOptions,
                                                val auditService: AuditService,
                                                val controllerComponents: MessagesControllerComponents,
                                                val view: manualAddress
                                               )(implicit val ec: ExecutionContext) extends ManualAddressController
  with I18nSupport {

  protected val form: Form[Address] = formProvider()
  private[controllers] val postCall = routes.InsurerConfirmAddressController.onSubmit _
  private[controllers] val title: Message = "messages__insurer_confirm_address__title"
  private[controllers] val heading: String = "messages__common__confirmAddress__h1"

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>

        InsuranceCompanyNameId.retrieve.map { companyName =>
          get(InsurerConfirmAddressId, InsurerSelectAddressId, viewmodel(mode, srn, companyName))
        }
    }

  private def viewmodel(mode: Mode, srn: Option[String], companyName: String): ManualAddressViewModel =
    ManualAddressViewModel(
      postCall(mode, srn),
      countryOptions.options,
      title = title,
      heading = Message(heading, companyName),
      srn = srn
    )

  def onSubmit(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate() andThen getData(mode, srn)
    andThen requireData).async {
    implicit request =>
      InsuranceCompanyNameId.retrieve.map { companyName =>
        post(InsurerConfirmAddressId,
          InsurerSelectAddressId,
          viewmodel(mode, srn, companyName),
          mode,
          "Insurer Address",
          InsurerEnterPostCodeId)
      }
  }
}
