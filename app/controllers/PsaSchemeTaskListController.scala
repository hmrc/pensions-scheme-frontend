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

package controllers

import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.actions.*
import identifiers.SchemeNameId
import models.*
import models.AuthEntity.PSA
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc.*
import play.api.Logging
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import views.html.{oldPsaTaskList, psaTaskListRegistration}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PsaSchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                            override val messagesApi: MessagesApi,
                                            authenticate: AuthAction,
                                            getData: DataRetrievalAction,
                                            @TaskList allowAccess: AllowAccessActionProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            val oldView: oldPsaTaskList,
                                            val viewRegistration: psaTaskListRegistration,
                                            hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                            hsTaskListHelperVariations: HsTaskListHelperVariations,
                                            dataCacheConnector: UserAnswersCacheConnector
                                           )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals
    with Logging {

  private def parseDateElseException(dateOpt: Option[JsValue]): Option[LastUpdated] =
    dateOpt.map(ts =>
      LastUpdated(
        ts.validate[Long] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
      )
    )

  def onPageLoad(mode: Mode, srn: OptionalSchemeReferenceNumber): Action[AnyContent] =
    (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true) andThen allowAccess(srn)).async {
      implicit request =>
        val lastUpdatedDate: Future[Option[LastUpdated]] =
          mode match {
            case NormalMode | CheckMode =>
              dataCacheConnector.lastUpdated(request.externalId).map(parseDateElseException)
            case _ =>
              Future.successful(None)
          }

        val schemeNameOpt: Option[String] =
          request.userAnswers.flatMap(_.get(SchemeNameId))
        
        lastUpdatedDate.flatMap { date =>
          (srn, request.userAnswers, schemeNameOpt) match {
            case (OptionalSchemeReferenceNumber(None), Some(userAnswers), Some(schemeName)) =>
              Future.successful(Ok(viewRegistration(hsTaskListHelperRegistration.taskList(userAnswers, None, srn, date), schemeName)))
            case (OptionalSchemeReferenceNumber(Some(_)), Some(userAnswers), Some(schemeName)) =>
              Future.successful(Ok(oldView(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName)))
            case (OptionalSchemeReferenceNumber(Some(_)), None, sn) =>
              logger.warn(s"Loading PSA task list page: srn $srn found but no user answers. Scheme name is $sn")
              Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))
            case _ =>
              Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
          }
        }
    }
}
