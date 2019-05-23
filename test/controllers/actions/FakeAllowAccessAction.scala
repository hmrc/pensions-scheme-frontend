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

import connectors.{PensionsSchemeConnector, SchemeDetailsReadOnlyCacheConnector}
import models.register.SchemeSubmissionResponse
import models.requests.OptionalDataRequest
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FakeAllowAccessAction(srn: Option[String], pensionsSchemeConnector: PensionsSchemeConnector,
                            schemeDetailsReadOnlyCacheConnector:SchemeDetailsReadOnlyCacheConnector) extends AllowAccessAction(srn, pensionsSchemeConnector, schemeDetailsReadOnlyCacheConnector) {
  override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = Future.successful(None)
}

case class FakeAllowAccessProvider(srn: Option[String] = None,
                                   pensionsSchemeConnector: Option[PensionsSchemeConnector] = None,
                                   schemeDetailsReadOnlyCacheConnector: Option[SchemeDetailsReadOnlyCacheConnector] = None
                                  ) extends AllowAccessActionProvider with MockitoSugar{
  override def apply(srn: Option[String]): AllowAccessAction = {
    val psc = pensionsSchemeConnector match {
      case None =>
        new PensionsSchemeConnector {
          override def registerScheme(answers: UserAnswers, psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = ???

          override def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = ???

          override def checkForAssociation(psaId: String, srn: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[Boolean] = Future.successful(true)
        }
      case Some(psc) => psc
    }

    val sdrocc = schemeDetailsReadOnlyCacheConnector match {
      case None =>
        val s = mock[SchemeDetailsReadOnlyCacheConnector]
        when(s.fetch(any())(any(),any()))
          .thenReturn(Future.successful(None))
        s
      case Some(s) => s
    }

    new FakeAllowAccessAction(srn, psc, sdrocc)
  }
}

