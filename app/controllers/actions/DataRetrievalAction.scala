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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import connectors._
import identifiers.PsaMinimalFlagsId._
import identifiers.racdac.IsRacDacId
import identifiers.{PsaMinimalFlagsId, SchemeSrnId, SchemeStatusId}
import models.OptionalSchemeReferenceNumber.toSrn
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.UserAnswers
import utils.annotations.Racdac

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalImpl(
                         dataConnector: UserAnswersCacheConnector,
                         viewConnector: SchemeDetailsReadOnlyCacheConnector,
                         updateConnector: UpdateSchemeCacheConnector,
                         lockConnector: PensionSchemeVarianceLockConnector,
                         schemeDetailsConnector: SchemeDetailsConnector,
                         minimalPsaConnector: MinimalPsaConnector,
                         mode: Mode,
                         srn: OptionalSchemeReferenceNumber,
                         refreshData: Boolean
                       )(implicit val executionContext: ExecutionContext) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    mode match {
      case NormalMode | CheckMode =>
        createOptionalRequest(dataConnector.fetch(request.externalId), viewOnly = false)(request)
      case UpdateMode | CheckUpdateMode =>
        (toSrn(srn), request.psaId) match {
          case (Some(extractedSrn), Some(psaId)) =>
            lockConnector.isLockByPsaIdOrSchemeId(psaId.id, extractedSrn).flatMap(optionLock =>
              getOptionalDataRequest(extractedSrn, optionLock, psaId.id, refreshData)(request, hc))
          case _ => Future(OptionalDataRequest(
            request = request.request,
            externalId = request.externalId,
            userAnswers = None,
            psaId = request.psaId,
            pspId = request.pspId,
            administratorOrPractitioner = request.administratorOrPractitioner
          ))
        }
    }
  }

  private def getOptionalDataRequest[A](srn: SchemeReferenceNumber,
                                        optionLock: Option[Lock],
                                        psaId: String,
                                        refresh: Boolean)
                                       (implicit request: AuthenticatedRequest[A],
                                        hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    (refresh, optionLock) match {
      case (true, Some(VarianceLock)) =>
        updateConnector.fetch(srn).flatMap {
          case Some(ua) =>
            val optJs = addMinimalFlagsAndUpdateRepository(srn, ua, psaId, updateConnector.upsert(srn, _)).map(Some(_))
            createOptionalRequest(optJs, viewOnly = false)
          case _ =>
            lockConnector.releaseLock(psaId, srn).flatMap(_ => getRequestWithNoLock(srn, refresh, psaId))
        }
      case (false, Some(VarianceLock)) => createOptionalRequest(updateConnector.fetch(srn), viewOnly = false)
      case (_, Some(_)) => getRequestWithLock(srn, refresh, psaId)
      case _ => getRequestWithNoLock(srn, refresh, psaId)
    }


  private def createOptionalRequest[A](f: Future[Option[JsValue]], viewOnly: Boolean)
                                      (implicit request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    f.map {
      case None => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = None,
        psaId = request.psaId,
        pspId = request.pspId,
        viewOnly = viewOnly,
        administratorOrPractitioner = request.administratorOrPractitioner
      )
      case Some(data) => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = Some(UserAnswers(data)),
        psaId = request.psaId,
        pspId = request.pspId,
        viewOnly = viewOnly,
        administratorOrPractitioner = request.administratorOrPractitioner
      )
    }

  private def refreshBasedJsFetch[A](refresh: Boolean, srn: SchemeReferenceNumber, psaId: String)
                                    (implicit request: AuthenticatedRequest[A],
                                     hc: HeaderCarrier): Future[Option[JsValue]] =
    if (refresh) {
      schemeDetailsConnector
        .getSchemeDetails(psaId, schemeIdType = "srn", srn, Some(true))
        .flatMap(ua => addMinimalFlagsAndUpdateRepository(srn, ua.json, psaId, viewConnector.upsert(request.externalId, _)))
        .map(Some(_))
    } else {
      viewConnector.fetch(request.externalId)
    }

  private def addMinimalFlagsAndUpdateRepository[A](srn: SchemeReferenceNumber,
                                                    jsValue: JsValue,
                                                    psaId: String,
                                                    upsertUserAnswers: JsValue => Future[JsValue])
                                                   (implicit hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.getMinimalFlags(psaId).flatMap { minimalFlags =>
      val ua = UserAnswers(jsValue)
        .set(PsaMinimalFlagsId)(minimalFlags)
        .flatMap(
          _.set(SchemeSrnId)(srn)).asOpt.getOrElse(UserAnswers(jsValue))
      upsertUserAnswers(ua.json)
    }
  }

  private def getRequestWithLock[A](srn: SchemeReferenceNumber, refresh: Boolean, psaId: String)
                                   (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(data) =>
        UserAnswers(data).get(SchemeSrnId) match {
          case Some(foundSrn) if foundSrn == srn.id =>
            OptionalDataRequest(
              request = request.request,
              externalId = request.externalId,
              userAnswers = Some(UserAnswers(data)),
              psaId = request.psaId,
              pspId = request.pspId,
              viewOnly = true,
              administratorOrPractitioner = request.administratorOrPractitioner
            )

          case _ =>
            OptionalDataRequest(
              request = request.request,
              externalId = request.externalId,
              userAnswers = None,
              psaId = request.psaId,
              pspId = request.pspId,
              viewOnly = true,
              administratorOrPractitioner = request.administratorOrPractitioner
            )
        }
      case None =>
        OptionalDataRequest(
          request = request.request,
          externalId = request.externalId,
          userAnswers = None,
          psaId = request.psaId,
          pspId = request.pspId,
          viewOnly = true,
          administratorOrPractitioner = request.administratorOrPractitioner
        )
    }

  private def getRequestWithNoLock[A](srn: SchemeReferenceNumber, refresh: Boolean, psaId: String)
                                     (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(answersJsValue) =>
        val ua: UserAnswers = UserAnswers(answersJsValue)
        (ua.get(SchemeSrnId), ua.get(SchemeStatusId)) match {
          case (Some(foundSrn), Some(status)) if foundSrn == srn.id =>
            val viewOnlyStatus = if (ua.get(IsRacDacId).contains(true)) true else status != "Open"
            OptionalDataRequest(request.request, request.externalId, Some(ua), request.psaId, request.pspId, viewOnly = viewOnlyStatus,
              request.administratorOrPractitioner)
          case (Some(_), _) =>
            OptionalDataRequest(
              request = request.request,
              externalId = request.externalId,
              userAnswers = None,
              psaId = request.psaId,
              pspId = request.pspId,
              viewOnly = true,
              administratorOrPractitioner = request.administratorOrPractitioner
            )
          case _ =>
            OptionalDataRequest(
              request = request.request,
              externalId = request.externalId,
              userAnswers = Some(ua),
              psaId = request.psaId,
              pspId = request.pspId,
              viewOnly = true,
              administratorOrPractitioner = request.administratorOrPractitioner
            )
        }
      case None =>
        OptionalDataRequest(
          request = request.request,
          externalId = request.externalId,
          userAnswers = None,
          psaId = request.psaId,
          pspId = request.pspId,
          viewOnly = true,
          administratorOrPractitioner = request.administratorOrPractitioner
        )
    }
}

