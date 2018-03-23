/*
 * Copyright 2018 HM Revenue & Customs
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

package connectors

import identifiers.TypedIdentifier
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.{Cleanup, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

trait DataCacheConnector {

  def save[A, I <: TypedIdentifier[A]](id: I, value: A)
                                      (implicit
                                       request: DataRequest[AnyContent],
                                       wrts: Format[A],
                                       cleanup: Cleanup[I],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier
                                      ): Future[UserAnswers] = {
    save(request.externalId, id, value).map(UserAnswers)
  }

  def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                      (implicit
                                       fmt: Format[A],
                                       cleanup: Cleanup[I],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier
                                      ): Future[JsValue]

  def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                     (implicit
                                      cleanup: Cleanup[I],
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier
                                     ): Future[JsValue]

  def fetch(cacheId: String)(implicit
                             ec: ExecutionContext,
                             hc: HeaderCarrier
                            ): Future[Option[JsValue]]

}
