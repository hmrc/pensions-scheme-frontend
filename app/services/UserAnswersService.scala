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

import com.google.inject.{ImplementedBy, Inject}
import connectors.{SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.{EstablishersOrTrusteesChangedId, InsuranceDetailsChangedId, TypedIdentifier}
import javax.inject.Singleton
import models._
import models.requests.DataRequest
import play.api.libs.json.{Format, JsSuccess, JsValue}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def subscriptionCacheConnector: SubscriptionCacheConnector

  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector

  case class MissingSrnNumber() extends Exception

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                      (implicit
                                       fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]
                                      ): Future[JsValue]

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A,
                                       changeId: TypedIdentifier[Boolean])
                                      (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode =>
        srn match {
          case Some(srnId) =>
            val answers = request.userAnswers.set(id)(value).flatMap {
              _.set(changeId)(true)
            }.asOpt.getOrElse(request.userAnswers)
            updateSchemeCacheConnector.upsert(srnId, answers.json)
          case _ => Future.failed(throw new MissingSrnNumber)
        }
    }

  def remove[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode =>
        srn match {
          case Some(srnId) =>
            updateSchemeCacheConnector.remove(srnId, id)
          case _ => Future.failed(throw new MissingSrnNumber)
        }
    }

  def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                  request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode =>
        srn match {
          case Some(srnId) =>
            updateSchemeCacheConnector.upsert(srnId, value)
          case _ => Future.failed(throw new MissingSrnNumber)
        }
    }
}

@Singleton
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode: Mode, srn: Option[String], id: I, value: A, EstablishersOrTrusteesChangedId)
}

@Singleton
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode: Mode, srn: Option[String], id: I, value: A, InsuranceDetailsChangedId)
}
