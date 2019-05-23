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

import connectors.PensionsSchemeConnector
import models.register.SchemeSubmissionResponse
import models.requests.OptionalDataRequest
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FakeAllowAccessAction(srn: Option[String], pensionsSchemeConnector: PensionsSchemeConnector) extends AllowAccessAction(srn, pensionsSchemeConnector) {
  override def filter[A](request: OptionalDataRequest[A]): Future[Option[Result]] = Future.successful(None)
}

case class FakeAllowAccessProvider(srn: Option[String] = None, pensionsSchemeConnector: Option[PensionsSchemeConnector] = None) extends AllowAccessActionProvider{
  override def apply(srn: Option[String]): AllowAccessAction = {
    pensionsSchemeConnector match {
      case None =>
        val psc = new PensionsSchemeConnector {
          override def registerScheme(answers: UserAnswers, psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = ???

          override def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = ???

          override def checkForAssociation(psaId: String, srn: String)(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[Boolean] = Future.successful(true)
        }
        new FakeAllowAccessAction(srn, psc)
      case Some(psc) => new FakeAllowAccessAction(srn, psc)
    }

  }
}

