/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.SessionDataCacheConnector
import controllers.actions.AuthAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LogoutController @Inject()(appConfig: FrontendAppConfig,
                                 val controllerComponents: MessagesControllerComponents,
                                 authenticate: AuthAction,
                                 sessionDataCacheConnector: SessionDataCacheConnector
                                )(implicit val ec: ExecutionContext) extends
  FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate().async { implicit request =>
    sessionDataCacheConnector.removeAll.map { _ =>
      Redirect(appConfig.serviceSignOut)
    }
  }
  def keepAlive: Action[AnyContent] = Action.async {
    Future successful Ok("OK")
  }
}
