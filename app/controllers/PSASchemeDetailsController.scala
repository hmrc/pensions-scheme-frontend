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
import connectors.{SchemeDetailsConnector, UserAnswersCacheConnector}
import controllers.actions._
import handlers.ErrorHandler
import javax.inject.Inject
import models.details.transformation.SchemeDetailsMasterSection
import models.requests.AuthenticatedRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.{HsTaskListHelperVariations, Toggles}
import utils.annotations.SchemeDetailsReadOnly
import viewmodels.SchemeDetailsTaskList
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.{ExecutionContext, Future}

class PSASchemeDetailsController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           schemeDetailsConnector: SchemeDetailsConnector,
                                           schemeTransformer: SchemeDetailsMasterSection,
                                           authenticate: AuthAction,
                                           errorHandler: ErrorHandler,
                                           featureSwitchManagementService: FeatureSwitchManagementService,
                                           @SchemeDetailsReadOnly schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(srn: String): Action[AnyContent] = authenticate.async {
    implicit request =>
      if (featureSwitchManagementService.get(Toggles.isVariationsEnabled)) {
        onPageLoadVariationsToggledOn(srn)
      } else {
        onPageLoadVariationsToggledOff(srn)
      }
  }

  private def onPageLoadVariationsToggledOff(srn: String)(implicit
                                                          request: AuthenticatedRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {
    schemeDetailsConnector.getSchemeDetails(request.psaId.id, schemeIdType = "srn", srn).flatMap { scheme =>
      val schemeDetailMasterSection = schemeTransformer.transformMasterSection(scheme)
      Future.successful(Ok(psa_scheme_details(appConfig, schemeDetailMasterSection, scheme.schemeDetails.name, srn)))
    }
  }

  private def onPageLoadVariationsToggledOn(srn: String)(implicit
                                                         request: AuthenticatedRequest[AnyContent],
                                                          hc: HeaderCarrier): Future[Result] = {
    schemeDetailsConnector.getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn).flatMap { userAnswers =>
      val taskList: SchemeDetailsTaskList = new HsTaskListHelperVariations(userAnswers, srn).taskList
      schemeDetailsReadOnlyCacheConnector.upsert(request.externalId, userAnswers.json).map( _ =>
        Ok(schemeDetailsTaskList(appConfig, taskList))
      )
    }
  }
}
