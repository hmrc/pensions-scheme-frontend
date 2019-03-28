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
import connectors.{AddressLookupConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerAddressPostcodeLookupId, PartnerDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersPartner
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

import scala.concurrent.ExecutionContext

class PartnerAddressPostcodeLookupController @Inject()(
                                                        override val appConfig: FrontendAppConfig,
                                                        override val messagesApi: MessagesApi,
                                                        override val cacheConnector: UserAnswersCacheConnector,
                                                        override val addressLookupConnector: AddressLookupConnector,
                                                        @EstablishersPartner override val navigator: Navigator,
                                                        authenticate: AuthAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        formProvider: PostCodeLookupFormProvider
                                                      ) extends PostcodeLookupController {

  protected val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, partnerIndex, mode, srn).retrieve.right map get
    }

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, partnerIndex, mode, srn).retrieve.right.map(
          vm =>
            post(PartnerAddressPostcodeLookupId(establisherIndex, partnerIndex), vm, mode)
        )
    }

  private def viewmodel(establisherIndex: Index, partnerIndex: Index, mode: Mode, srn: Option[String]): Retrieval[PostcodeLookupViewModel] =
    Retrieval(
      implicit request =>
        PartnerDetailsId(establisherIndex, partnerIndex).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.PartnerAddressPostcodeLookupController.onSubmit(mode, establisherIndex, partnerIndex, srn),
              routes.PartnerAddressController.onPageLoad(mode, establisherIndex, partnerIndex, srn),
              Message("messages__partnerAddressPostcodeLookup__title"),
              Message("messages__partnerAddressPostcodeLookup__heading"),
              Some(details.fullName),
              Some(Message("messages__partnerAddressPostcodeLookup__lede"))
            )
        }
    )

}
