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
import connectors.SchemeDetailsConnector
import controllers.actions._
import handlers.ErrorHandler
import identifiers.PsaDetailsId
import javax.inject.Inject
import models.details.transformation.SchemeDetailsMasterSection
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.HsTaskListHelperVariations
import viewmodels.SchemeDetailsTaskList
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.{ExecutionContext, Future}

class PSASchemeDetailsController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           schemeDetailsConnector: SchemeDetailsConnector,
                                           schemeTransformer: SchemeDetailsMasterSection,
                                           authenticate: AuthAction,
                                           errorHandler: ErrorHandler,
                                           featureSwitchManagementService: FeatureSwitchManagementService
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(srn: String): Action[AnyContent] = authenticate.async {
    implicit request =>
      if (featureSwitchManagementService.get("is-variations-enabled")) {
        onPageLoadVariationsToggledOn(srn)
      } else {
        onPageLoadVariationsToggledOff(srn)
      }
  }

  private def onPageLoadVariationsToggledOff(srn: String)(implicit
                                                          request: AuthenticatedRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {
    schemeDetailsConnector.getSchemeDetails(request.psaId.id, schemeIdType = "srn", srn).flatMap { scheme =>
      if (scheme.psaDetails.exists(_.id == request.psaId.id)) {
        val schemeDetailMasterSection = schemeTransformer.transformMasterSection(scheme)
        Future.successful(Ok(psa_scheme_details(appConfig, schemeDetailMasterSection, scheme.schemeDetails.name, srn)))
      } else {
        Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }
  }

  private def onPageLoadVariationsToggledOn(srn: String)(implicit
                                                         request: AuthenticatedRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {
    schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn).flatMap { userAnswers =>
      val schemeAdministrators = userAnswers.get(PsaDetailsId).toSeq.flatten
      if (schemeAdministrators.contains(request.psaId.id)) {
        
        val taskSections: SchemeDetailsTaskList = new HsTaskListHelperVariations(userAnswers).taskList
        Future.successful(Ok(schemeDetailsTaskList(appConfig, taskSections)))
      } else {
        Future.successful(NotFound(errorHandler.notFoundTemplate))
      }
    }
  }
}
