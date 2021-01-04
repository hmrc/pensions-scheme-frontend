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

class SubscriptionCacheConnectorSpec extends CacheConnectorBehaviours {

  override protected def url(id: String): String = s"/pensions-scheme/journey-cache/scheme-subscription/$id"

  override protected def lastUpdatedUrl(id: String) = s"/pensions-scheme/journey-cache/scheme-subscription/$id/lastUpdated"

  protected def connector(): SubscriptionCacheConnector = injector.instanceOf[SubscriptionCacheConnector]

  "CacheConnector" when {

    behave like cacheConnector(connector)
    behave like cacheConnectorWithLastUpdate(connector)

  }
}
