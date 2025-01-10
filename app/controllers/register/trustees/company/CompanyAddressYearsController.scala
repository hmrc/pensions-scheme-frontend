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
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.company.{CompanyAddressYearsId, CompanyDetailsId}

import javax.inject.Inject
import models.requests.DataRequest
import models.{AddressYears, Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext

class CompanyAddressYearsController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               override val navigator: Navigator,
                                               val userAnswersService: UserAnswersService,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressYearsFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addressYears
                                             )(implicit val ec: ExecutionContext) extends controllers.address
.AddressYearsController {

  def onPageLoad(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.map {
          vm =>
            get(CompanyAddressYearsId(index), form, vm)
        }
    }

  def onSubmit(mode: Mode, index: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(index, mode, srn).retrieve.map {
          vm =>
            post(CompanyAddressYearsId(index), mode, form, vm)
        }
    }

  private def viewmodel(index: Index, mode: Mode, srn: OptionalSchemeReferenceNumber): Retrieval[AddressYearsViewModel] =
    Retrieval(
      implicit request =>
        CompanyDetailsId(index.id).retrieve.map {
          details =>
            val questionText = "messages__company_address_years__title"
            val title = "messages__company_trustee_address_years__title"
            val heading = "messages__company_trustee_address_years__heading"
            AddressYearsViewModel(
              postCall = routes.CompanyAddressYearsController.onSubmit(mode, index, srn),
              title = Message(title),
              heading = Message(heading, details.companyName),
              legend = Message(questionText, details.companyName),
              Some(details.companyName),
              srn = srn
            )
        }
    )

  private def form(implicit request: DataRequest[AnyContent]): Form[AddressYears] =
    formProvider(Message("messages__common_error__current_address_years"))
}
