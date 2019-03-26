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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.company.{CompanyAddressYearsId, CompanyDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class CompanyAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               @EstablishersCompany val navigator: Navigator,
                                               val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction
                                             ) extends AddressYearsController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map { companyDetails =>
        get(CompanyAddressYearsId(index), form, viewModel(mode, srn, index, companyDetails.companyName))
      }
  }

  def onSubmit(mode: Mode, srn: Option[String], index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      CompanyDetailsId(index).retrieve.right.map { companyDetails =>
        post(CompanyAddressYearsId(index), mode, form, viewModel(mode, srn, index, companyDetails.companyName))
      }
  }

  private def viewModel(mode: Mode, srn: Option[String], index: Index, companyName: String) = AddressYearsViewModel(
    postCall = routes.CompanyAddressYearsController.onSubmit(mode, srn, index),
    title = Message("messages__company_address_years__title"),
    heading = Message("messages__company_address_years__title"),
    legend = Message("messages__company_address_years__title"),
    subHeading = Some(Message(companyName)),
    srn = srn
  )
}
