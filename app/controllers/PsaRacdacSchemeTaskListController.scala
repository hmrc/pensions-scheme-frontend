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

class PsaRacdacSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         @Racdac getData: DataRetrievalAction,
                                         minimalPsaConnector: MinimalPsaConnector,
                                         @TaskList allowAccess: AllowAccessActionProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         val view: psaTaskList,
                                         hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                         hsTaskListHelperVariations: HsTaskListHelperVariations
                                        )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private def redirects(implicit request: OptionalDataRequest[AnyContent], hc: HeaderCarrier):Future[Option[Result]] = {
    request.psaId match {
      case None => Future.successful(None)
      case Some(psaId) =>
        minimalPsaConnector.getMinimalFlags(psaId.id).map {
          case PSAMinimalFlags(_, true, false) => Some(Redirect(Call("GET", appConfig.youMustContactHMRCUrl)))
          case PSAMinimalFlags(_, false, true) => Some(Redirect(Call("GET",appConfig.psaUpdateContactDetailsUrl)))
          case _ => None
        }
    }
  }

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).async {
    implicit request =>

      redirects.map {
        case Some(result) => result
        case _ =>
          val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
          val isRacDacOpt: Option[Boolean] = request.userAnswers.flatMap(_.get(IsRacDacId))

          (srn, request.userAnswers, schemeNameOpt, isRacDacOpt) match {

            case (_, Some(_), Some(_), Some(true)) =>
              Redirect(controllers.racdac.routes.CheckYourAnswersController.onPageLoad(UpdateMode, srn))

            case (None, Some(userAnswers), Some(schemeName), _) =>
              Ok(view(hsTaskListHelperRegistration.taskList(userAnswers, None, srn), schemeName))

            case (Some(_), Some(userAnswers), Some(schemeName), _) =>
              Ok(view(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName))

            case (Some(_), _, _, _) =>
              Redirect(controllers.routes.SessionExpiredController.onPageLoad())

            case _ =>
              Redirect(appConfig.managePensionsSchemeOverviewUrl)
          }
      }
  }
}
