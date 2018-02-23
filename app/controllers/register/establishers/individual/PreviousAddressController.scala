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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.FrontendBaseController
import controllers.actions._
import forms.register.establishers.individual.AddressFormProvider
import identifiers.register.establishers.individual.PreviousAddressId
import models.register.CountryOptions
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.annotations.EstablishersIndividual
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.individual.previousAddress

import scala.concurrent.Future

class PreviousAddressController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: DataCacheConnector,
                                           @EstablishersIndividual navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: AddressFormProvider,
                                           countryOptions: CountryOptions
                                         ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val result = request.userAnswers.get(PreviousAddressId(index)) match {
            case None => Ok(previousAddress(appConfig, form, mode, index, countryOptions.options, establisherName))
            case Some(value) => Ok(previousAddress(appConfig, form.fill(value), mode, index, countryOptions.options, establisherName))
          }
          Future.successful(result)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(previousAddress(appConfig, formWithErrors, mode, index, countryOptions.options, establisherName))),
            (value) =>
              dataCacheConnector.save(
                request.externalId,
                PreviousAddressId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(PreviousAddressId(index), mode)(new UserAnswers(json)))
              }
          )
      }
  }

}
