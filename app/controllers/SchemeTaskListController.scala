/*
 * Copyright 2020 HM Revenue & Customs
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
import models.Mode
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import views.html.schemeDetailsTaskList

import scala.concurrent.{ExecutionContext, Future}

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @TaskList allowAccess: AllowAccessActionProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: schemeDetailsTaskList,
                                         hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                         hsTaskListHelperVariations: HsTaskListHelperVariations
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).async {
    implicit request =>
      (srn, request.userAnswers) match {
        case (None, Some(userAnswers)) =>
          Future.successful(Ok(view(hsTaskListHelperRegistration.taskList(userAnswers, None, srn))))
        case (Some(srnValue), optionUserAnswers) =>
          onPageLoadVariations(srnValue, optionUserAnswers)
        case _ =>
          Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }

  private def onPageLoadVariations(srn: String,
                                   ua: Option[UserAnswers])(implicit request: OptionalDataRequest[AnyContent],
                                                            hc: HeaderCarrier): Future[Result] = {
    request.userAnswers match {
      case Some(ua) =>
        val taskList = hsTaskListHelperVariations.taskList(ua, Some(request.viewOnly), Some(srn))
        Future.successful(Ok(view(taskList)))
      case _ => Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }
}
