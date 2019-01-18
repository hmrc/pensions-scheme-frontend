/*
 * Copyright 2019 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.EstablisherKindFormProvider
import identifiers.register.establishers.EstablisherKindId
import javax.inject.Inject
import models.{Index, Mode}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Establishers
import utils.{Enumerable, IDataFromRequest, Navigator, UserAnswers}
import views.html.register.establishers.establisherKind

import scala.concurrent.{ExecutionContext, Future}

class EstablisherKindController @Inject()(
                                           appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           dataCacheConnector: UserAnswersCacheConnector,
                                           @Establishers navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: EstablisherKindFormProvider
                                         )(implicit val ec: ExecutionContext) extends FrontendController with Retrievals with IDataFromRequest with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      val formWithData = request.userAnswers.get(EstablisherKindId(index)).fold(form)(form.fill)
      Future.successful(Ok(establisherKind(appConfig, formWithData, mode, index, existingSchemeName)))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(establisherKind(appConfig, formWithErrors, mode, index, existingSchemeName))),
        value =>
          dataCacheConnector.save(
            request.externalId,
            EstablisherKindId(index),
            value
          ).map {
            json =>
              Redirect(navigator.nextPage(EstablisherKindId(index), mode, UserAnswers(json)))
          }
      )
  }

}
