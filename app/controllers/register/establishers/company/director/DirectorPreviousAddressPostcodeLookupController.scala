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

package controllers.register.establishers.company.director

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, DataCacheConnector}
import controllers.actions._
import controllers.address.PostcodeLookupController
import forms.address.PostCodeLookupFormProvider
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorPreviousAddressPostcodeLookupId}
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class DirectorPreviousAddressPostcodeLookupController @Inject() (
                                        override val appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        override val cacheConnector: DataCacheConnector,
                                        override val addressLookupConnector: AddressLookupConnector,
                                        override val navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PostCodeLookupFormProvider
                                      ) extends PostcodeLookupController {

  protected val form: Form[String] = formProvider()

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index) = Retrieval {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map(
        details => PostcodeLookupViewModel(
          routes.DirectorPreviousAddressPostcodeLookupController.onSubmit(mode, establisherIndex, directorIndex),
          routes.DirectorPreviousAddressController.onPageLoad(mode, establisherIndex, directorIndex),
          Message("messages__directorPreviousAddressPostcodeLookup__title"),
          Message("messages__directorPreviousAddressPostcodeLookup__heading"),
          Some(details.directorName)
        )
      )
  }

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode,establisherIndex, directorIndex).retrieve.right.map(
          vm =>
            get(vm)
      )
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      viewmodel(mode, establisherIndex, directorIndex).retrieve.right.map(
        vm =>
          post(DirectorPreviousAddressPostcodeLookupId(establisherIndex, directorIndex), vm, mode)
      )
  }
}
