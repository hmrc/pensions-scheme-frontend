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
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyCacheConnector @Inject()(
                                        config: FrontendAppConfig,
                                        http: WSClient
                                      ) {

  def dropCollection(collectionName: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    http
      .url(url(collectionName))
      .withHttpHeaders(headers(hc): _*)
      .delete()
      .map(_ => Ok)
  }

  protected def url(collectionName: String) = s"${config.pensionsSchemeUrl}/test-only/$collectionName"
}
