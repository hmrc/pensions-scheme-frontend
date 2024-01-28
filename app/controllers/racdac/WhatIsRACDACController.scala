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

package controllers.racdac

import connectors.PensionAdministratorConnector
import controllers.actions._

import javax.inject.Inject
import models.NormalMode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Racdac
import views.html.racdac.whatIsRACDAC

import scala.concurrent.{ExecutionContext, Future}

class WhatIsRACDACController @Inject()(override val messagesApi: MessagesApi,
                                                     authenticate: AuthAction,
                                                     pensionAdministratorConnector: PensionAdministratorConnector,
                                                     @Racdac getData: DataRetrievalAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: whatIsRACDAC
                                                    )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad: Action[AnyContent] = (authenticate() andThen getData() andThen allowAccess(None)).async {
    implicit request =>
      pensionAdministratorConnector.getPSAName.flatMap { psaName =>
        Future.successful(Ok(view(psaName)))
      }
  }

  def onSubmit: Action[AnyContent] = authenticate() {
      Redirect(controllers.racdac.routes.RACDACNameController.onPageLoad(NormalMode))
  }
}
