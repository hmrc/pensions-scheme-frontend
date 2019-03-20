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

package services

import connectors.{FakeUpdateSchemeCacheConnector, FakeUserAnswersCacheConnector, UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.TypedIdentifier
import models.Mode
import org.scalatest.Matchers
import play.api.libs.json.{Format, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class FakeUserAnswersService extends UserAnswersService with Matchers {

  private val data: mutable.HashMap[String, JsValue] = mutable.HashMap()

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, cacheId: String, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
  {
    data += (id.toString -> Json.toJson(value))
    Future.successful(Json.obj())
  }

  def verify[A, I <: TypedIdentifier[A]](id: I, value: A)(implicit fmt: Format[A]): Unit = {
    data should contain(id.toString -> Json.toJson(value))
  }

  def verifyNot(id: TypedIdentifier[_]): Unit = {
    data should not contain key(id.toString)
  }
  def reset(): Unit = {
    data.clear()
  }

  override protected def userAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

  override protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector = FakeUpdateSchemeCacheConnector
}

object FakeUserAnswersService extends FakeUserAnswersService