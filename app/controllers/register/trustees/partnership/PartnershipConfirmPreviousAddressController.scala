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

package controllers.register.trustees.partnership

import config.FrontendAppConfig
import controllers.Retrievals
import controllers.actions._
import controllers.address.ConfirmPreviousAddressController
import identifiers.register.trustees.ExistingCurrentAddressId
import identifiers.register.trustees.partnership._
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import utils.CountryOptions
import viewmodels.Message
import viewmodels.address.ConfirmAddressViewModel
import views.html.address.confirmPreviousAddress

import scala.concurrent.ExecutionContext

class PartnershipConfirmPreviousAddressController @Inject()(val appConfig: FrontendAppConfig,
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
  ConfirmPreviousAddressController with I18nSupport with Retrievals {

  private[controllers] val postCall = routes.PartnershipConfirmPreviousAddressController.onSubmit _
  private[controllers] val title: Message = "messages__confirmPreviousAddress__title"
  private[controllers] val heading: Message = "messages__confirmPreviousAddress__heading"

  def onPageLoad(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(srn, index).retrieve.map { vm =>
        get(PartnershipConfirmPreviousAddressId(index), vm)
      }
  }

  private def viewmodel(srn: SchemeReferenceNumber, index: Int) =
    Retrieval(
      implicit request =>
        (PartnershipDetailsId(index) and ExistingCurrentAddressId(index)).retrieve.map {
          case details ~ address =>
            ConfirmAddressViewModel(
              postCall(index, srn),
              Message(title),
              Message(heading, details.name),
              None,
              address,
              details.name,
              srn
            )
        }
    )

  def onSubmit(mode: Mode, index: Index, srn: SchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData
  (mode, srn) andThen requireData).async {
    implicit request =>
      viewmodel(srn, index).retrieve.map { vm =>
        post(PartnershipConfirmPreviousAddressId(index), PartnershipPreviousAddressId(index), vm, mode)
      }
  }
}

