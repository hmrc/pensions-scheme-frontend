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
import connectors.UserAnswersCacheConnector
import controllers.actions._
import identifiers.SchemeNameId
import models.AuthEntity.PSA
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc._
import services.FeatureToggleService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.TaskList
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import viewmodels.{Message, SchemeDetailsTaskList, SchemeDetailsTaskListEntitySection}
import views.html.{oldPsaTaskList, psaTaskListRegistration, psaTaskListVariations}

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
                                            val viewRegistration: psaTaskListRegistration,
                                            val viewVariations: psaTaskListVariations,
                                            hsTaskListHelperRegistration: HsTaskListHelperRegistration,
                                            hsTaskListHelperVariations: HsTaskListHelperVariations,
                                            dataCacheConnector: UserAnswersCacheConnector
                                           )(implicit val executionContext: ExecutionContext) extends
  FrontendBaseController with I18nSupport with Retrievals {

  private def parseDateElseException(dateOpt: Option[JsValue]): Option[LastUpdated] =
    dateOpt.map(ts =>
      LastUpdated(
        ts.validate[Long] match {
          case JsSuccess(value, _) => value
          case JsError(errors) => throw JsResultException(errors)
        }
      )
    )


  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate(Some(PSA)) andThen getData(mode, srn, refreshData = true)
    andThen allowAccess(srn)).async {
    implicit request =>
      import play.twirl.api.HtmlFormat.Appendable

      val lastUpdatedDate: Future[Option[LastUpdated]] = mode match {
        case NormalMode | CheckMode => dataCacheConnector.lastUpdated(request.externalId)
          .map(parseDateElseException)
        case _ => Future.successful(None)
      }

      def renderViewRegistrations(taskSections: SchemeDetailsTaskList, schemeName: String): Future[Appendable] = {
        featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map {
          case true => viewRegistration(taskSections, schemeName)
          case _ => oldView(taskSections, schemeName)
        }
      }

      def renderViewVariations(taskSections: SchemeDetailsTaskList, schemeName: String): Future[Appendable] = {
        featureToggleService.get(FeatureToggleName.SchemeRegistration).map(_.isEnabled).map {
          case true => viewVariations(taskSections, schemeName)
          case _ => oldView(taskSections, schemeName)
        }
      }

      lastUpdatedDate.flatMap { date =>

        val schemeNameOpt: Option[String] = request.userAnswers.flatMap(_.get(SchemeNameId))
        (srn, request.userAnswers, schemeNameOpt) match {
          case (None, Some(userAnswers), Some(schemeName)) =>
            renderViewRegistrations(hsTaskListHelperRegistration.taskList(userAnswers, None, srn, date), schemeName).map {
              Ok(_)
            }

          case (Some(_), Some(userAnswers), Some(schemeName)) =>
            renderViewVariations(hsTaskListHelperVariations.taskList(userAnswers, Some(request.viewOnly), srn), schemeName).map {
              Ok(_)
            }

          case (Some(_), _, _) =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad))

          case _ =>
            Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
        }
      }
  }
}
