/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.UserAnswersCacheConnector
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.address.AddressYearsFormProvider
import identifiers.register.trustees.company.{CompanyAddressYearsId, CompanyDetailsId}
import javax.inject.Inject
import models.{AddressYears, Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.TrusteesCompany
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class CompanyAddressYearsController @Inject()(
                                               override val appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               @TrusteesCompany override val navigator: Navigator,
                                               override val cacheConnector: UserAnswersCacheConnector,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: AddressYearsFormProvider
                                             ) extends controllers.address.AddressYearsController {

  private def viewmodel(index: Index, mode: Mode): Retrieval[AddressYearsViewModel] =
    Retrieval(
      implicit request =>
        CompanyDetailsId(index.id).retrieve.right.map {
          details =>
            val questionText = "messages__company_address_years__title"
            AddressYearsViewModel(
              postCall = routes.CompanyAddressYearsController.onSubmit(mode, index),
              title = Message(questionText),
              heading = Message(questionText),
              legend = Message(questionText),
              Some(details.companyName)
            )
        }
    )

  private val form: Form[AddressYears] = formProvider(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right.map {
          vm =>
            get(CompanyAddressYearsId(index), form, vm)
        }
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right.map {
          vm =>
            post(CompanyAddressYearsId(index), mode, form, vm)
        }
    }
}
