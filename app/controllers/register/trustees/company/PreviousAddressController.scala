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

package controllers.register.trustees.company

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import controllers.address.ManualAddressController
import controllers.register.trustees.company.routes._
import forms.address.AddressFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import models.address.Address
import models.register.CountryOptions
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class PreviousAddressController @Inject() (
                                        val appConfig: FrontendAppConfig,
                                        val messagesApi: MessagesApi,
                                        val dataCacheConnector: DataCacheConnector,
                                        val navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        val formProvider: AddressFormProvider,
                                        val countryOptions: CountryOptions
                                      ) extends ManualAddressController with I18nSupport with Retrievals {

  private val title: Message = "messages__companyAddress__title"
  private val heading: Message = "messages__companyAddress__heading"

  protected val form: Form[Address] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[ManualAddressViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            ManualAddressViewModel(
              PreviousAddressController.onSubmit(mode, Index(index)),
              countryOptions.options,
              title = Message(title),
              heading = Message(heading),
              secondaryHeader = Some(details.companyName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(index, mode).retrieve.right map get
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      ???
  }
}
