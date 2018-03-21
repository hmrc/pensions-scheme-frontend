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
import controllers.Retrievals
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.individual.AddressId
import models.address.Address
import models.register.CountryOptions
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersIndividual
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.individual.address

import scala.concurrent.Future

class AddressController @Inject() (
                                    appConfig: FrontendAppConfig,
                                    override val messagesApi: MessagesApi,
                                    dataCacheConnector: DataCacheConnector,
                                    @EstablishersIndividual navigator: Navigator,
                                    authenticate: AuthAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    formProvider: AddressFormProvider,
                                    countryOptions: CountryOptions
                                  ) extends FrontendController with Retrievals with I18nSupport {

  val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val result = request.userAnswers.get(AddressId(index)) match {
            case None => Ok(address(appConfig, form, mode, index, countryOptions.options, establisherName))
            case Some(value) => Ok(address(appConfig, form.fill(value), mode, index, countryOptions.options, establisherName))
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
              Future.successful(BadRequest(address(appConfig, formWithErrors, mode, index, countryOptions.options, establisherName))),
            (value) =>
              dataCacheConnector.save(
                request.externalId,
                AddressId(index),
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(AddressId(index), mode)(new UserAnswers(json)))
              }
          )
      }
  }

}
