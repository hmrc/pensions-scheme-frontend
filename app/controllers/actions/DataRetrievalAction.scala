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

package controllers.actions


import com.google.inject.{ImplementedBy, Inject}
import connectors._
import identifiers.{IsPsaSuspendedId, SchemeSrnId, SchemeStatusId}
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.libs.json.JsValue
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalImpl(
                         dataConnector: UserAnswersCacheConnector,
                         viewConnector: SchemeDetailsReadOnlyCacheConnector,
                         updateConnector: UpdateSchemeCacheConnector,
                         lockConnector: PensionSchemeVarianceLockConnector,
                         schemeDetailsConnector: SchemeDetailsConnector,
                         minimalPsaConnector: MinimalPsaConnector,
                         mode: Mode,
                         srn: Option[String],
                         refreshData: Boolean
                       )(implicit val executionContext: ExecutionContext) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers,
      Some(request.session))
    mode match {
      case NormalMode | CheckMode =>
        createOptionalRequest(dataConnector.fetch(request.externalId), viewOnly = false)(request)
      case UpdateMode | CheckUpdateMode =>
        (srn, request.psaId) match {
          case (Some(extractedSrn), Some(psaId)) =>
            lockConnector.isLockByPsaIdOrSchemeId(psaId.id, extractedSrn).flatMap(optionLock =>
              getOptionalDataRequest(extractedSrn, optionLock, psaId.id, refreshData)(request, hc))
          case _ => Future(OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId))
        }
    }
  }

  private def getOptionalDataRequest[A](srn: String,
                                        optionLock: Option[Lock],
                                        psaId: String,
                                        refresh: Boolean)
                                       (implicit request: AuthenticatedRequest[A],
                                        hc: HeaderCarrier): Future[OptionalDataRequest[A]] = {

    (refresh, optionLock) match {
      case (true, Some(VarianceLock)) =>
        val optJs: Future[Option[JsValue]] = updateConnector.fetch(srn).flatMap {
          case Some(ua) =>
            addSuspensionFlagAndUpdateRepository(srn, ua, psaId, updateConnector.upsert(srn, _)).map(Some(_))
          case _ => Future.successful(None)
        }
        createOptionalRequest(optJs, viewOnly = false)
      case (false, Some(VarianceLock)) => createOptionalRequest(updateConnector.fetch(srn), viewOnly = false)
      case (_, Some(_)) => getRequestWithLock(srn, refresh, psaId)
      case _ => getRequestWithNoLock(srn, refresh, psaId)
    }
  }

  private def createOptionalRequest[A](f: Future[Option[JsValue]], viewOnly: Boolean)
                                      (implicit request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    f.map {
      case None => OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId, viewOnly)
      case Some(data) => OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(data)), request.psaId, request.pspId, viewOnly)
    }

  private def refreshBasedJsFetch[A](refresh: Boolean, srn: String, psaId: String)
                                    (implicit request: AuthenticatedRequest[A],
                                     hc: HeaderCarrier): Future[Option[JsValue]] =
    if (refresh) {
      schemeDetailsConnector
        .getSchemeDetails(psaId, schemeIdType = "srn", srn)
        .flatMap(ua => addSuspensionFlagAndUpdateRepository(srn, ua.json, psaId, viewConnector.upsert(request.externalId, _)))
        .map(Some(_))
    } else {
      viewConnector.fetch(request.externalId)
    }

  private def addSuspensionFlagAndUpdateRepository[A](srn: String,
                                                      jsValue: JsValue,
                                                      psaId: String,
                                                      upsertUserAnswers: JsValue => Future[JsValue])
                                                     (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.isPsaSuspended(psaId).flatMap { isSuspended =>
      val updatedUserAnswers = UserAnswers(jsValue).set(IsPsaSuspendedId)(isSuspended).flatMap(
        _.set(SchemeSrnId)(srn)).asOpt.getOrElse(UserAnswers(jsValue))
      upsertUserAnswers(updatedUserAnswers.json)
    }
  }

  private def getRequestWithLock[A](srn: String, refresh: Boolean, psaId: String)
                                   (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier)
  : Future[OptionalDataRequest[A]] = {
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(data) =>
        UserAnswers(data).get(SchemeSrnId) match {
          case Some(foundSrn) if foundSrn == srn =>
            OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(data)), request.psaId, request.pspId, viewOnly = true)
          case _ =>
            OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId, viewOnly = true)
        }
      case None =>
        OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId, viewOnly = true)
    }
  }

  private def getRequestWithNoLock[A](srn: String, refresh: Boolean, psaId: String)(implicit request: AuthenticatedRequest[A], hc: HeaderCarrier)
  : Future[OptionalDataRequest[A]] = {
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(answersJsValue) =>
        val ua: UserAnswers = UserAnswers(answersJsValue)
        (ua.get(SchemeSrnId), ua.get(SchemeStatusId)) match {
          case (Some(foundSrn), Some(status)) if foundSrn == srn =>
            OptionalDataRequest(request.request, request.externalId, Some(ua), request.psaId, request.pspId, viewOnly = status != "Open")
          case (Some(_), _) =>
            OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId, viewOnly = true)
          case _ =>
            OptionalDataRequest(request.request, request.externalId, Some(ua), request.psaId, request.pspId, viewOnly = true)
        }
      case None =>
        OptionalDataRequest(request.request, request.externalId, None, request.psaId, request.pspId, viewOnly = true)
    }
  }

}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[AuthenticatedRequest, OptionalDataRequest]

class DataRetrievalActionImpl @Inject()(dataConnector: UserAnswersCacheConnector,
                                        viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                        updateConnector: UpdateSchemeCacheConnector,
                                        lockConnector: PensionSchemeVarianceLockConnector,
                                        schemeDetailsConnector: SchemeDetailsConnector,
                                        minimalPsaConnector: MinimalPsaConnector
                                       )(implicit ec: ExecutionContext) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: Option[String], refreshData: Boolean): DataRetrieval = {
    new DataRetrievalImpl(dataConnector,
      viewConnector,
      updateConnector,
      lockConnector,
      schemeDetailsConnector,
      minimalPsaConnector,
      mode,
      srn,
      refreshData)
  }
}

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(mode: Mode = NormalMode, srn: Option[String] = None, refreshData: Boolean = false): DataRetrieval
}


