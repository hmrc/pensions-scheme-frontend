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

package controllers

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.{PSANameCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import identifiers.{IndexId, PsaEmailId, PsaNameId}
import javax.inject.Inject
import models.{NormalMode, PSAName}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.ProductionMode
import views.html.whatYouWillNeed

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedController @Inject()(appConfig: FrontendAppConfig,
                                          override val messagesApi: MessagesApi,
                                          authenticate: AuthAction,
                                          psaNameCacheConnector: PSANameCacheConnector,
                                          crypto: ApplicationCrypto,
                                          userAnswersCacheConnector: UserAnswersCacheConnector,
                                          fs:FeatureSwitchManagementService
                                         ) (implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad: Action[AnyContent] = authenticate {
    implicit request =>
      Ok(whatYouWillNeed(appConfig))
  }

  def onSubmit: Action[AnyContent] = authenticate.async {
    implicit request =>
      if (fs.get("is-hub-enabled")) {
        Future.successful(Redirect(controllers.register.routes.SchemeTaskListController.onPageLoad()))
      }
      else {
        Future.successful(Redirect(controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode)))
      }
  }
}

final case class PSANameNotFoundException() extends Exception("Unable to retrieve PSA Name")
