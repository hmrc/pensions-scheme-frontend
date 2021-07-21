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
import identifiers.PsaMinimalFlagsId._
import identifiers.racdac.IsRacDacId
import identifiers.{PsaMinimalFlagsId, SchemeSrnId, SchemeStatusId}
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.libs.json.JsValue
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
                         srn: Option[String],
                         refreshData: Boolean
                       )(implicit val executionContext: ExecutionContext) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    mode match {
      case NormalMode | CheckMode =>
        createOptionalRequest(dataConnector.fetch(request.externalId), viewOnly = false)(request)
      case UpdateMode | CheckUpdateMode =>
        (srn, request.psaId) match {
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

  private def getOptionalDataRequest[A](srn: String,
                                        optionLock: Option[Lock],
                                        psaId: String,
                                        refresh: Boolean)
                                       (implicit request: AuthenticatedRequest[A],
                                        hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    (refresh, optionLock) match {
      case (true, Some(VarianceLock)) =>
        val optJs: Future[Option[JsValue]] = updateConnector.fetch(srn).flatMap {
          case Some(ua) =>
            addMinimalFlagsAndUpdateRepository(srn, ua, psaId).map(Some(_))
          case _ => Future.successful(None)
        }
        createOptionalRequest(optJs, viewOnly = false)
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

  private def refreshBasedJsFetch[A](refresh: Boolean, srn: String, psaId: String)
                                    (implicit request: AuthenticatedRequest[A],
                                     hc: HeaderCarrier): Future[Option[JsValue]] =
    if (refresh) {
      schemeDetailsConnector
        .getSchemeDetails(psaId, schemeIdType = "srn", srn)
        .flatMap(ua => addMinimalFlagsAndUpdateRepository(srn, ua.json, psaId))
        .map(Some(_))
    } else {
      viewConnector.fetch(request.externalId)
    }

  private def addMinimalFlagsAndUpdateRepository[A](srn: String,
                                                      jsValue: JsValue,
                                                      psaId: String)
                                                     (implicit hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.getMinimalFlags(psaId).map { minimalFlags =>
      UserAnswers(jsValue)
        .set(PsaMinimalFlagsId)(minimalFlags)
        .flatMap(
        _.set(SchemeSrnId)(srn)).asOpt.getOrElse(UserAnswers(jsValue)).json
    }
  }

  private def getRequestWithLock[A](srn: String, refresh: Boolean, psaId: String)
                                   (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(data) =>
        UserAnswers(data).get(SchemeSrnId) match {
          case Some(foundSrn) if foundSrn == srn =>
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

  private def getRequestWithNoLock[A](srn: String, refresh: Boolean, psaId: String)
                                     (implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case Some(answersJsValue) =>
        val ua: UserAnswers = UserAnswers(answersJsValue)
        (ua.get(SchemeSrnId), ua.get(SchemeStatusId)) match {
          case (Some(foundSrn), Some(status)) if foundSrn == srn =>
            val viewOnlyStatus = if(ua.get(IsRacDacId).contains(true)) true else status != "Open"
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
                         @Racdac dataConnector: UserAnswersCacheConnector,
                         mode: Mode,
                         minimalPsaConnector: MinimalPsaConnector,
                         schemeDetailsConnector: SchemeDetailsConnector,
                         srn: Option[String],
                         refreshData: Boolean)(implicit val executionContext: ExecutionContext) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    mode match {
      case NormalMode | CheckMode =>
        getOrCreateOptionalRequest(srn.getOrElse(""), request.psaId.getOrElse("").asInstanceOf[String], refreshData)(request, hc)
      case UpdateMode | CheckUpdateMode =>
        (srn, request.psaId) match {
          case (Some(extractedSrn), Some(psaId)) =>
            getOrCreateOptionalRequest(extractedSrn, psaId.id, refreshData)(request, hc)
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

  private def getOrCreateOptionalRequest[A](srn: String,
                                       psaId: String,
                                       refresh: Boolean)
                                      (implicit request: AuthenticatedRequest[A],
                                       hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    refreshBasedJsFetch(refresh, srn, psaId).map {
      case None => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = None,
        psaId = request.psaId,
        pspId = request.pspId,
        administratorOrPractitioner = request.administratorOrPractitioner
      )
      case Some(data) => OptionalDataRequest(
        request = request.request,
        externalId = request.externalId,
        userAnswers = Some(UserAnswers(data)),
        psaId = request.psaId,
        pspId = request.pspId,
        administratorOrPractitioner = request.administratorOrPractitioner
      )
    }

  private def addMinimalFlagsAndUpdateRepository[A](srn: String,
                                                    jsValue: JsValue,
                                                    psaId: String)
                                                   (implicit hc: HeaderCarrier): Future[JsValue] = {
    minimalPsaConnector.getMinimalFlags(psaId).map { minimalFlags =>
      UserAnswers(jsValue).set(PsaMinimalFlagsId)(minimalFlags).flatMap(
        _.set(SchemeSrnId)(srn)).asOpt.getOrElse(UserAnswers(jsValue)).json
    }
  }

  private def refreshBasedJsFetch[A](refresh: Boolean, srn: String, psaId: String)
                                    (implicit request: AuthenticatedRequest[A],
                                     hc: HeaderCarrier): Future[Option[JsValue]] =
    if (refresh) {
      schemeDetailsConnector
        .getSchemeDetails(psaId, schemeIdType = "srn", srn)
        .flatMap(ua => addMinimalFlagsAndUpdateRepository(srn, ua.json, psaId))
        .map(Some(_))
    } else {
      dataConnector.fetch(request.externalId)
    }
}


case object MissingSchemeNameException extends Exception

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

class RacdacDataRetrievalActionImpl @Inject()(@Racdac dataConnector: UserAnswersCacheConnector,
                                              minimalPsaConnector: MinimalPsaConnector,
                                              schemeDetailsConnector: SchemeDetailsConnector)(implicit ec: ExecutionContext) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: Option[String], refreshData: Boolean): DataRetrieval = {
    new RacdacDataRetrievalImpl(dataConnector, mode, minimalPsaConnector, schemeDetailsConnector, srn: Option[String],refreshData: Boolean)
  }
}


trait DataRetrievalAction {
  def apply(mode: Mode = NormalMode, srn: Option[String] = None, refreshData: Boolean = false): DataRetrieval
}
