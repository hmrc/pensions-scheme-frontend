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

package controllers

import audit.AuditService
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import identifiers._
import identifiers.register.SchemeNameId
import javax.inject.Inject
import models.Mode
import models.address.Address
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import utils.annotations.Register
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class InsurerConfirmAddressController @Inject()(val appConfig: FrontendAppConfig,
                                         val messagesApi: MessagesApi,
                                         val dataCacheConnector: UserAnswersCacheConnector,
                                         @Register val navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         val formProvider: AddressFormProvider,
                                         val countryOptions: CountryOptions,
                                         val auditService: AuditService
                                        ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = routes.InsurerConfirmAddressController.onSubmit _
  private[controllers] val title: Message = "messages__insurer_confirm_address__title"
  private[controllers] val heading: Message = "messages__insurer_confirm_address__h1"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        SchemeNameId.retrieve.right.map {
          schemeName =>
            ManualAddressViewModel(
              postCall(mode),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading, schemeName),
              secondaryHeader = None
            )
        }
    }

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map {
        vm =>
          get(InsurerConfirmAddressId, InsurerSelectAddressId, vm)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode).retrieve.right.map {
        vm =>
          post(InsurerConfirmAddressId, InsurerSelectAddressId, vm, mode, "Insurer Address", InsurerEnterPostCodeId)
      }
  }

}
