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
import identifiers.MinimalPsaDetailsId
import javax.inject.Inject
import models._
import models.details.transformation.SchemeDetailsMasterSection
import models.requests.OptionalDataRequest
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.{SchemeDetailsReadOnly, SchemeDetailsUpdate}
import utils.{HsTaskListHelperVariations, Toggles, UserAnswers}
import viewmodels.SchemeDetailsTaskList
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.{ExecutionContext, Future}

class PSASchemeDetailsController @Inject()(appConfig: FrontendAppConfig,
                                           override val messagesApi: MessagesApi,
                                           schemeDetailsConnector: SchemeDetailsConnector,
                                           schemeTransformer: SchemeDetailsMasterSection,
                                           authenticate: AuthAction,
                                           getData: DataRetrievalAction,
                                           errorHandler: ErrorHandler,
                                           featureSwitchManagementService: FeatureSwitchManagementService,
                                           lockConnector: PensionSchemeVarianceLockConnector,
                                           @SchemeDetailsReadOnly schemeDetailsReadOnlyCacheConnector: UserAnswersCacheConnector,
                                           @SchemeDetailsUpdate updateConnector: UserAnswersCacheConnector,
                                           minimalPsaConnector: MinimalPsaConnector
                                          )(implicit val ec: ExecutionContext) extends FrontendController with I18nSupport {

  def onPageLoad(srn: String): Action[AnyContent] = (authenticate andThen getData(UpdateMode, Some(srn))).async {
    implicit request =>
      if (featureSwitchManagementService.get(Toggles.isVariationsEnabled)) {
        onPageLoadVariationsToggledOn(srn)
      } else {
        onPageLoadVariationsToggledOff(srn)
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

  private def onPageLoadVariationsToggledOn(srn: String)(implicit
                                                         request: OptionalDataRequest[AnyContent],
                                                         hc: HeaderCarrier): Future[Result] = {
    getSchemeDetailsVariations(request.psaId.id, srn).flatMap(
      saveUserAnswersWithPSAMinimalDetails(srn, _).map { userAnswers =>
        val taskList: SchemeDetailsTaskList = new HsTaskListHelperVariations(userAnswers, request.viewOnly, Some(srn)).taskList
        Ok(schemeDetailsTaskList(appConfig, taskList, isVariations = true))
      }
    )
  }

  private def getSchemeDetailsVariations(psaId: String,
                                         srn: String)(implicit request: OptionalDataRequest[AnyContent],
                                                      hc: HeaderCarrier): Future[UserAnswers] = {
    request.userAnswers match {
      case Some(ua) => Future.successful(ua)
      case _ => schemeDetailsConnector.getSchemeDetailsVariations(psaId, schemeIdType = "srn", srn)
    }
  }

  private def saveUserAnswersWithPSAMinimalDetails(srn: String, userAnswers: UserAnswers)(implicit
                                                                                          request: OptionalDataRequest[AnyContent],
                                                                             hc: HeaderCarrier): Future[UserAnswers] = {
    minimalPsaConnector.getMinimalPsaDetails(request.psaId.id).flatMap { minimalDetails =>
      val json = userAnswers.json.as[JsObject] ++ Json.obj(
        MinimalPsaDetailsId.toString -> Json.toJson(minimalDetails)
      )
      lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, srn).flatMap { optionLock =>
        val futureJsValue = optionLock match {
          case Some(VarianceLock) => updateConnector.upsert(srn, json)
          case _ => schemeDetailsReadOnlyCacheConnector.upsert(request.externalId, json)
        }
        futureJsValue.map(UserAnswers)
      }
    }
  }
}
