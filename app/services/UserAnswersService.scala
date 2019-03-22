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

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import identifiers.{EstablishersOrTrusteesChangedId, InsuranceDetailsChangedId, TypedIdentifier}
import models._
import models.requests.DataRequest
import play.api.libs.json.{Format, JsValue}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait UserAnswersService {

  protected def userAnswersCacheConnector: UserAnswersCacheConnector

  case class MissingSrnNumber() extends Exception

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A,
                                                  changeId: TypedIdentifier[Boolean])
                                      (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier, request: DataRequest[AnyContent]): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => userAnswersCacheConnector.save(request.externalId, id, value)
      case UpdateMode | CheckUpdateMode =>
        srn match {
          case Some(srnId) =>
            userAnswersCacheConnector(mode).save(srnId, id, value).flatMap { _ =>
              userAnswersCacheConnector(mode).save(srnId, changeId, true)
          }
          case _ => Future.failed(throw new MissingSrnNumber)
        }
    }

  def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                      (implicit
                                       fmt: Format[A],
                                       ec: ExecutionContext,
                                       hc: HeaderCarrier,
                                       request: DataRequest[AnyContent]
                                      ): Future[JsValue]

  def remove[I <: TypedIdentifier[_]](mode: Mode, srn: Option[String], id: I)
                                     (implicit
                                      ec: ExecutionContext,
                                      hc: HeaderCarrier,
                                      request: DataRequest[AnyContent]
                                     ): Future[JsValue] =
    mode match {
      case NormalMode | CheckMode => userAnswersCacheConnector.remove(request.externalId, id)
      case UpdateMode | CheckUpdateMode =>
        srn match {
          case Some(srnId) =>
            userAnswersCacheConnector(mode).remove(srnId, id)
          case _ => Future.failed(throw new MissingSrnNumber)
        }
    }

}

class UserAnswersServiceImpl @Inject()(override val userAnswersCacheConnector: UserAnswersCacheConnector) extends UserAnswersService {

  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode: Mode, srn: Option[String], id: I, value: A, EstablishersOrTrusteesChangedId)
}

class UserAnswersServiceInsuranceImpl @Inject()(override val userAnswersCacheConnector: UserAnswersCacheConnector) extends UserAnswersService {
  override def save[A, I <: TypedIdentifier[A]](mode: Mode, srn: Option[String], id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier,
                                                request: DataRequest[AnyContent]): Future[JsValue] =
    save(mode: Mode, srn: Option[String], id: I, value: A, InsuranceDetailsChangedId)
}
