/*
 * Copyright 2023 HM Revenue & Customs
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

class SchemeDetailsReadOnlyCacheConnectorSpec extends CacheConnectorBehaviours {

  override protected def url(id: String) = s"/pensions-scheme/journey-cache/scheme-details/$id"

  override protected def lastUpdatedUrl(id: String) = s"/pensions-scheme/journey-cache/scheme-details/$id/lastUpdated"

  protected def connector: SchemeDetailsReadOnlyCacheConnector = injector.instanceOf[SchemeDetailsReadOnlyCacheConnector]

  "SchemeDetailsReadOnlyCacheConnector" when {

    behave like cacheConnector(connector)

  }
}
