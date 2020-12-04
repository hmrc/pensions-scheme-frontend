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
import connectors.{SchemeDetailsConnector, SchemeDetailsReadOnlyCacheConnector}
import identifiers.SchemeSrnId
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PspDataRetrievalImpl @Inject()(val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                     schemeDetailsConnector: SchemeDetailsConnector,
                                     srn: String
                                       )(implicit val executionContext: ExecutionContext) extends PspDataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    viewConnector.fetch(request.externalId).flatMap {
      case None =>
          val pspId = request.pspId.getOrElse(throw IdNotFound("PspIdNotFound")).id
          schemeDetailsConnector.getPspSchemeDetails(pspId, srn).map { ua =>
            val userAnswers = ua.set(SchemeSrnId)(srn).asOpt.getOrElse(ua)
            OptionalDataRequest(request.request, request.externalId, Some(userAnswers), request.psaId, request.pspId, viewOnly = true)
          }

      case Some(data) =>
        Future.successful(OptionalDataRequest(
          request.request, request.externalId, Some(UserAnswers(data)), request.psaId, request.pspId, viewOnly = true))
    }
  }
}

@ImplementedBy(classOf[PspDataRetrievalImpl])
trait PspDataRetrieval extends ActionTransformer[AuthenticatedRequest, OptionalDataRequest]

class PspDataRetrievalActionImpl @Inject()(
                                        viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                        schemeDetailsConnector: SchemeDetailsConnector
                                       )(implicit ec: ExecutionContext) extends PspDataRetrievalAction {
  override def apply(srn: String): PspDataRetrieval = {
    new PspDataRetrievalImpl(viewConnector, schemeDetailsConnector, srn)
  }
}

@ImplementedBy(classOf[PspDataRetrievalActionImpl])
trait PspDataRetrievalAction {
  def apply(srn: String): PspDataRetrieval
}