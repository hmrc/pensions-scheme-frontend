/*
 * Copyright 2020 HM Revenue & Customs
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
    (mode, refreshData) match {
      case (NormalMode | CheckMode, _) =>
        dataConnector
          .fetch(request.externalId)
          .map(optJsValue => getOptionalRequest(optJsValue.map(UserAnswers), viewOnly = false)(request))
      case (UpdateMode | CheckUpdateMode, false) =>
        variationsTransform(srn, getUserAnswersBasedOnLockStatus(_, _)(request, hc))(request, hc)
      case (UpdateMode | CheckUpdateMode, true) =>
        variationsTransform(srn, getUserAnswersWithDataRefreshIfNotLocked(_, _)(request, hc))(request, hc)
    }
  }

  private def variationsTransform[A](srn: Option[String],
                                     obtainUserAnswers: (String, Option[Lock]) => Future[Option[UserAnswers]]
                                    )(implicit request: AuthenticatedRequest[A],
                                      hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    srn match {
      case Some(extractedSrn) =>
        lockConnector.isLockByPsaIdOrSchemeId(request.psaId.id, extractedSrn).flatMap(optionLock =>
          obtainUserAnswers(extractedSrn, optionLock)
            .map(optionUA => getOptionalDataRequest(extractedSrn, optionLock, optionUA))
        )
      case _ => Future(OptionalDataRequest(request.request, request.externalId, None, request.psaId))
    }

  private def getUserAnswersIfHasLocked(srn: String,
                                        optionLock: Option[Lock])(implicit
                                                                  hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    optionLock match {
      case Some(VarianceLock) =>
        updateConnector.fetch(srn).map {
          case None => None
          case x@Some(_) => x.map(UserAnswers)
        }
      case _ => Future.successful(None)
    }
  }

  private def getOptionalDataRequest[A](srn: String,
                                        optionLock: Option[Lock],
                                        optionUserAnswers: Option[UserAnswers])(implicit
                                                                                request: AuthenticatedRequest[A],
                                                                                hc: HeaderCarrier): OptionalDataRequest[A] = {
    optionLock match {
      case Some(VarianceLock) => getOptionalRequest(optionUserAnswers, viewOnly = false)(request)
      case Some(_) => getRequestWithLock(request, srn, optionUserAnswers)
      case None => getRequestWithNoLock(request, srn, optionUserAnswers)
    }
  }

  private def getUserAnswersBasedOnLockStatus[A](srn: String,
                                                 optionLock: Option[Lock])(implicit
                                                                           request: AuthenticatedRequest[A],
                                                                           hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    val futureRetrievedJson = optionLock match {
      case Some(VarianceLock) => updateConnector.fetch(srn)
      case Some(_) => viewConnector.fetch(request.externalId)
      case None => viewConnector.fetch(request.externalId)
    }
    futureRetrievedJson.map(optJsValue => optJsValue.map(UserAnswers))
  }

  private def getUserAnswersWithDataRefreshIfNotLocked[A](srn: String,
                                                          optionLock: Option[Lock])(implicit
                                                                                    request: AuthenticatedRequest[A],
                                                                                    hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    getUserAnswersIfHasLocked(srn, optionLock).flatMap { optionUA =>
      val futureOptionJsValue = (optionLock, optionUA) match {
        case (Some(VarianceLock), Some(ua)) =>
          addSuspensionFlagAndUpdateRepository(srn, ua, updateConnector.upsert(srn, _)).map(Some(_))
        case (Some(VarianceLock), None) => Future.successful(None)
        case _ =>
          schemeDetailsConnector
            .getSchemeDetailsVariations(request.psaId.id, schemeIdType = "srn", srn)
            .flatMap(addSuspensionFlagAndUpdateRepository(srn, _, viewConnector.upsert(request.externalId, _)))
            .map(Some(_))
      }
      futureOptionJsValue.map(_.map(UserAnswers))
    }
  }

  private def getOptionalRequest[A](optionUA: Option[UserAnswers], viewOnly: Boolean)(implicit
                                                                                      request: AuthenticatedRequest[A])
  : OptionalDataRequest[A] =
    optionUA match {
      case None => OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly)
      case ua@Some(_) => OptionalDataRequest(request.request, request.externalId, ua, request.psaId, viewOnly)
    }

  private def addSuspensionFlagAndUpdateRepository[A](srn: String,
                                                      userAnswers: UserAnswers,
                                                      upsertUserAnswers: JsValue => Future[JsValue])(implicit
                                                                                                     request: AuthenticatedRequest[A],
                                                                                                     hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.isPsaSuspended(request.psaId.id).flatMap { isSuspended =>
      val updatedUserAnswers = userAnswers.set(IsPsaSuspendedId)(isSuspended).flatMap(
        _.set(SchemeSrnId)(srn)).asOpt.getOrElse(userAnswers)
      upsertUserAnswers(updatedUserAnswers.json)
    }
  }

  private def getRequestWithLock[A](request: AuthenticatedRequest[A], srn: String, optionUA: Option[UserAnswers])(implicit hc: HeaderCarrier)
  : OptionalDataRequest[A] = {
    optionUA match {
      case None =>
        OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly = true)
      case Some(data) =>
        data.get(SchemeSrnId) match {
          case Some(foundSrn) if foundSrn == srn =>
            OptionalDataRequest(request.request, request.externalId, optionUA, request.psaId, viewOnly = true)
          case _ =>
            OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly = true)
        }
    }
  }

  private def getRequestWithNoLock[A](request: AuthenticatedRequest[A], srn: String, optionUA: Option[UserAnswers])(implicit hc: HeaderCarrier)
  : OptionalDataRequest[A] = {
    optionUA match {
      case Some(ua) =>
        (ua.get(SchemeSrnId), ua.get(SchemeStatusId)) match {
          case (Some(foundSrn), Some(status)) if foundSrn == srn =>
            OptionalDataRequest(request.request, request.externalId, optionUA, request.psaId, viewOnly = status != "Open")
          case (Some(_), _) =>
            OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly = true)
          case _ =>
            OptionalDataRequest(request.request, request.externalId, optionUA, request.psaId, viewOnly = true)
        }
      case None =>
        OptionalDataRequest(request.request, request.externalId, None, request.psaId, viewOnly = true)
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
