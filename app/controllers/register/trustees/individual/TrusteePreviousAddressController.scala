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

package controllers.register.trustees.individual

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.trustees.individual.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.individual.{IndividualPreviousAddressPostCodeLookupId, TrusteeDetailsId, TrusteePreviousAddressId, TrusteePreviousAddressListId}
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.TrusteesIndividual
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class TrusteePreviousAddressController @Inject()(
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                                  @TrusteesIndividual override val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = TrusteePreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__trustee_individual_previous_address__title"
  private[controllers] val heading: Message = "messages__trustee_individual_previous_address__heading"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        TrusteeDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(index)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              secondaryHeader = Some(details.fullName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          get(TrusteePreviousAddressId(index), TrusteePreviousAddressListId(index), vm)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right.map {
        vm =>
          post(TrusteePreviousAddressId(index), TrusteePreviousAddressListId(index), vm, mode, context(vm),
            IndividualPreviousAddressPostCodeLookupId(index)
          )
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(fullName) => s"Trustee Individual Previous Address: $fullName"
      case _ => "Trustee Individual Previous Address"
    }
  }

}
