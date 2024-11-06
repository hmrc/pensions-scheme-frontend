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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.FeatureToggle
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class FeatureToggleConnector @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends HttpResponseHelper {

  private val logger  = Logger(classOf[FeatureToggleConnector])

  def get(name: String
         )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FeatureToggle] = {
    val endPoint = url"${config.featureToggleUrl(name)}"

    httpClientV2.get(endPoint)
      .execute[HttpResponse]
      .map { response =>
          response.status match {
            case OK => response.json.as[FeatureToggle]
            case _ => handleErrorResponse("GET", endPoint.toString)(response)
          }
      } andThen {
        case Failure(t: Throwable) => logger.warn("Unable to get toggle value", t)
      }
  }
}
