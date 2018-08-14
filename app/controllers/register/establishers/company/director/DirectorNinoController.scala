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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.company.director.DirectorNinoFormProvider
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorNinoId}
import javax.inject.Inject
import models.{Index, Mode, Nino}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.EstablishersCompanyDirector
import utils.{Enumerable, Navigator, UserAnswers}
import views.html.register.establishers.company.director.directorNino

import scala.concurrent.Future

class DirectorNinoController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        @EstablishersCompanyDirector navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: DirectorNinoFormProvider
                                      ) extends FrontendController with Retrievals with I18nSupport with Enumerable.Implicits {

  private val form: Form[Nino] = formProvider()

  def onPageLoad(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.flatMap { director =>
        DirectorNinoId(establisherIndex, directorIndex).retrieve.right.map { value =>
          Future.successful(Ok(directorNino(appConfig, form.fill(value), mode, establisherIndex, directorIndex, director.fullName)))
        }.left.map { _ =>
          Future.successful(Ok(directorNino(appConfig, form, mode, establisherIndex, directorIndex, director.fullName)))
        }
      }
  }


  def onSubmit(mode: Mode, establisherIndex: Index, directorIndex: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      DirectorDetailsId(establisherIndex, directorIndex).retrieve.right.map { director =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(directorNino(appConfig, formWithErrors, mode, establisherIndex, directorIndex, director.fullName))),
          (value) =>
            dataCacheConnector.save(
              request.externalId,
              DirectorNinoId(establisherIndex, directorIndex),
              value
            ) map { json =>
              Redirect(navigator.nextPage(DirectorNinoId(establisherIndex, directorIndex), mode, new UserAnswers(json)))
            }
        )
      }
  }

}
