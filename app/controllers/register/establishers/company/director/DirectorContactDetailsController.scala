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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.company.director.{DirectorContactDetailsId, DirectorDetailsId}
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils._
import utils.annotations.EstablishersCompanyDirector
import viewmodels.{ContactDetailsViewModel, Message}

class DirectorContactDetailsController @Inject()(
                                                  @EstablishersCompanyDirector override val navigator: Navigator,
                                                  override val appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  override val cacheConnector: DataCacheConnector,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: ContactDetailsFormProvider
                                                ) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
        director =>
          get(DirectorContactDetailsId(establisherIndex, directorIndex), form, viewmodel(mode, establisherIndex, directorIndex, director.directorName))
      }
  }

  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map {
        director =>
          post(DirectorContactDetailsId(establisherIndex, directorIndex), mode, form, viewmodel(mode, establisherIndex, directorIndex, director.directorName))
      }
  }

  private def viewmodel(mode: Mode, establisherIndex: Index, directorIndex: Index, directorName: String) = ContactDetailsViewModel(
    postCall = routes.DirectorContactDetailsController.onSubmit(mode, establisherIndex, directorIndex),
    title = Message("messages__company_director_contact__title"),
    heading = Message("messages__company_director_contact__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(directorName)
  )
}
