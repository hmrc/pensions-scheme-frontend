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

import javax.inject.Inject

import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import connectors.DataCacheConnector
import controllers.actions._
import config.FrontendAppConfig
import controllers.Retrievals
import forms.register.establishers.company.director.DirectorNinoFormProvider
import identifiers.register.establishers.company.director.DirectorNinoId
import models.register.establishers.company.director.DirectorNino
import models.{Index, Mode}
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.director.directorNino

import scala.concurrent.Future

class DirectorNinoController @Inject()(
                                               appConfig: FrontendAppConfig,
                                               override val messagesApi: MessagesApi,
                                               dataCacheConnector: DataCacheConnector,
                                               navigator: Navigator,
                                               authenticate: AuthAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: DirectorNinoFormProvider
                                             ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[DirectorNino] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(establisherIndex,directorIndex) { directorName =>
        val redirectResult = request.userAnswers.get(DirectorNinoId(establisherIndex, directorIndex)) match {
          case None =>
            Ok(directorNino(appConfig, form, mode, establisherIndex, directorIndex,directorName))
          case Some(value) =>
            Ok(directorNino(appConfig, form.fill(value), mode, establisherIndex, directorIndex,directorName))
        }
        Future.successful(redirectResult)
      }
  }


  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveDirectorName(establisherIndex,directorIndex) { directorName =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorNino(appConfig, formWithErrors, mode, establisherIndex, directorIndex,directorName))),
          (value) =>
            dataCacheConnector.save(
              request.externalId,
              DirectorNinoId(establisherIndex, directorIndex),
              value
            ).map {
              json =>
                Redirect(navigator.nextPage(DirectorNinoId(establisherIndex, directorIndex), mode)(new UserAnswers(json)))
            }
        )
      }
  }

}
