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
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.individual.AddressFormProvider
import identifiers.register._
import models.{Address, Mode}
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Register
import utils.{CountryOptions, Navigator, UserAnswers}
import views.html.register.insurerAddress

import scala.concurrent.Future

class InsurerAddressController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         @Register navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddressFormProvider,
                                         countryOptions: CountryOptions) extends FrontendController with Retrievals with I18nSupport {

  val form: Form[Address] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val result = request.userAnswers.get(InsurerAddressId) match {
            case None => Ok(insurerAddress(appConfig, form, mode, countryOptions.options, schemeName))
            case Some(value) => Ok(insurerAddress(appConfig, form.fill(value), mode, countryOptions.options, schemeName))
          }
          Future.successful(result)
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(insurerAddress(appConfig, formWithErrors, mode, countryOptions.options, schemeName))),
            (value) =>
              dataCacheConnector.save(
                request.externalId,
                InsurerAddressId,
                value
              ).map {
                json =>
                  Redirect(navigator.nextPage(InsurerAddressId, mode)(new UserAnswers(json)))
              }
          )
      }
  }

}
