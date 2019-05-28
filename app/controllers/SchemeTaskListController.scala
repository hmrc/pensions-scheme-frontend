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
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, SchemeDetailsConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import handlers.ErrorHandler
import identifiers.{IsPsaSuspendedId, SchemeStatusId}
import javax.inject.Inject
import models.{Mode, VarianceLock}
import models.details.transformation.SchemeDetailsMasterSection
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import utils.{Toggles, UserAnswers}
import viewmodels.SchemeDetailsTaskList
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.{ExecutionContext, Future}

class SchemeTaskListController @Inject()(appConfig: FrontendAppConfig,
                                         override val messagesApi: MessagesApi,
                                         authenticate: AuthAction,
                                         getData: DataRetrievalAction,
                                         schemeDetailsConnector: SchemeDetailsConnector,
                                         schemeTransformer: SchemeDetailsMasterSection,
                                         errorHandler: ErrorHandler,
                                         featureSwitchManagementService: FeatureSwitchManagementService,
                                         lockConnector: PensionSchemeVarianceLockConnector,
                                         viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                         updateConnector: UpdateSchemeCacheConnector,
                                         minimalPsaConnector: MinimalPsaConnector
                                        )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport with Retrievals {

  def onPageLoad(mode: Mode, srn: Option[String]): Action[AnyContent] = (authenticate andThen getData(mode, srn)).async {
    implicit request =>
      (srn, request.userAnswers) match {

        case (None, Some(userAnswers)) =>
          Future.successful(Ok(schemeDetailsTaskList(appConfig, new HsTaskListHelperRegistration(userAnswers).taskList)))

        case (Some(srnValue), _) if !featureSwitchManagementService.get(Toggles.isVariationsEnabled) =>
          onPageLoadVariationsToggledOff(srnValue)

        case (Some(srnValue), optionUserAnswers) =>
          onPageLoadVariationsToggledOn(srnValue, optionUserAnswers)
        case _ => Future.successful(Redirect(appConfig.managePensionsSchemeOverviewUrl))
      }
  }

  private def onPageLoadVariationsToggledOff(srn: String)(implicit
                                                          request: OptionalDataRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {

    schemeDetailsConnector.getSchemeDetails(request.psaId.id, schemeIdType = "srn", srn).flatMap { scheme =>

      val schemeDetailMasterSection = schemeTransformer.transformMasterSection(scheme)
      Future.successful(Ok(psa_scheme_details(appConfig, schemeDetailMasterSection, scheme.schemeDetails.name, srn)))

    }
  }

  private def onPageLoadVariationsToggledOn(srn: String,
                                            ua: Option[UserAnswers])(implicit request: OptionalDataRequest[AnyContent],
                                                                     hc: HeaderCarrier): Future[Result] = {
    lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap {
      case Some(VarianceLock) =>
        ua match {
          case Some(userAnswers) =>
            createViewWithSuspensionFlag(srn, userAnswers, updateConnector.upsert(srn, _), false)
          case _ =>
            Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
        }
      case Some(_) =>
        ua match {
          case Some(userAnswers) =>
            createViewWithSuspensionFlag(srn, userAnswers, viewConnector.upsert(srn, _), true)
          case _ =>
            schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn)
              .flatMap { userAnswers =>
                createViewWithSuspensionFlag(srn, userAnswers, viewConnector.upsert(request.externalId, _), true)
              }
        }
      case _ =>
        schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn)
          .flatMap { userAnswers =>
            createViewWithSuspensionFlag(srn, userAnswers, viewConnector.upsert(request.externalId, _), false)
          }
    }
  }

  private def createViewWithSuspensionFlag(srn: String, userAnswers: UserAnswers,
                                           upsertUserAnswers: JsValue => Future[JsValue], viewOnly: Boolean)(implicit request: OptionalDataRequest[AnyContent],
                                                                                                             hc: HeaderCarrier): Future[Result] =
    minimalPsaConnector.isPsaSuspended(request.psaId.id).flatMap { isSuspended =>

      val updatedUserAnswers = userAnswers.set(IsPsaSuspendedId)(isSuspended).asOpt.getOrElse(userAnswers)
      val taskList: SchemeDetailsTaskList = new HsTaskListHelperVariations(updatedUserAnswers,
        viewOnly || !userAnswers.get(SchemeStatusId).contains("Open"), Some(srn)).taskList

      upsertUserAnswers(updatedUserAnswers.json).flatMap { _ =>

        Future.successful(Ok(schemeDetailsTaskList(appConfig, taskList)))
      }
    }

  case class TaskListDetails(userAnswers: UserAnswers, taskList: SchemeDetailsTaskList)

}
