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

package controllers.register.establishers.company

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyPostCodeLookupId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersCompany
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class CompanyPostCodeLookupController @Inject()(
                                                 override val appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 override val cacheConnector: DataCacheConnector,
                                                 override val addressLookupConnector: AddressLookupConnector,
                                                 @EstablishersCompany
                                                 override val navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: PostCodeLookupFormProvider
                                               ) extends PostcodeLookupController {

  private val title: Message = "messages__companyAddress__title"
  private val heading: Message = "messages__companyAddress__heading"

  protected val form: Form[String] = formProvider()

  private def viewmodel(index: Int, mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval {
      implicit request =>
        CompanyDetailsId(index).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.CompanyPostCodeLookupController.onSubmit(mode, index),
              routes.CompanyAddressController.onPageLoad(mode, index),
              title = Message(title),
              heading = Message(heading),
              subHeading = Some(details.companyName)
            )
        }
    }

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right map get
    }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(index, mode).retrieve.right.map {
          vm =>
            post(CompanyPostCodeLookupId(index), vm, mode)
        }
    }
}
