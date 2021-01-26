/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.ImplementedBy
import config.FrontendAppConfig
import javax.inject.{Inject, Singleton}
import models.{Lock, SchemeVariance}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[PensionSchemeVarianceLockConnectorImpl])
trait PensionSchemeVarianceLockConnector {

  def lock(psaId: String, srn: String)
          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Lock]

  def getLock(psaId: String, srn: String)
             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SchemeVariance]]

  def getLockByPsa(psaId: String)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SchemeVariance]]

  def isLockByPsaIdOrSchemeId(psaId: String, srn: String)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Lock]]

  def releaseLock(psaId: String, srn: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]

}

@Singleton
class PensionSchemeVarianceLockConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig)
  extends PensionSchemeVarianceLockConnector {

  private val logger  = Logger(classOf[PensionSchemeVarianceLockConnectorImpl])

  override def lock(psaId: String, srn: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Lock] = {

    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("psaId" -> psaId, "srn" -> srn)

    val url = s"${config.updateSchemeDetailsUrl}/lock"

    http.POSTEmpty[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>

      response.status match {
        case OK =>
          Json.parse(response.body).validate[Lock] match {
            case JsSuccess(value, _) => value
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          throw new HttpException(response.body, response.status)
      }
    } andThen logExceptions("Unable to get the lock")
  }

  override def getLock(psaId: String, srn: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SchemeVariance]] = {

    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("psaId" -> psaId, "srn" -> srn)

    val url = s"${config.updateSchemeDetailsUrl}/getLock"

    http.GET[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>

      response.status match {
        case NOT_FOUND =>
          None
        case OK =>
          Json.parse(response.body).validate[SchemeVariance] match {
            case JsSuccess(value, _) => Some(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          throw new HttpException(response.body, response.status)
      }
    } andThen logExceptions("Unable to find the lock")
  }

  override def getLockByPsa(psaId: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SchemeVariance]] = {

    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("psaId" -> psaId)

    val url = s"${config.updateSchemeDetailsUrl}/get-lock-by-psa"

    http.GET[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>

      response.status match {
        case NOT_FOUND =>
          None
        case OK =>
          Json.parse(response.body).validate[SchemeVariance] match {
            case JsSuccess(value, _) => Some(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          throw new HttpException(response.body, response.status)
      }
    } andThen logExceptions("Unable to find the lock")
  }


  override def isLockByPsaIdOrSchemeId(psaId: String, srn: String)
                                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Lock]] = {

    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("psaId" -> psaId, "srn" -> srn)

    val url = s"${config.updateSchemeDetailsUrl}/isLockByPsaOrScheme"

    http.GET[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>

      response.status match {
        case NOT_FOUND =>
          None
        case OK =>
          Json.parse(response.body).validate[Lock] match {
            case JsSuccess(value, _) => Some(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          throw new HttpException(response.body, response.status)
      }
    } andThen logExceptions("Unable to find the lock")
  }

  private def logExceptions[I](msg: String): PartialFunction[Try[I], Unit] = {
    case Failure(t: Throwable) => logger.error(msg, t)
  }

  override def releaseLock(psaId: String, srn: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    implicit val headerCarrier: HeaderCarrier = hc.withExtraHeaders("psaId" -> psaId, "srn" -> srn)

    val url = s"${config.updateSchemeDetailsUrl}/release-lock"

    http.DELETE[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>

      response.status match {
        case OK => {}
        case _ =>
          throw new HttpException(response.body, response.status)
      }
    } andThen logExceptions("Unable to release the lock")
  }

}
