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


import base.SpecBase.controllerComponents
import models.AuthEntity
import models.AuthEntity.PSA
import models.requests.AuthenticatedRequest
import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import uk.gov.hmrc.domain.{PsaId, PspId}

import scala.concurrent.{ExecutionContext, Future}

object FakeAuthAction extends AuthAction {
  override def apply(authEntity: Option[AuthEntity] = Some(PSA)): Auth = new FakeAuth
  val externalId: String = "id"
}

class FakeAuth extends Auth {
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(AuthenticatedRequest(request, "id", Some(PsaId("A0000000")), Some(PspId("00000000"))))

  val parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
