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
import connectors.CacheConnector.headers
import play.api.libs.json._
import uk.gov.hmrc.http._
import play.api.http.Status._
import play.api.mvc.Results._
import play.api.mvc.Result
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

class SessionDataCacheConnector @Inject()(config: FrontendAppConfig, httpClientV2: HttpClientV2) {

  private def url(cacheId: String) = url"${config.pensionsAdministratorUrl}/pension-administrator/journey-cache/session-data/$cacheId"

  def fetch(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] =
    httpClientV2.get(url(id))
      .setHeader(headers(hc): _*)
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NOT_FOUND => Future.successful(None)
          case OK => Future.successful(Some(Json.parse(response.body)))
          case _ => Future.failed(new HttpException(response.body, response.status))
        }
      }

  def removeAll(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] =
    httpClientV2.delete(url(id))
      .setHeader(headers(hc): _*)
      .execute[HttpResponse]
      .map(_ => Ok)

}
