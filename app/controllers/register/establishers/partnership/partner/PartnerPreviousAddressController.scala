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

package controllers.register.establishers.partnership.partner

import audit.AuditService
import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers.register.establishers.partnership.partner._
import javax.inject.Inject
import models.address.Address
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersPartner
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PartnerPreviousAddressController @Inject()(
                                                  val appConfig: FrontendAppConfig,
                                                  val messagesApi: MessagesApi,
                                                  val userAnswersService: UserAnswersService,
                                                  @EstablishersPartner val navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val formProvider: AddressFormProvider,
                                                  val countryOptions: CountryOptions,
                                                  val auditService: AuditService
                                                ) extends ManualAddressController with I18nSupport with Retrievals {

  private[controllers] val postCall = routes.PartnerPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__partnerPreviousAddress__title"
  private[controllers] val heading: Message = "messages__partnerPreviousAddress__heading"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
          partner =>
            ManualAddressViewModel(
              postCall(mode, establisherIndex, partnerIndex),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              secondaryHeader = Some(partner.fullName)
            )
        }
    }

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index).retrieve.right.map {
        vm =>
          get(PartnerPreviousAddressId(establisherIndex, partnerIndex), PartnerPreviousAddressListId(establisherIndex, partnerIndex), vm)
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode: Mode, establisherIndex: Index, partnerIndex: Index).retrieve.right.map {
        vm =>
          post(
            PartnerPreviousAddressId(establisherIndex, partnerIndex),
            PartnerPreviousAddressListId(establisherIndex, partnerIndex),
            vm,
            mode,
            context(vm),
            PartnerPreviousAddressPostcodeLookupId(establisherIndex, partnerIndex)
          )
      }
  }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Partnership Partner Previous Address: $name"
      case _ => "Partnership Partner Previous Address"
    }
  }

}
