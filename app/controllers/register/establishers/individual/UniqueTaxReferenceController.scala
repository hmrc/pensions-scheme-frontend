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
import forms.register.establishers.individual.UniqueTaxReferenceFormProvider
import identifiers.register.establishers.individual.UniqueTaxReferenceId
import models.requests.DataRequest
import models.{Index, Mode, UniqueTaxReference}
import play.api.mvc.{Action, AnyContent, Result}
import utils.{Enumerable, MapFormats, Navigator, UserAnswers}
import views.html.register.establishers.individual._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import play.api.libs.json._

class UniqueTaxReferenceController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: UniqueTaxReferenceFormProvider) extends FrontendController with I18nSupport
  with Enumerable.Implicits with MapFormats {

  private def key(index: Int) = __ \ "establishers" \ index \ UniqueTaxReferenceId

  private val form: Form[UniqueTaxReference] = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          val redirectResult = request.userAnswers.uniqueTaxReference(index) match {
            case None =>
              Ok(uniqueTaxReference(appConfig, form, mode, index, establisherName))
            case Some(value) =>
              Ok(uniqueTaxReference(appConfig, form.fill(value), mode, index, establisherName))
          }
          Future.successful(redirectResult)
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveEstablisherName(index) {
        establisherName =>
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(uniqueTaxReference(appConfig, formWithErrors, mode, index, establisherName))),
            (value) =>
              dataCacheConnector.save[UniqueTaxReference](request.externalId,
                key(index), value).map(cacheMap =>
                Redirect(navigator.nextPage(UniqueTaxReferenceId, mode)(new UserAnswers(cacheMap))))
          )
      }
  }

  private def retrieveEstablisherName(index:Int)(block: String => Future[Result])
                                (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.establisherDetails(index) match {
      case Some(value) =>
        block(value.establisherName)
      case _ =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
