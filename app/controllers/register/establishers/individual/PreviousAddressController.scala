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

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import forms.register.establishers.individual.PreviousAddressFormProvider
import identifiers.register.establishers.individual.PreviousAddressId
import models.Mode
import utils.{Navigator, UserAnswers}
import views.html.register.establishers.individual.previousAddress

import scala.concurrent.Future

class PreviousAddressController @Inject() (
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PreviousAddressFormProvider
                                      ) extends FrontendController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode) = (authenticate andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(PreviousAddressId) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(previousAddress(appConfig, preparedForm, mode))
  }

  def onSubmit(mode: Mode) = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(previousAddress(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.save[String](request.externalId, PreviousAddressId, value).map(cacheMap =>
            Redirect(navigator.nextPage(PreviousAddressId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
