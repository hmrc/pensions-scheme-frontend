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

package connectors

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.register.SchemeSubmissionResponse
import play.api.Logger
import play.api.http.Status
import play.api.libs.json._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.UserAnswers
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.http.Status._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[PensionsSchemeConnectorImpl])
trait PensionsSchemeConnector {

  def registerScheme(answers: UserAnswers, psaId: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, SchemeSubmissionResponse]]

  def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

  def checkForAssociation(psaId: String, srn: String)
                         (implicit headerCarrier: HeaderCarrier,
                          ec: ExecutionContext, request: RequestHeader): Future[Either[HttpResponse, Boolean]]
}

@Singleton
class PensionsSchemeConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends
  PensionsSchemeConnector {

  def registerScheme(answers: UserAnswers, psaId: String)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, SchemeSubmissionResponse]] = {

    val url = config.registerSchemeUrl

    http.POST[JsValue, HttpResponse](url, answers.json, Seq("psaId" -> psaId)).map { response =>
      response.status match {
        case OK =>
          val json = Json.parse(response.body)

          json.validate[SchemeSubmissionResponse] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          Logger.error("Unable to register Scheme")
          Left(response)
      }
    }
  }

  private def translateExceptions[I](): PartialFunction[Throwable, Future[I]] = {
    case e: BadRequestException if e.getMessage contains "INVALID_PAYLOAD" => Future.failed(new InvalidPayloadException)
  }

  private def logExceptions[I](msg: String): PartialFunction[Try[I], Unit] = {
    case Failure(t: Throwable) => Logger.error(msg, t)
  }

  def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {

    val url = config.updateSchemeDetailsUrl

    http.POST[JsValue, HttpResponse](url, answers.json, Seq("psaId" -> psaId, "pstr" -> pstr))
  }

  def checkForAssociation(psaId: String, srn: String)
                         (implicit headerCarrier: HeaderCarrier,
                          ec: ExecutionContext, request: RequestHeader): Future[Either[HttpResponse, Boolean]] = {
    val headers: Seq[(String, String)] =
      Seq(("psaId", psaId), ("schemeReferenceNumber", srn), ("Content-Type", "application/json"))

    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers: _*)

    http.GET[HttpResponse](config.checkAssociationUrl)(implicitly, hc, implicitly).map { response =>
      response.status match {
        case OK =>
          val json = Json.parse(response.body)

          json.validate[Boolean] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          Logger.error(response.body)
          Left(response)
      }
    }
  }
}

sealed trait RegisterSchemeException extends Exception

class InvalidPayloadException extends RegisterSchemeException
