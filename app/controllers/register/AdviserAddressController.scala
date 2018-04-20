/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import controllers.address.ManualAddressController
import forms.address.AddressFormProvider
import controllers.register.routes._
import identifiers.register.AdviserAddressId
import models.address.Address
import models.Mode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.{CountryOptions, Navigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class AdviserAddressController @Inject()(
                                          val appConfig: FrontendAppConfig,
                                          val messagesApi: MessagesApi,
                                          val dataCacheConnector: DataCacheConnector,
                                          val navigator: Navigator,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          val formProvider: AddressFormProvider,
                                          val countryOptions: CountryOptions
                                        ) extends ManualAddressController with I18nSupport {

  private[controllers] val postCall = AdviserAddressController.onSubmit _
  private[controllers] val title: Message = "messages__adviserAddress__title"
  private[controllers] val heading: Message = "messages__adviserAddress__heading"
  private[controllers] val secondary: Message = "messages__adviserAddress__secondary"
  private[controllers] val hint = None

  protected val form: Form[Address] = formProvider()

  private def viewmodel(mode: Mode): ManualAddressViewModel =
        ManualAddressViewModel(
          postCall(mode),
          countryOptions.options,
          title = Message(title),
          heading = Message(heading),
          hint = None,
          secondaryHeader = Some(secondary)
        )


  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>

          get(AdviserAddressId, viewmodel(mode))
      }


  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
          post(AdviserAddressId, viewmodel(mode), mode)
      }
  }

