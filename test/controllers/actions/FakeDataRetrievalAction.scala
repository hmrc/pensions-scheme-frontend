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

import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import models.{Mode, NormalMode}
import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.PsaId
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(json: Option[JsValue], mode: Mode = NormalMode, viewOnly: Boolean = false) extends DataRetrievalAction {
  override def apply(mode: Mode, srn: Option[String], refreshData: Boolean = false): DataRetrieval = new FakeDataRetrieval(json, mode, viewOnly)
}

class FakeDataRetrieval(optionalJson: Option[JsValue], mode: Mode = NormalMode, viewOnly: Boolean = false) extends DataRetrieval {
  override protected def transform[A](request: AuthenticatedRequest[A]): Future[OptionalDataRequest[A]] =
    optionalJson match {
      case None =>
        Future.successful(OptionalDataRequest(request.request, request.externalId, None, Some(PsaId("A0000000")), viewOnly = viewOnly))

      case Some(json) =>
        Future.successful(OptionalDataRequest(request.request, request.externalId,
          Some(UserAnswers(json)), Some(PsaId("A0000000")), viewOnly = viewOnly))
    }

  override protected def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
