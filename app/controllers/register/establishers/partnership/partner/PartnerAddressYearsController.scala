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
import controllers.Retrievals
import controllers.actions._
import controllers.address.AddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerAddressYearsId, PartnerDetailsId, PartnerNameId}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel

import scala.concurrent.ExecutionContext

class PartnerAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               val userAnswersService: UserAnswersService,
                                               val navigator: Navigator,
                                               val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction
                                             )(implicit val ec: ExecutionContext) extends AddressYearsController with Retrievals {

  private def form(partnerName: String) = new AddressYearsFormProvider()(Message("messages__partner_address_years__formError", partnerName))

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map { partnerDetails =>
          get(PartnerAddressYearsId(establisherIndex, partnerIndex), form(partnerDetails.fullName),
            viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn))
        }
    }

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, partnerName: String, srn: Option[String]) = AddressYearsViewModel(
    postCall = routes.PartnerAddressYearsController.onSubmit(mode, establisherIndex, partnerIndex, srn),
    title = Message("messages__partner_address_years__title", Message("messages__common__address_years__partner").resolve),
    heading = Message("messages__partner_address_years__heading", partnerName),
    legend = Message("messages__partner_address_years__heading", partnerName),
    subHeading = Some(Message(partnerName)),
    srn = srn
  )

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.right.map { partnerDetails =>
          post(
            PartnerAddressYearsId(establisherIndex, partnerIndex),
            mode,
            form(partnerDetails.fullName),
            viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn)
          )
        }
    }
}
