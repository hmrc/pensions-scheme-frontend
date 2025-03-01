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

package controllers.register

import controllers.Retrievals
import controllers.actions._
import models.{OptionalSchemeReferenceNumber, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import views.html.register.cannotMakeChanges

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CannotMakeChangesController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             authenticate: AuthAction,
                                             getData: DataRetrievalAction,
                                             val controllerComponents: MessagesControllerComponents,
                                             val view: cannotMakeChanges,
                                             @TaskList allowAccessAction: AllowAccessActionProvider
                                           )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with Retrievals with I18nSupport {


  def onPageLoad(srn: OptionalSchemeReferenceNumber): Action[AnyContent] = (authenticate() andThen getData(UpdateMode, srn) andThen allowAccessAction(srn)).async {
    implicit request =>
      Future.successful(Ok(view(srn, existingSchemeName)))
  }
}
