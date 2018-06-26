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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.actions._
import forms.ContactDetailsFormProvider
import identifiers.register.establishers.individual.ContactDetailsId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import utils.Navigator
import utils.annotations.EstablishersIndividual
import viewmodels.{ContactDetailsViewModel, Message}

class ContactDetailsController @Inject()(
                                          @EstablishersIndividual override val navigator: Navigator,
                                          override val appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          override val cacheConnector: DataCacheConnector,
                                          authenticate: AuthAction,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: ContactDetailsFormProvider
                                        ) extends controllers.ContactDetailsController {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          get(ContactDetailsId(index), form, viewmodel(mode, index, establisherName))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          post(ContactDetailsId(index), mode, form, viewmodel(mode, index, establisherName))
      }
  }

  private def viewmodel(mode: Mode, index: Index, establisherName: String) = ContactDetailsViewModel(
    postCall = routes.ContactDetailsController.onSubmit(mode, index),
    title = Message("messages__establisher_individual_contact_details__title"),
    heading = Message("messages__establisher_individual_contact_details__heading"),
    body = Message("messages__contact_details__body"),
    subHeading = Some(establisherName)
  )
}
