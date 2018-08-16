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

import config.FrontendAppConfig
import connectors.{DataCacheConnector, PSANameCacheConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.NeedContactFormProvider
import identifiers.PsaEmailId
import javax.inject.Inject
import models.NormalMode
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Register
import views.html.register.needContact

import scala.concurrent.Future

class NeedContactController @Inject()(
                                       appConfig: FrontendAppConfig,
                                       override val messagesApi: MessagesApi,
                                       dataCacheConnector: DataCacheConnector,
                                       authenticate: AuthAction,
                                       formProvider: NeedContactFormProvider,
                                       psaNameCacheConnector: PSANameCacheConnector
                                     ) extends FrontendController with I18nSupport with Retrievals {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
      Ok(needContact(appConfig, form))
  }

  def onSubmit: Action[AnyContent] = authenticate.async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(needContact(appConfig, formWithErrors))),
        value =>
          psaNameCacheConnector.fetch(request.externalId).flatMap {
            case Some(_) =>
              psaNameCacheConnector.save(request.externalId, PsaEmailId, value).map { _ =>
                Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode))
              }
            case _ =>
              Future.successful(Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)))

          }
      )
  }
}
