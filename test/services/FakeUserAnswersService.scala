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

package services

import config.FrontendAppConfig
import connectors._
import identifiers.TypedIdentifier
import models.address.Address
import models.requests.DataRequest
import models.{Mode, OptionalSchemeReferenceNumber}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Result}
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

trait FakeUserAnswersService extends UserAnswersService with Matchers with OptionValues {

  override protected def subscriptionCacheConnector: SubscriptionCacheConnector = FakeSubscriptionCacheConnector.getConnector
  override protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector = FakeUpdateCacheConnector.getConnector
  override protected def lockConnector: PensionSchemeVarianceLockConnector = FakeLockConnector.getConnector
  override protected def viewConnector: SchemeDetailsReadOnlyCacheConnector = FakeReadOnlyCacheConnector.getConnector
    override val appConfig: FrontendAppConfig =  FakeFrontendAppConfig.getConfig

  private val data: mutable.HashMap[String, JsValue] = mutable.HashMap()
  private val removed: mutable.ListBuffer[String] = mutable.ListBuffer()

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
  {
    data += (id.toString -> Json.toJson(value))
    data += ("userAnswer" -> request.userAnswers.set(id)(value).asOpt.value.json)
    Future.successful(Json.obj())
  }

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I, value: A, changeId: TypedIdentifier[Boolean])
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
  {
    data += (id.toString -> Json.toJson(value))
    data += ("userAnswer" -> request.userAnswers.set(id)(value).asOpt.value.json)
    Future.successful(Json.obj())
  }


  override def setExistingAddress(mode: Mode, id: TypedIdentifier[Address], userAnswers: UserAnswers): UserAnswers = {
    userAnswers.get(id).fold(userAnswers) {
      address =>
        data += ("fakeExistingAddressId" -> Json.toJson(address))
        userAnswers
    }
  }

  override def upsert(mode: Mode, srn: OptionalSchemeReferenceNumber, value: JsValue)
            (implicit ec: ExecutionContext, hc: HeaderCarrier,
             request: DataRequest[AnyContent]): Future[JsValue] = {
    data += ("userAnswer" -> Json.toJson(value))
    Future.successful(value)
  }

  override def remove[I <: TypedIdentifier[?]](mode: Mode, srn: OptionalSchemeReferenceNumber, id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] = {
    removed += id.toString
    Future.successful(request.userAnswers.json)
  }

  def fetch(cacheId: String): Future[Option[JsValue]] = Future.successful(Some(Json.obj()))

  def lastUpdated(cacheId: String): Future[Option[JsValue]] = Future.successful(Some(Json.obj()))

  def userAnswer: UserAnswers = UserAnswers(data.getOrElse("userAnswer", Json.obj()))

  def verify[A, I <: TypedIdentifier[A]](id: I, value: A)(implicit fmt: Format[A]): Unit = {
    data should contain(id.toString -> Json.toJson(value))
  }

  def verifyNot(id: TypedIdentifier[?]): Unit = data should not contain key(id.toString)

  def verifyRemoved(id: TypedIdentifier[?]): Unit = removed should contain(id.toString)

  def removeAll(cacheId: String): Future[Result] = Future.successful(Ok)

  def reset(): Unit = {
    data.clear()
    removed.clear()
  }
}

object FakeUserAnswersService extends FakeUserAnswersService


