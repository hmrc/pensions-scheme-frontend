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

package controllers.register.establishers

import javax.inject.Inject

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.FrontendBaseController
import controllers.actions._
import forms.register.establishers.EstablisherKindFormProvider
import identifiers.register.establishers.EstablisherKindId
import models.register.establishers.EstablisherKind
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.establisherKind

import scala.concurrent.Future

class EstablisherKindController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: DataCacheConnector,
                                           navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EstablisherKindFormProvider
                                         ) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val redirectResult = request.userAnswers.get[EstablisherKind](EstablisherKindId(index)) match {
            case None => Ok(establisherKind(appConfig, form, mode, index,schemeName))
            case Some(value) => Ok(establisherKind(appConfig, form.fill(value), mode, index,schemeName))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName=>
          form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(establisherKind(appConfig, formWithErrors, mode, index,schemeName))),
          (value) =>
            dataCacheConnector.save(
              request.externalId,
              EstablisherKindId(index),
              value
            ).map {
              json =>
                Redirect(navigator.nextPage(EstablisherKindId(index), mode)(new UserAnswers(json)))
            }
        )
      }
  }

}
