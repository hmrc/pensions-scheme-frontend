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
import identifiers.TcmpToggleId
import models.AuthEntity.PSA
import models.FeatureToggle.Enabled
import models.FeatureToggleName.TCMP

import javax.inject.Inject
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FeatureToggleService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import views.html.schemeDetailsTaskList

import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @TaskList allowAccess: AllowAccessActionProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: schemeDetailsTaskList,
                                         hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                         hsTaskListHelperVariations: HsTaskListHelperVariations,
                                         featureToggleService: FeatureToggleService
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).async {
    implicit request =>
      (srn, request.userAnswers) match {
        case (None, Some(userAnswers)) =>
          userAnswersWithTcmpToggle(userAnswers).flatMap { ua =>
            Future.successful(Ok(view(hsTaskListHelperRegistration.taskList(ua, None, srn))))
          }
        case (Some(_), Some(userAnswers)) =>
          userAnswersWithTcmpToggle(userAnswers).flatMap { ua =>
            Future.successful(Ok(view(hsTaskListHelperVariations.taskList(ua, Some(request.viewOnly), srn))))
          }
        case (Some(_), _) =>
          Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        case _ =>
          Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }

  def userAnswersWithTcmpToggle(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {
    featureToggleService.get(TCMP).map {
      case Enabled(_) => userAnswers.set(TcmpToggleId)(true).get
      case _ => userAnswers
    }
  }
}
