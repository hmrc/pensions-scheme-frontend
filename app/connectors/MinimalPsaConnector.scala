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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.{MinimalPSA, PSAMinimalFlags}
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http._
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[MinimalPsaConnectorImpl])
trait MinimalPsaConnector {

  def getMinimalFlags(psaId: String
                     )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PSAMinimalFlags]

  def getMinimalPsaDetails(psaId: String
                          )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalPSA]

  def getPsaNameFromPsaID(psaId: String
                         )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]]
}

class MinimalPsaConnectorImpl @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends MinimalPsaConnector with HttpResponseHelper {

  private val logger  = Logger(classOf[MinimalPsaConnectorImpl])

  override def getMinimalPsaDetails(psaId: String
                                   )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalPSA] = {
    val psaHc = hc.withExtraHeaders("psaId" -> psaId)
    val url = url"${config.minimalPsaDetailsUrl}"

    httpClientV2.get(url)(psaHc)
      .execute[HttpResponse].map { response =>
        response.status match {
          case OK =>
            Json.parse(response.body).validate[MinimalPSA] match {
              case JsSuccess(value, _) => value
              case JsError(errors) => throw JsResultException(errors)
            }
          case FORBIDDEN if response.body.contains(delimitedErrorMsg) => throw new DelimitedAdminException
          case _ => handleErrorResponse("GET", url.toString)(response)
        }
      } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to invite PSA to administer scheme", t)
    }
  }

  val delimitedErrorMsg: String = "DELIMITED_PSAID"

  override def getPsaNameFromPsaID(psaId: String)
                                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    getMinimalPsaDetails(psaId).map { minimalDetails =>
      (minimalDetails.individualDetails, minimalDetails.organisationName) match {
        case (Some(individual), None) => Some(individual.fullName)
        case (None, Some(org)) => Some(s"$org")
        case _ => None
      }
    }
  }

  override def getMinimalFlags(psaId: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PSAMinimalFlags] = {
    val psaHc = hc.withExtraHeaders("psaId" -> psaId)
    val url = url"${config.minimalPsaDetailsUrl}"

    httpClientV2.get(url)(psaHc)
      .execute[HttpResponse].map { response =>
        response.status match {
          case OK =>
            val isSuspended = (response.json \ "isPsaSuspended").as[Boolean]
            val isDeceased = (response.json \ "deceasedFlag").as[Boolean]
            val rlsFlag = (response.json \ "rlsFlag").as[Boolean]
            PSAMinimalFlags(isSuspended, isDeceased, rlsFlag)
          case _ => handleErrorResponse("GET", url.toString)(response)
        }
      } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get PSA minimal details", t)
    }
  }

}


class DelimitedAdminException extends
  Exception("The administrator has already de-registered. The minimal details API has returned a DELIMITED PSA response")
