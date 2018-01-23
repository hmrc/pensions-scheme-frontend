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
import models.{EstablisherDetails, Index, Mode}
import models.requests.DataRequest
import play.api.mvc.{Action, AnyContent}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual.establisherDetails
import play.api.mvc.Result

import scala.concurrent.Future
import scala.util.{Failure, Success}
import play.api.libs.json._

class EstablisherDetailsController @Inject()(appConfig: FrontendAppConfig,
                                             override val messagesApi: MessagesApi,
                                             dataCacheConnector: DataCacheConnector,
                                             navigator: Navigator,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: EstablisherDetailsFormProvider) extends FrontendController
  with I18nSupport with Enumerable.Implicits with MapFormats {

  private def key(index: Int) = __ \ "establishers" \ index \ EstablisherDetailsId

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val redirectResult = request.userAnswers.establisherDetails(index) match {
            case None =>
              Ok(establisherDetails(appConfig, form, mode, index, schemeName))
            case Some(value) =>
              Ok(establisherDetails(appConfig, form.fill(value), mode, index, schemeName))
          }
        Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(establisherDetails(appConfig, formWithErrors, mode, index,
                schemeName))),
            (value) =>
              dataCacheConnector.save[EstablisherDetails](request.externalId,
                key(index), value).map(cacheMap =>
                Redirect(navigator.nextPage(EstablisherDetailsId, mode)(new UserAnswers(cacheMap))))
          )
      }
  }

  private def retrieveSchemeName(block: String => Future[Result])
                           (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.schemeDetails.map { schemeDetails =>
      block(schemeDetails.schemeName)
    }.getOrElse(Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad())))
  }
}
