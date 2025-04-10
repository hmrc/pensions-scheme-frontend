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

package connectors

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.SchemeReferenceNumber
import models.enumerations.SchemeJourneyType
import models.register.SchemeSubmissionResponse
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.writeableOf_JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.{HttpResponseHelper, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PensionsSchemeConnectorImpl])
trait PensionsSchemeConnector {

  def registerScheme(answers: UserAnswers, psaId: String, schemeJourneyType: SchemeJourneyType.Name
                    )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse]

  def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers
                         )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]

  def checkForAssociation(userId: String, srn: SchemeReferenceNumber, isPsa: Boolean = true)
                         (implicit headerCarrier: HeaderCarrier,
                          ec: ExecutionContext, request: RequestHeader): Future[Either[HttpResponse, Boolean]]
}

@Singleton
class PensionsSchemeConnectorImpl @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends PensionsSchemeConnector with HttpResponseHelper {

  private val logger  = Logger(classOf[PensionsSchemeConnectorImpl])

  def registerScheme(answers: UserAnswers, psaId: String, schemeJourneyType: SchemeJourneyType.Name
                    )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {

    val url = url"${config.registerSchemeUrl(schemeJourneyType)}"
    val headers = Seq("psaId" -> psaId)

    httpClientV2.post(url)
      .withBody(answers.json)
      .setHeader(headers*)
      .execute[HttpResponse].map { response =>
        response.status match {
          case OK =>
            val json = Json.parse(response.body)

            json.validate[SchemeSubmissionResponse] match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("POST", url.toString)(response)
        }
      }
  }

  def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers
                         )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {

    val url = url"${config.updateSchemeDetailsUrl}"
    val headers = Seq("psaId" -> psaId, "pstr" -> pstr)

    httpClientV2.post(url)
      .withBody(answers.json)
      .setHeader(headers*)
      .execute[HttpResponse].map { response =>
        response.status match {
          case OK => Right(())
          case _ =>
            handleErrorResponse("POST", url.toString)(response)
        }
      }
  }

  def checkForAssociation(userId: String, srn: SchemeReferenceNumber, isPsa: Boolean)
                         (implicit headerCarrier: HeaderCarrier,
                          ec: ExecutionContext, request: RequestHeader): Future[Either[HttpResponse, Boolean]] = {
    val headers: Seq[(String, String)] =
      Seq((if(isPsa) "psaId" else "pspId", userId), ("schemeReferenceNumber", srn.id), ("Content-Type", "application/json"))
    val url = url"${config.checkAssociationUrl}"
    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(headers*)

    httpClientV2.get(url)(hc)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            val json = Json.parse(response.body)

            json.validate[Boolean] match {
              case JsSuccess(value, _) => Right(value)
              case JsError(errors) => throw JsResultException(errors)
            }
          case _ =>
            logger.error(response.body)
            Left(response)
        }
      }
  }
}

sealed trait RegisterSchemeException extends Exception

class InvalidPayloadException extends RegisterSchemeException
