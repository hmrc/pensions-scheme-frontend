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

import config.FrontendAppConfig
import controllers.actions._
import identifiers.SchemeNameId
import models.AuthEntity.PSA
import models.Mode
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import views.html.psaTaskList

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PsaNormalSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @TaskList allowAccess: AllowAccessActionProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: psaTaskList,
                                         hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                         hsTaskListHelperVariations: HsTaskListHelperVariations
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).apply {
    implicit request =>
      val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
      (srn, request.userAnswers, schemeNameOpt) match {
        case (None, Some(userAnswers), Some(schemeName)) =>
          Ok(view(hsTaskListHelperRegistration.taskList(userAnswers, None, srn), schemeName))

        case (Some(_), Some(userAnswers), Some(schemeName)) =>
          Ok(view(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName))

        case (Some(_), _, _) =>
          Redirect(controllers.routes.SessionExpiredController.onPageLoad())

        case _ =>
          Redirect(appConfig.managePensionsSchemeOverviewUrl)
      }
  }
}
