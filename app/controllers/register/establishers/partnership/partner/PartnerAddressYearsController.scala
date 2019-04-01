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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerAddressYearsId, PartnerDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersPartner
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class PartnerAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               val cacheConnector: UserAnswersCacheConnector,
                                               @EstablishersPartner val navigator: Navigator,
                                               val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction
                                             ) extends AddressYearsController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__common_error__current_address_years"))

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map { partnerDetails =>
        get(PartnerAddressYearsId(establisherIndex, partnerIndex), form,
          viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn))
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
    implicit request =>
      PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map { partnerDetails =>
        post(
          PartnerAddressYearsId(establisherIndex, partnerIndex),
          mode,
          form,
          viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn)
        )
      }
  }

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, partnerName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = routes.PartnerAddressYearsController.onSubmit(mode, establisherIndex, partnerIndex, srn),
    title = Message("messages__partner_address_years__title"),
    heading = Message("messages__partner_address_years__heading"),
    legend = Message("messages__partner_address_years__heading"),
    subHeading = Some(Message(partnerName))
  )
}
