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
import identifiers.register.establishers.company.director.{DirectorAddressPostcodeLookupId, DirectorDetailsId}
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel

class DirectorAddressPostcodeLookupController @Inject() (
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

  private def viewmodel(establisherIndex: Index, directorIndex: Index, mode: Mode): Retrieval[PostcodeLookupViewModel] =
    Retrieval(
      implicit request =>
        DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
          details =>
            PostcodeLookupViewModel(
              routes.DirectorAddressPostcodeLookupController.onSubmit(mode, establisherIndex, directorIndex),
              routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, establisherIndex, directorIndex),
              Message("messages__directorAddressPostcodeLookup__title"),
              Message("messages__directorAddressPostcodeLookup__heading"),
              Some(details.directorName)
            )
        }
    )

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, directorIndex, mode).retrieve.right map get
    }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        viewmodel(establisherIndex, directorIndex, mode).retrieve.right.map(
          vm =>
            post(DirectorAddressPostcodeLookupId(establisherIndex, directorIndex), vm, invalidPostcode, noResults, mode)
        )
    }
}
