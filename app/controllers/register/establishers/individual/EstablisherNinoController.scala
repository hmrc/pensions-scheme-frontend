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
import forms.register.establishers.individual.EstablisherNinoFormProvider
import identifiers.register.establishers.individual.EstablisherNinoId
import models.requests.DataRequest
import models.{EstablisherNino, Index, Mode}
import play.api.mvc.{Action, AnyContent, Result}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.establisherNino

import scala.concurrent.Future
import scala.util.{Failure, Success}

class EstablisherNinoController @Inject()(
                                        appConfig: FrontendAppConfig,
                                        override val messagesApi: MessagesApi,
                                        dataCacheConnector: DataCacheConnector,
                                        navigator: Navigator,
                                        authenticate: AuthAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EstablisherNinoFormProvider) extends FrontendController
  with I18nSupport with Enumerable.Implicits with MapFormats{

  val form: Form[EstablisherNino] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName {
        establisherName =>
          val redirectResult = request.userAnswers.establisherNino(index) match {
            case Success(None) => Ok(establisherNino(appConfig, form, mode, index, establisherName))
            case Success(Some(value)) => Ok(establisherNino(appConfig, form.fill(value), mode, index, establisherName))
            case Failure(_) => Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          }
          Future.successful(redirectResult)
      }(index)
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(establisherNino(appConfig, formWithErrors, mode, index, establisherName))),
            (value) =>
              dataCacheConnector.saveMap[EstablisherNino](request.externalId,
                EstablisherNinoId.toString, index, value).map(cacheMap =>
                Redirect(navigator.nextPage(EstablisherNinoId, mode)(new UserAnswers(cacheMap))))
          )
      }(index)
  }

  private def retrieveEstablisherName(block: String => Future[Result])(index:Int)
                                     (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.establisherDetails(index) match {
      case Success(Some(value)) => block(value.establisherName)
      case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
