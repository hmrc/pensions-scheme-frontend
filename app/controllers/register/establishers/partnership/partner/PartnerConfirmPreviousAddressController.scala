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
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.establishers.partnership.partner._
import models.{Index, Mode, OptionalSchemeReferenceNumber}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PartnerConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
                                                        override val messagesApi: MessagesApi,
                                                        val userAnswersService: UserAnswersService,
                                                        val navigator: Navigator,
                                                        authenticate: AuthAction,
                                                        allowAccess: AllowAccessActionProvider,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        val countryOptions: CountryOptions,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        val view: confirmPreviousAddress
                                                       )(implicit val ec: ExecutionContext) extends
  ConfirmPreviousAddressController with Retrievals with I18nSupport {

  private[controllers] val postCall = routes.PartnerConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  def onPageLoad(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, partnerIndex, srn).retrieve.map { vm =>
          get(PartnerConfirmPreviousAddressId(establisherIndex, partnerIndex), vm)
        }
    }

  private def viewmodel(mode: Mode, establisherIndex: Int, partnerIndex: Int, srn: OptionalSchemeReferenceNumber) =
    Retrieval(
      implicit request =>
        (PartnerNameId(establisherIndex, partnerIndex) and ExistingCurrentAddressId(establisherIndex, partnerIndex))
          .retrieve.map {
          case details ~ address =>
            ConfirmAddressViewModel(
              postCall(establisherIndex, partnerIndex, srn),
              title = Message(title),
              heading = Message(heading, details.fullName),
              hint = None,
              address = address,
              name = details.fullName,
              srn = srn
            )
        }
    )

  def onSubmit(mode: Mode, establisherIndex: Index, partnerIndex: Index, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        viewmodel(mode, establisherIndex, partnerIndex, srn).retrieve.map { vm =>
          post(PartnerConfirmPreviousAddressId(establisherIndex, partnerIndex), PartnerPreviousAddressId
          (establisherIndex, partnerIndex), vm, mode)
        }
    }


}
