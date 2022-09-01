/*
 * Copyright 2022 HM Revenue & Customs
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
import models.{FeatureToggleName, Mode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import viewmodels.SchemeDetailsTaskList
import views.html.{oldPsaTaskList, psaTaskList}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            @TaskList allowAccess: AllowAccessActionProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            featureToggleService: FeatureToggleService,
                                            val oldView: oldPsaTaskList,
                                            val view: psaTaskList,
                                            hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                            hsTaskListHelperVariations: HsTaskListHelperVariations
                                           )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).async {
    implicit request =>
      import play.twirl.api.HtmlFormat.Appendable

      def renderView(taskSections: SchemeDetailsTaskList, schemeName: String): Future[Appendable] = {
        featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map {
          case true => view.apply(taskSections, schemeName)
          case _ => oldView.apply(taskSections, schemeName)
        }
      }

      val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
      (srn, request.userAnswers, schemeNameOpt) match {
        case (None, Some(userAnswers), Some(schemeName)) =>
          renderView(hsTaskListHelperRegistration.taskList(userAnswers, None, srn), schemeName).map {
            Ok(_)
          }

        case (Some(_), Some(userAnswers), Some(schemeName)) =>
          renderView(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName).map {
            Ok(_)
          }

        case (Some(_), _, _) =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))

        case _ =>
          Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }
}
