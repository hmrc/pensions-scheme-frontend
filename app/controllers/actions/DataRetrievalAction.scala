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

package controllers.actions


import com.google.inject.{ImplementedBy, Inject}
import connectors.{PensionSchemeVarianceLockConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import models._
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.libs.json.JsValue
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalImpl(dataConnector: UserAnswersCacheConnector,
                        viewConnector: SubscriptionCacheConnector,
                        updateConnector: UpdateSchemeCacheConnector,
                        lockConnector: PensionSchemeVarianceLockConnector,
                        mode: Mode = NormalMode,
                        srn: Option[String] = None) extends DataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    mode match {
      case NormalMode | CheckMode => getOptionalRequest(dataConnector.fetch(request.externalId))(request)

      case UpdateMode | CheckUpdateMode =>
        srn.map { srn =>
          lockConnector.getLock(request.psaId.id, srn).flatMap {
            case Some(SchemeVarianceLock(true, true)) => getOptionalRequest(updateConnector.fetch(srn))(request)
            case _ => placeLockAndGetRequest(srn)(request, implicitly)
          }
        }.getOrElse(Future(OptionalDataRequest(request.request, request.externalId, None, request.psaId)))
    }
  }

  def placeLockAndGetRequest[A](srn: String)(implicit request: AuthenticatedRequest[A], hc: HeaderCarrier): Future[OptionalDataRequest[A]] =
    viewConnector.fetch(request.externalId).flatMap {
      case None => Future(OptionalDataRequest(request.request, request.externalId, None, request.psaId))
      case Some(data) =>
        lockConnector.lock(request.psaId.id, srn).flatMap {
          case SchemeVarianceLock(true, true) => updateConnector.upsert(srn, UserAnswers(data).json).map { _ =>
            OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(data)), request.psaId)
          }
          case _ => getOptionalRequest(viewConnector.fetch(request.externalId))(request)
        }
    }

  def getOptionalRequest[A](f: Future[Option[JsValue]])(implicit request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    f.map {
      case None => OptionalDataRequest(request.request, request.externalId, None, request.psaId)
      case Some(data) => OptionalDataRequest(request.request, request.externalId, Some(UserAnswers(data)), request.psaId)

    }
}

@ImplementedBy(classOf[DataRetrievalImpl])
trait DataRetrieval extends ActionTransformer[AuthenticatedRequest, OptionalDataRequest]

class DataRetrievalActionImpl @Inject()(dataConnector: UserAnswersCacheConnector,
                                        viewConnector: SubscriptionCacheConnector,
                                        updateConnector: UpdateSchemeCacheConnector,
                                        lockConnector: PensionSchemeVarianceLockConnector
                                       ) extends DataRetrievalAction {
  override def apply(mode: Mode = NormalMode,
                     srn: Option[String] = None): DataRetrieval =
    new DataRetrievalImpl(dataConnector, viewConnector, updateConnector, lockConnector, mode, srn)
}

@ImplementedBy(classOf[DataRetrievalActionImpl])
trait DataRetrievalAction {
  def apply(mode: Mode = NormalMode, srn: Option[String] = None): DataRetrieval
}
