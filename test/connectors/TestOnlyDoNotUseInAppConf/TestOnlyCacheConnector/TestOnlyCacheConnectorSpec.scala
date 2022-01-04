/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.TestOnlyDoNotUseInAppConf.TestOnlyCacheConnector

import com.github.tomakehurst.wiremock.client.WireMock.{delete, ok, urlEqualTo}
import connectors.TestOnlyCacheConnector
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class TestOnlyCacheConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with OptionValues {

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  protected def url(collectionName: String) = s"/test-only/$collectionName"

  protected lazy val connector: TestOnlyCacheConnector = injector.instanceOf[TestOnlyCacheConnector]

  "dropCollection" must {
    "drop the collection " in {
      server.stubFor(delete(urlEqualTo(url("psa-name"))).
        willReturn(ok)
      )
      connector.dropCollection("psa-name").map {
        _ mustEqual Ok
      }
    }
  }

}
