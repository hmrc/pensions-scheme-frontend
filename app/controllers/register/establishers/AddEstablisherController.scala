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

import config.FrontendAppConfig
import connectors.DataCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.register.establishers.AddEstablisherFormProvider
import identifiers.register.establishers.AddEstablisherId
import javax.inject.Inject
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.Navigator
import utils.annotations.Establishers
import views.html.register.establishers.addEstablisher

import scala.concurrent.Future

class AddEstablisherController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         dataCacheConnector: DataCacheConnector,
                                         @Establishers navigator: Navigator,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddEstablisherFormProvider) extends FrontendController with Retrievals with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val establishers = request.userAnswers.allEstablishers
          Future.successful(Ok(addEstablisher(appConfig, formProvider(establishers), mode,
            establishers, schemeName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      retrieveSchemeName {
        schemeName =>
          val establishers = request.userAnswers.allEstablishers
          formProvider(establishers).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(addEstablisher(appConfig, formWithErrors, mode,
                establishers, schemeName))),
            value =>
              Future.successful(Redirect(navigator.nextPage(AddEstablisherId(value), mode, request.userAnswers)))
          )
      }
  }
}
