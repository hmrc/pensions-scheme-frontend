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

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import forms.register.DeclarationDutiesFormProvider
import identifiers.register.DeclarationDutiesId
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.declarationDuties
import models.{Mode, NormalMode}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

class DeclarationDutiesController @Inject()(
                                             appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             dataCacheConnector: DataCacheConnector,
                                             navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: DeclarationDutiesFormProvider
                                           ) extends FrontendController with I18nSupport with Retrievals with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val result = request.userAnswers.get(DeclarationDutiesId) match {
            case None => Ok(declarationDuties(appConfig, form, schemeName))
            case Some(value) => Ok(declarationDuties(appConfig, form.fill(value), schemeName))
          }
          Future.successful(result)
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(declarationDuties(appConfig, formWithErrors, schemeName))),
            (value) =>
              dataCacheConnector.save(request.externalId, DeclarationDutiesId, value).map(cacheMap =>
                Redirect(navigator.nextPage(DeclarationDutiesId, NormalMode)(UserAnswers(cacheMap))))
          )
      }
  }
}
