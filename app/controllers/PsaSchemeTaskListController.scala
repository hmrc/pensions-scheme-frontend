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
import connectors.SchemeDetailsConnector
import connectors.MinimalPsaConnector
import controllers.actions._
import identifiers.SchemeNameId
import identifiers.racdac.IsRacDacId
import models.AuthEntity.PSA
import models.requests.OptionalDataRequest
import models.{Mode, PSAMinimalFlags, UpdateMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import views.html.psaTaskList

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.annotations.Racdac


import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                            schemeDetailsConnector: SchemeDetailsConnector,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         val controllerComponents: MessagesControllerComponents
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {




  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = authenticate(Some(PSA)).async { implicit request =>
    (request.psaId, srn) match {
      case (Some(psaId), Some(srnNo)) =>
        schemeDetailsConnector.getSchemeDetails(psaId.id, "srn", srnNo).map {ua =>
          ua.get(IsRacDacId) match {
            case Some(true) =>
              Redirect(controllers.routes.PsaRacdacSchemeTaskListController.onPageLoad(mode,srn))
            case Some(false) =>
              Redirect(controllers.routes.PsaNormalSchemeTaskListController.onPageLoad(mode,srn))
            case _ =>
              Redirect(controllers.routes.SessionExpiredController.onPageLoad())
          }
        }
      case _ =>
        Future(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
    }
  }

//  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = authenticate(Some(PSA)).async {
//    implicit request =>
//
//      redirects.map {
//        case Some(result) => result
//        case _ =>
//          val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
//          val isRacDacOpt: Option[Boolean] = request.userAnswers.flatMap(_.get(IsRacDacId))
//
//          (srn, request.userAnswers, schemeNameOpt, isRacDacOpt) match {
//
//            case (_, Some(_), Some(_), Some(true)) =>
//              Redirect(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn))
//
//            case (None, Some(userAnswers), Some(schemeName), _) =>
//              Ok(view(hsTaskListHelperRegistration.taskList(userAnswers, None, srn), schemeName))
//
//            case (Some(_), Some(userAnswers), Some(schemeName), _) =>
//              Ok(view(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName))
//
//            case (Some(_), _, _, _) =>
//              Redirect(controllers.routes.SessionExpiredController.onPageLoad())
//
//            case _ =>
//              Redirect(appConfig.managePensionsSchemeOverviewUrl)
//          }
//      }
//  }
}
