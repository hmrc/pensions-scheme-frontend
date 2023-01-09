/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.actions._
import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.whatYouWillNeedMembers

import scala.concurrent.{ExecutionContext, Future}

class WhatYouWillNeedMembersController @Inject()(appConfig: FrontendAppConfig,
                                                 override val messagesApi: MessagesApi,
                                                 authenticate: AuthAction,
                                                 getData: DataRetrievalAction,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: whatYouWillNeedMembers
                                                )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData()).async {
    implicit request =>
      Future.successful(Ok(view(existingSchemeName)))
  }

  def onSubmit: Action[AnyContent] = authenticate() { Redirect(controllers.routes.CurrentMembersController.onPageLoad(NormalMode)) }
}
