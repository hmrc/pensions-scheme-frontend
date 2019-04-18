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

import config.FrontendAppConfig
import connectors.{PensionSchemeVarianceLockConnector, SubscriptionCacheConnector, UpdateSchemeCacheConnector}
import identifiers.{EstablishersOrTrusteesChangedId, InsuranceDetailsChangedId, TypedIdentifier}
import javax.inject.{Inject, Singleton}
import models.requests.DataRequest
import models.{Mode, _}
import play.api.libs.json.{Format, JsResultException, JsValue, Json}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def subscriptionCacheConnector: SubscriptionCacheConnector

  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector
  protected def lockConnector: PensionSchemeVarianceLockConnector
  protected def appConfig: FrontendAppConfig

  case class MissingSrnNumber() extends Exception

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                      (implicit fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]
                                      ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.save(_, id, value))
    }

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A,
                                       changeId: TypedIdentifier[Boolean])
                                      (implicit fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode =>
        val answers = request.userAnswers
          .set(id)(value).flatMap {
          _.set(changeId)(true)
        }.asOpt.getOrElse(request.userAnswers)

       lockAndCall(srn, updateSchemeCacheConnector.upsert(_, answers.json))
    }

  def remove[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.remove(_, id))
    }

  def removeAll[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.remove(_, id))
    }

  def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                  request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode => lockAndCall(srn, updateSchemeCacheConnector.upsert(_, value))
    }

  def upsert(mode: Mode, srn: Option[String], value: JsValue,
             changeId: TypedIdentifier[Boolean])(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                              request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.upsert(request.externalId, value)
      case UpdateMode | CheckUpdateMode =>
        val answers = UserAnswers(value)
          .set(changeId)(true).asOpt.getOrElse(UserAnswers(value))
        lockAndCall(srn, updateSchemeCacheConnector.upsert(_, answers.json))
    }

  def lockAndCall(srn: Option[String], f: String => Future[JsValue])(implicit
                                                                     ec: ExecutionContext,
                                                                     hc: HeaderCarrier,
                                                                     request: DataRequest[AnyContent]
  ): Future[JsValue] = srn match {
    case Some(srnId) => lockConnector.lock(request.psaId.id, srnId).flatMap {
          case VarianceLock => f(srnId)
          case _ => Future(Json.obj())
      }

    case _ => Future.failed(throw new MissingSrnNumber)
  }

  def setCompleteFlag(mode: Mode, srn: Option[String], id: TypedIdentifier[Boolean], userAnswers: UserAnswers, value: Boolean)
                     (implicit fmt: Format[Boolean], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[UserAnswers] = {

    userAnswers.set(id)(value).fold(
      invalid => Future.failed(JsResultException(invalid)),
      valid => Future.successful(valid)
    )

    mode match {
      case NormalMode | CheckMode => subscriptionCacheConnector.save(request.externalId, id, value) map UserAnswers
      case UpdateMode | CheckUpdateMode => srn match {
        case Some(srnId) => lockConnector.lock(request.psaId.id, srnId).flatMap {
          case VarianceLock => save(mode, srn, id, value) map UserAnswers
          case _ => Future.successful(request.userAnswers)
        }

        case _ =>
          case class MissingSrnNumber() extends Exception
          Future.failed(throw new MissingSrnNumber)
      }
    }
  }
}

@Singleton
class UserAnswersServiceEstablishersAndTrusteesImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                       override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                       override val lockConnector: PensionSchemeVarianceLockConnector,
                                       override val appConfig: FrontendAppConfig
                                      ) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, EstablishersOrTrusteesChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, EstablishersOrTrusteesChangedId)
}

@Singleton
class UserAnswersServiceInsuranceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode, srn, id, value, InsuranceDetailsChangedId)

  override def upsert(mode: Mode, srn: Option[String], value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier,
                                                                       request: DataRequest[AnyContent]): Future[JsValue] =
    upsert(mode, srn, value, InsuranceDetailsChangedId)
}

@Singleton
class UserAnswersServiceImpl @Inject()(override val subscriptionCacheConnector: SubscriptionCacheConnector,
                                                override val updateSchemeCacheConnector: UpdateSchemeCacheConnector,
                                                override val lockConnector: PensionSchemeVarianceLockConnector,
                                                override val appConfig: FrontendAppConfig
                                               ) extends UserAnswersService