class RacdacDataRetrievalImpl(
                               mode: Mode,
                               @Racdac dataConnector: UserAnswersCacheConnector,
                               viewConnector: SchemeDetailsReadOnlyCacheConnector,
                               schemeDetailsConnector: SchemeDetailsConnector,
                               minimalPsaConnector: MinimalPsaConnector,
                               srnOpt: OptionalSchemeReferenceNumber)(implicit val executionContext: ExecutionContext) extends DataRetrieval {
  private val logger = Logger(classOf[RacdacDataRetrievalImpl])
  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    mode match {

      case NormalMode | CheckMode if request.psaId.isEmpty => throw MissingPsaIdException
      case NormalMode | CheckMode => getOrCreateOptionalRequest(dataConnector.fetch, viewOnly = false)(request)

      case UpdateMode | CheckUpdateMode =>
        (toSrn(srnOpt), request.psaId) match {
          case (Some(srn), Some(psaId)) =>
            getOrCreateOptionalRequest(dataInUpdateMode(srn, psaId.id)(request, hc), viewOnly = true)(request)
          case _ => Future(OptionalDataRequest(
            request = request.request,
            externalId = request.externalId,
            userAnswers = None,
            psaId = request.psaId,
            pspId = request.pspId,
            administratorOrPractitioner = request.administratorOrPractitioner
          ))
        }
    }
  }

  private def getOrCreateOptionalRequest[A](modeBasedDataFetch: String => Future[Option[JsValue]], viewOnly: Boolean)
                                           (implicit request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    modeBasedDataFetch(request.externalId).map {
      case None => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = None,
        psaId = request.psaId,
        pspId = request.pspId,
        administratorOrPractitioner = request.administratorOrPractitioner,
        viewOnly = viewOnly
      )
      case Some(data) => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = Some(UserAnswers(data)),
        psaId = request.psaId,
        pspId = request.pspId,
        administratorOrPractitioner = request.administratorOrPractitioner,
        viewOnly = viewOnly
      )
    }

  private def dataInUpdateMode[A](srn: SchemeReferenceNumber, psaId: String)
                                 (implicit request: AuthenticatedRequest[A],
                                  hc: HeaderCarrier): String => Future[Option[JsValue]] = {

    def refreshSchemeDetails: Future[Some[JsValue]] = schemeDetailsConnector
      .getSchemeDetails(psaId, schemeIdType = "srn", srn, Some(true))
      .flatMap(ua => addMinimalFlagsAndUpdateRepository(srn, ua.json, psaId, viewConnector.upsert(request.externalId, _)).map(Some(_)))

    id =>
      viewConnector.fetch(id) flatMap {
        case None => refreshSchemeDetails
        case Some(jsonValue) => (jsonValue \ SchemeSrnId.toString).validate[String] match {
          case JsSuccess(value, _) =>
            if (value eq srn.id) {
              Future.successful(Some(jsonValue))
            } else {
              refreshSchemeDetails
            }
          case JsError(_) =>
            logger.warn(s"No SRN found for id $id when retrieving from read-only scheme cache. Refreshing data.")
            refreshSchemeDetails
        }
      }
  }

  private def addMinimalFlagsAndUpdateRepository[A](srn: SchemeReferenceNumber,
                                                    jsValue: JsValue,
                                                    psaId: String,
                                                    upsertUserAnswers: JsValue => Future[JsValue])
                                                   (implicit hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.getMinimalFlags(psaId).flatMap { minimalFlags =>
      val ua = UserAnswers(jsValue)
        .set(PsaMinimalFlagsId)(minimalFlags)
        .flatMap(_.set(SchemeSrnId)(srn)).asOpt.getOrElse(UserAnswers(jsValue))
      upsertUserAnswers(ua.json)
    }
  }
}


