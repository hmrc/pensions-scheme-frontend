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

package controllers.register.establishers.company

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.company.{CompanyAddressYearsId, CompanyDetailsId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class CompanyAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,

                                               val userAnswersService: UserAnswersService,
                                               @EstablishersCompany val navigator: Navigator,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               val view: addressYears,
                                               val controllerComponents: MessagesControllerComponents
                                             )(implicit val ec: ExecutionContext) extends AddressYearsController with
  Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        CompanyDetailsId(index).retrieve.map { companyDetails =>
          get(CompanyAddressYearsId(index), form(companyDetails.companyName), viewModel(mode, srn, index,
            companyDetails.companyName))
        }
    }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.map { companyDetails =>
        post(CompanyAddressYearsId(index), mode, form(companyDetails.companyName), viewModel(mode, srn, index,
          companyDetails.companyName))
      }
  }

  private def form(companyName: String)(implicit request: DataRequest[AnyContent]) =
    new AddressYearsFormProvider()(Message("messages__company_address_years__form_error", companyName))

  private def viewModel(mode: Mode, srn: Option[String], index: Index, companyName: String) = AddressYearsViewModel(
    postCall = routes.CompanyAddressYearsController.onSubmit(mode, srn, index),
    title = Message("messages__company_address_years__title"),
    heading = Message("messages__company_address_years__h1", companyName),
    legend = Message("messages__company_address_years__title"),
    subHeading = Some(Message(companyName)),
    srn = srn
  )
}
