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
import uk.gov.hmrc.http.StringContextOps
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Singleton

@Singleton
class SubscriptionCacheConnector @Inject()(
                                            override val config: FrontendAppConfig,
                                            override val httpClientV2: HttpClientV2
                                          ) extends CacheConnector {

  override protected def url(id: String) =
    url"${config.pensionsSchemeUrl}/pensions-scheme/journey-cache/scheme-subscription/$id"

  override protected def lastUpdatedUrl(id: String) =
    url"${config.pensionsSchemeUrl}/pensions-scheme/journey-cache/scheme-subscription/$id/lastUpdated"
}
