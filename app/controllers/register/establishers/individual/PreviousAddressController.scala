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

package controllers.register.establishers.individual

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.individual.{EstablisherDetailsId, PreviousAddressId, PreviousAddressListId, PreviousPostCodeLookupId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.EstablishersIndividual
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PreviousAddressController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val dataCacheConnector: UserAnswersCacheConnector,
                                           @EstablishersIndividual val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           val formProvider: AddressFormProvider,
                                           val countryOptions: CountryOptions,
                                           val auditService: AuditService
                                         ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.PreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__establisher_individual_previous_address__title"
  private[controllers] val heading: Message = "messages__establisher_individual_previous_address__title"
  private[controllers] val hint: Message = "messages__establisher_individual_previous_address_lede"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        EstablisherDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.fullName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          get(PreviousAddressId(index), PreviousAddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          post(PreviousAddressId(index), PreviousAddressListId(index), vm, mode, context(vm),
            PreviousPostCodeLookupId(index)
          )
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Establisher Individual Previous Address: $name"
      case _ => "Establisher Individual Previous Address"
    }
  }

}
