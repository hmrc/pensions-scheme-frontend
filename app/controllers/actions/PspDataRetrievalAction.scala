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
import connectors.{SchemeDetailsConnector, SchemeDetailsReadOnlyCacheConnector}
import identifiers.racdac.{IsRacDacId, RACDACNameId}
import identifiers.{SchemeNameId, SchemeSrnId}
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import play.api.libs.json.JsValue
import play.api.mvc.ActionTransformer
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class PspDataRetrievalImpl @Inject()(val viewConnector: SchemeDetailsReadOnlyCacheConnector,
                                     schemeDetailsConnector: SchemeDetailsConnector,
                                     srn: String
                                    )(implicit val executionContext: ExecutionContext) extends PspDataRetrieval {

  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    def isSchemeDataForCurrentSRN(data: JsValue): Boolean = (data \ SchemeSrnId.toString).asOpt[String].contains(srn)

    viewConnector.fetch(request.externalId).flatMap {
      case Some(data) if isSchemeDataForCurrentSRN(data) =>
        Future.successful(OptionalDataRequest(
          request.request, request.externalId, Some(UserAnswers(data)), request.psaId, request.pspId, viewOnly = true, request.administratorOrPractitioner))
      case _ =>
        val pspId = request.pspId.getOrElse(throw IdNotFound("PspIdNotFound")).id
        schemeDetailsConnector.getPspSchemeDetails(pspId, srn).map { ua =>
          OptionalDataRequest(request.request, request.externalId, Some(additionalUserAnswers(ua)), request.psaId, request.pspId, viewOnly = true, request.administratorOrPractitioner)
        }
    }
  }

  private def additionalUserAnswers(ua: UserAnswers): UserAnswers =
    (ua.get(IsRacDacId), ua.get(RACDACNameId)) match {
      case (Some(true), None) =>
        val schemeName: String = ua.get(SchemeNameId).getOrElse(throw MissingSchemeNameException)
        ua.set(SchemeSrnId)(srn).flatMap(_.set(RACDACNameId)(schemeName)).asOpt.getOrElse(ua)
      case _ => ua.set(SchemeSrnId)(srn).asOpt.getOrElse(ua)
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
