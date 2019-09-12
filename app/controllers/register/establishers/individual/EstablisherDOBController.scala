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

package controllers.register.establishers.individual

import config.FrontendAppConfig
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import javax.inject.Inject
import models.{Index, Mode}
import navigators.Navigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{NotImplemented, Redirect}
import play.api.mvc.{Action, AnyContent}
import services.UserAnswersService
import utils.annotations.EstablishersIndividual

import scala.concurrent.ExecutionContext

class EstablisherDOBController @Inject()(
                                           val appConfig: FrontendAppConfig,
                                           val messagesApi: MessagesApi,
                                           val userAnswersService: UserAnswersService,
                                           @EstablishersIndividual val navigator: Navigator,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           allowAccess: AllowAccessActionProvider,
                                           requireData: DataRequiredAction
                                         )(implicit val ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] =
    (authenticate andThen getData(mode, srn) andThen allowAccess(srn) andThen requireData) {
      implicit request => NotImplemented("Not implemented: " + this.getClass.toString)
    }

  def onSubmit(mode: Mode, index: Index, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen requireData) {
    implicit request => Redirect(controllers.routes.IndexController.onPageLoad())
  }
}
