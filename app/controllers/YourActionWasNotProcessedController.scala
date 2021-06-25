/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.{AuthAction, DataRequiredAction, DataRetrievalAction}
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.yourActionWasNotProcessed

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class YourActionWasNotProcessedController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     view: yourActionWasNotProcessed
                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = {
    (authenticate() andThen getData(mode, srn) andThen requireData).async {
      implicit request =>
        val returnUrl = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn).url
        Future.successful(Ok(view(existingSchemeName, returnUrl)))
    }
  }

}
