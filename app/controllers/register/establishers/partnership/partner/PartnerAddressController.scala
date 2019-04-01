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
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.establishers.partnership.partner.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerAddressId, PartnerAddressListId, PartnerAddressPostcodeLookupId, PartnerDetailsId}
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

class PartnerAddressController @Inject()(
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
                                        ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = PartnerAddressController.onSubmit _
  private[controllers] val title: Message = "messages__partnerAddress__title"
  private[controllers] val heading: Message = "messages__partnerAddress__heading"
  private[controllers] val hint: Message = "messages__partnerAddress__lede"

  protected val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, partnerIndex, mode, srn).retrieve.right.map {
        vm =>
          get(PartnerAddressId(establisherIndex, partnerIndex), PartnerAddressListId(establisherIndex, partnerIndex), vm)
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      viewmodel(establisherIndex, partnerIndex, mode, srn).retrieve.right.map {
        vm =>
          post(
            PartnerAddressId(establisherIndex, partnerIndex),
            PartnerAddressListId(establisherIndex, partnerIndex),
            vm,
            mode,
            context(vm),
            PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex)
          )
      }
  }

  private def viewmodel(establisherIndex: Int, partnerIndex: Int, mode: Mode, srn: Option[String]): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              postCall(mode, Index(establisherIndex), Index(partnerIndex), srn),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              hint = Some(Message(hint)),
              secondaryHeader = Some(details.fullName)
            )
        }
    }

  private def context(viewModel: ManualAddressViewModel): String = {
    viewModel.secondaryHeader match {
      case Some(name) => s"Company Partner Address: $name"
      case _ => "Company Partner Address"
    }
  }

}
