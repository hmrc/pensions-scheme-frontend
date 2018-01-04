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
import forms.register.establishers.individual.EstablisherDetailsFormProvider
import identifiers.register.establishers.individual.EstablisherDetailsId
import models.Mode
import models.EstablisherDetails
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.establisherDetails

import scala.concurrent.Future
import scala.util.{Failure, Success}

class EstablisherDetailsController @Inject()(appConfig: FrontendAppConfig,
                                                  override val messagesApi: MessagesApi,
                                                  dataCacheConnector: DataCacheConnector,
                                                  navigator: Navigator,
                                                  authenticate: AuthAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: EstablisherDetailsFormProvider) extends FrontendController
                                                  with I18nSupport with Enumerable.Implicits with MapFormats {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.establisherDetails(index) match {
        case Success(None) => Ok(establisherDetails(appConfig, form, mode))
        case Success(Some(value)) => Ok(establisherDetails(appConfig, form.fill(value), mode))
        case Failure(_) => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(establisherDetails(appConfig, formWithErrors, mode))),
        (value) =>
          dataCacheConnector.saveMap[EstablisherDetails](request.externalId,
            EstablisherDetailsId.toString, index, value).map(cacheMap =>
            Redirect(navigator.nextPage(EstablisherDetailsId, mode)(new UserAnswers(cacheMap))))
      )
  }
}
