/*
 * Copyright 2019 HM Revenue & Customs
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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors._
import controllers.actions._
import handlers.ErrorHandler
import identifiers.{IsPsaSuspendedId, SchemeSrnId, SchemeStatusId}
import javax.inject.Inject
import models.requests.OptionalDataRequest
import models.{Mode, VarianceLock}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{Toggles, UserAnswers}
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import viewmodels.SchemeDetailsTaskList
import views.html.schemeDetailsTaskList
import views.html.schemeDetailsTaskListNonHns

import scala.concurrent.{ExecutionContext, Future}

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         @TaskList allowAccess: AllowAccessActionProvider,
                                         schemeDetailsConnector: SchemeDetailsConnector,
                                         errorHandler: ErrorHandler,
                                         featureSwitchManagementService: FeatureSwitchManagementService,
                                         lockConnector: PensionSchemeVarianceLockConnector,
                                         viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                         updateConnector: UpdateSchemeCacheConnector,
                                         minimalPsaConnector: MinimalPsaConnector
                                        )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  private def isHnsEnabled: Boolean = featureSwitchManagementService.get(Toggles.isEstablisherCompanyHnSEnabled)
  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn) andThen allowAccess(srn)).async {
    implicit request =>
      (srn, request.userAnswers) match {

        case (None, Some(userAnswers)) =>
          val view = if(isHnsEnabled)
            schemeDetailsTaskList(appConfig, new HsTaskListHelperRegistration(userAnswers, featureSwitchManagementService).taskList)
          else
            schemeDetailsTaskListNonHns(appConfig, new HsTaskListHelperRegistration(userAnswers, featureSwitchManagementService).taskList)

          Future.successful(Ok(view))
        case (Some(srnValue), optionUserAnswers) =>
          onPageLoadVariations(srnValue, optionUserAnswers)
        case _ =>
          Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }

  private def onPageLoadVariations(srn: String,
                                            ua: Option[UserAnswers])(implicit request: OptionalDataRequest[AnyContent],
                                                                     hc: HeaderCarrier): Future[Result] = {
    lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap {
      case Some(VarianceLock) =>
        ua match {
          case Some(userAnswers) =>
            createViewWithSuspensionFlag(srn, userAnswers, updateConnector.upsert(srn, _), viewOnly = false)
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
      case Some(_) =>
        getSchemeDetailsAndReturnResult(srn, viewOnly = true)
      case _ =>
        getSchemeDetailsAndReturnResult(srn, viewOnly = false)
    }
  }

  private def getSchemeDetailsAndReturnResult(srn: String, viewOnly: Boolean)(implicit request: OptionalDataRequest[AnyContent],
                                                                              hc: HeaderCarrier): Future[Result] = {
    schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn)
      .flatMap { userAnswers =>
        createViewWithSuspensionFlag(srn, userAnswers, viewConnector.upsert(request.externalId, _), viewOnly)
      }
  }

  private def createViewWithSuspensionFlag(srn: String, userAnswers: UserAnswers,
                                           upsertUserAnswers: JsValue => Future[JsValue], viewOnly: Boolean)(implicit request: OptionalDataRequest[AnyContent],
                                                                                                             hc: HeaderCarrier): Future[Result] =
    minimalPsaConnector.isPsaSuspended(request.psaId.id).flatMap { isSuspended =>

      val updatedUserAnswers = userAnswers.set(IsPsaSuspendedId)(isSuspended).flatMap(
        _.set(SchemeSrnId)(srn)).asOpt.getOrElse(userAnswers)
      val taskList: SchemeDetailsTaskList = new HsTaskListHelperVariations(updatedUserAnswers,
        viewOnly || !userAnswers.get(SchemeStatusId).contains("Open"), Some(srn), featureSwitchManagementService).taskList

      upsertUserAnswers(updatedUserAnswers.json).flatMap { _ =>
        val view = if(isHnsEnabled)
          schemeDetailsTaskList(appConfig, taskList)
        else
          schemeDetailsTaskListNonHns(appConfig, taskList)
        Future.successful(Ok(view))
      }
    }

  case class TaskListDetails(userAnswers: UserAnswers, taskList: SchemeDetailsTaskList)

}
