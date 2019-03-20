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

import connectors.{UpdateSchemeCacheConnector, UserAnswersCacheConnector}
import identifiers.{EstablishersOrTrusteesChangedId, InsuranceDetailsChangedId, TypedIdentifier}
import models._
import play.api.libs.json.{Format, JsValue}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def userAnswersCacheConnector: UserAnswersCacheConnector
  protected def updateSchemeCacheConnector: UpdateSchemeCacheConnector

  def save[A, I <: TypedIdentifier[A]](mode: Mode, cacheId: String, id: I, value: A,
                                                  changeId: TypedIdentifier[Boolean])
                                      (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => userAnswersCacheConnector.save(cacheId, id, value)
      case UpdateMode | CheckUpdateMode => updateSchemeCacheConnector.save(cacheId, id, value).flatMap { _ =>
        updateSchemeCacheConnector.save(cacheId, changeId, true)
      }
    }

  def save[A, I <: TypedIdentifier[A]](mode: Mode, cacheId: String, id: I, value: A)
                                      (implicit
                                       fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier
                                      ): Future[JsValue]


}

class UserAnswersServiceImpl(override val userAnswersCacheConnector: UserAnswersCacheConnector,
                             override val updateSchemeCacheConnector: UpdateSchemeCacheConnector) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, cacheId: String, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    save(mode: Mode, cacheId: String, id: I, value: A, EstablishersOrTrusteesChangedId)
}

class UserAnswersServiceInsuranceImpl(override val userAnswersCacheConnector: UserAnswersCacheConnector,
                                      override val updateSchemeCacheConnector: UpdateSchemeCacheConnector) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, cacheId: String, id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    save(mode: Mode, cacheId: String, id: I, value: A, InsuranceDetailsChangedId)
}