case object MissingSchemeNameException extends Exception

case object MissingPsaIdException extends Exception

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[AuthenticatedRequest, OptionalDataRequest]

class DataRetrievalActionImpl @Inject()(dataConnector: UserAnswersCacheConnector,
                                        viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                        updateConnector: UpdateSchemeCacheConnector,
                                        lockConnector: PensionSchemeVarianceLockConnector,
                                        schemeDetailsConnector: SchemeDetailsConnector,
                                        minimalPsaConnector: MinimalPsaConnector
                                       )(implicit ec: ExecutionContext) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: OptionalSchemeReferenceNumber, refreshData: Boolean): DataRetrieval = {
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

class RacdacDataRetrievalActionImpl @Inject()(@Racdac dataConnector: UserAnswersCacheConnector,
                                              viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                              schemeDetailsConnector: SchemeDetailsConnector,
                                              minimalPsaConnector: MinimalPsaConnector)
                                             (implicit ec: ExecutionContext) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: OptionalSchemeReferenceNumber, refreshData: Boolean): DataRetrieval = {
    new RacdacDataRetrievalImpl(mode, dataConnector, viewConnector, schemeDetailsConnector, minimalPsaConnector, srn: OptionalSchemeReferenceNumber)
  }
}


trait DataRetrievalAction {
  def apply(mode: Mode = NormalMode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, refreshData: Boolean = false): DataRetrieval
}
