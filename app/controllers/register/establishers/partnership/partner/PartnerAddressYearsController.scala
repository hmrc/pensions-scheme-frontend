/*
 * Copyright 2024 HM Revenue & Customs
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
import identifiers.register.establishers.partnership.partner.{PartnerAddressYearsId, PartnerNameId}
import javax.inject.Inject
import models.requests.DataRequest
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.ExecutionContext
import models.SchemeReferenceNumber

class PartnerAddressYearsController @Inject()(
                                               val appConfig: FrontendAppConfig,
                                               val userAnswersService: UserAnswersService,
                                               val navigator: Navigator,
                                               override val messagesApi: MessagesApi,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               allowAccess: AllowAccessActionProvider,
                                               requireData: DataRequiredAction,
                                               val controllerComponents: MessagesControllerComponents,
                                               val view: addressYears
                                             )(implicit val ec: ExecutionContext) extends AddressYearsController with
  Retrievals {

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { partnerDetails =>
          get(PartnerAddressYearsId(establisherIndex, partnerIndex), form(partnerDetails.fullName),
            viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn))
        }
    }

  private def form(partnerName: String)(implicit request: DataRequest[AnyContent]) =
    new AddressYearsFormProvider()(Message("messages__partner_address_years__formError", partnerName))

  private def viewModel(mode: Mode, establisherIndex: Index, partnerIndex: Index, partnerName: String,
                        srn: Option[SchemeReferenceNumber]) = AddressYearsViewModel(
    postCall = routes.PartnerAddressYearsController.onSubmit(mode, establisherIndex, partnerIndex, srn),
    title = Message("messages__partner_address_years__title", Message("messages__common__address_years__partner")),
    heading = Message("messages__partner_address_years__heading", partnerName),
    legend = Message("messages__partner_address_years__heading", partnerName),
    subHeading = Some(Message(partnerName)),
    srn = srn
  )

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: Option[SchemeReferenceNumber]): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        PartnerNameId(establisherIndex, partnerIndex).retrieve.map { partnerDetails =>
          post(
            PartnerAddressYearsId(establisherIndex, partnerIndex),
            mode,
            form(partnerDetails.fullName),
            viewModel(mode, establisherIndex, partnerIndex, partnerDetails.fullName, srn)
          )
        }
    }
}
