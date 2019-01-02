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

package connectors

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.register.SchemeSubmissionResponse
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[PensionsSchemeConnectorImpl])
trait PensionsSchemeConnector {

  def registerScheme(answers: UserAnswers, psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse]

}

@Singleton
class PensionsSchemeConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends PensionsSchemeConnector {

  def registerScheme(answers: UserAnswers, psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
    val url = config.registerSchemeUrl

    http.POST(url, answers.json, Seq("psaId" -> psaId)).map { response =>
      require(response.status == Status.OK)

      val json = Json.parse(response.body)

      json.validate[SchemeSubmissionResponse] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen logExceptions recoverWith translateExceptions

  }

  private def translateExceptions(): PartialFunction[Throwable, Future[SchemeSubmissionResponse]] = {
    case e: BadRequestException if e.getMessage contains "INVALID_PAYLOAD" => Future.failed(new InvalidPayloadException)
  }

  private def logExceptions(): PartialFunction[Try[SchemeSubmissionResponse], Unit] = {
    case Failure(t: Throwable) => Logger.error("Unable to register Scheme", t)
  }

}

sealed trait RegisterSchemeException extends Exception

class InvalidPayloadException extends RegisterSchemeException
