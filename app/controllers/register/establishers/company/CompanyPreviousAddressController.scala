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

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.individual.AddressFormProvider
import identifiers.register.establishers.company.CompanyPreviousAddressId
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{CountryOptions, Navigator, UserAnswers}
import views.html.register.establishers.company.companyPreviousAddress

import scala.concurrent.Future

class CompanyPreviousAddressController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: AddressFormProvider,
                                        countryOptions: CountryOptions
                                      ) extends FrontendController with Retrievals with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) { companyName =>
        val preparedForm = request.userAnswers.get(CompanyPreviousAddressId(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        Future.successful(Ok(companyPreviousAddress(appConfig, preparedForm, mode, index, companyName, countryOptions.options)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveCompanyName(index) { companyName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(companyPreviousAddress(appConfig, formWithErrors, mode, index, companyName, countryOptions.options))),
          (value) =>
            dataCacheConnector.save(request.externalId, CompanyPreviousAddressId(index), value).map(cacheMap =>
              Redirect(navigator.nextPage(CompanyPreviousAddressId(index), mode)(new UserAnswers(cacheMap))))
        )
      }
  }
}
