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

package controllers.register.establishers.partnership

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.partnership.{PartnershipAddressYearsId, PartnershipDetailsId}
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablisherPartnership
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

class PartnershipAddressYearsController @Inject()(
                                                   override val appConfig: FrontendAppConfig,
                                                   override val cacheConnector: UserAnswersCacheConnector,
                                                   @EstablisherPartnership override val navigator: Navigator,
                                                   override val messagesApi: MessagesApi,
                                                   authenticate: AuthAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction
                                                 ) extends AddressYearsController with Retrievals {

  private val form = new AddressYearsFormProvider()(Message("messages__partnershipAddressYears__error"))

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
        get(PartnershipAddressYearsId(index), form, viewModel(mode, index, partnershipDetails.name, srn))
      }
  }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData() andThen requireData).async {
    implicit request =>
      PartnershipDetailsId(index).retrieve.right.map { partnershipDetails =>
        post(PartnershipAddressYearsId(index), mode, form, viewModel(mode, index, partnershipDetails.name, srn))
      }
  }

  private def viewModel(mode: Mode, index: Index, partnershipName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = routes.PartnershipAddressYearsController.onSubmit(mode, index, srn),
    title = Message("messages__partnershipAddressYears__title"),
    heading = Message("messages__partnershipAddressYears__heading"),
    legend = Message("messages__partnershipAddressYears__heading"),
    subHeading = Some(Message(partnershipName))
  )

}
