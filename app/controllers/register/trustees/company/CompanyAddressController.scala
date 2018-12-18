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

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.trustees.company.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.company.{CompanyAddressId, CompanyAddressListId, CompanyDetailsId, CompanyPostcodeLookupId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.TrusteesCompany
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class CompanyAddressController @Inject()(
                                          val appConfig: FrontendAppConfig,
                                          val messagesApi: MessagesApi,
                                          val dataCacheConnector: UserAnswersCacheConnector,
                                          @TrusteesCompany val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions,
                                          val auditService: AuditService
                                        ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = CompanyAddressController.onSubmit _
  private[controllers] val title: Message = "messages__companyAddress__title"
  private[controllers] val heading: Message = "messages__companyAddress__heading"
  private[controllers] val hint: Message = "messages__companyAddress__trustee__lede"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.companyName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          get(CompanyAddressId(index), CompanyAddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          post(CompanyAddressId(index), CompanyAddressListId(index), vm, mode, context(vm),Some(CompanyPostcodeLookupId(index)))
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Trustee Company Address: $name"
      case _ => "Trustee Company Address"
    }
  }

}
