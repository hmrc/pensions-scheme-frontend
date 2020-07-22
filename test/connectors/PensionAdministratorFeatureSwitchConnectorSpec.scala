/*
 * Copyright 2020 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{AsyncWordSpec, MustMatchers, OptionValues}
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class PensionAdministratorFeatureSwitchConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with OptionValues {

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  protected implicit val hc: HeaderCarrier = HeaderCarrier()

  protected lazy val connector: FeatureSwitchConnector = injector.instanceOf[PensionAdministratorFeatureSwitchConnectorImpl]
  private val featureSwitch = "test-switch"

  "toggle On " must {

    val toggleOnUrl = s"/pension-administrator/test-only/toggle-on/$featureSwitch"

    "return No Content when backend toggles on successfully" in {
      server.stubFor(
        get(urlEqualTo(toggleOnUrl))
          .willReturn(
            noContent()
          )
      )
      connector.toggleOn(featureSwitch).map(response =>
        response mustEqual true
      )
    }

    "return Expectation Failed when backend can't toggle on" in {
      server.stubFor(
        get(urlEqualTo(toggleOnUrl))
          .willReturn(
            aResponse.withStatus(EXPECTATION_FAILED)
          )
      )
      connector.toggleOn(featureSwitch).map(response =>
        response mustEqual false
      )
    }
  }

  "toggle Off " must {

    val toggleOffUrl = s"/pension-administrator/test-only/toggle-off/$featureSwitch"

    "return No Content when backend toggles off successfully" in {
      server.stubFor(
        get(urlEqualTo(toggleOffUrl))
          .willReturn(
            noContent()
          )
      )
      connector.toggleOff(featureSwitch).map(response =>
        response mustEqual true
      )
    }

    "return Expectation Failed when backend can't toggle off" in {
      server.stubFor(
        get(urlEqualTo(toggleOffUrl))
          .willReturn(
            aResponse.withStatus(EXPECTATION_FAILED)
          )
      )
      connector.toggleOff(featureSwitch).map(response =>
        response mustEqual false
      )
    }
  }

  "reset " must {

    val resetUrl = s"/pension-administrator/test-only/reset/$featureSwitch"

    "return ok when backend resets successfully" in {
      server.stubFor(
        get(urlEqualTo(resetUrl))
          .willReturn(
            noContent()
          )
      )
      connector.reset(featureSwitch).map(response =>
        response mustEqual true
      )
    }

    "return Expectation Failed when backend can't reset" in {
      server.stubFor(
        get(urlEqualTo(resetUrl))
          .willReturn(
            aResponse.withStatus(EXPECTATION_FAILED)
          )
      )
      connector.reset(featureSwitch).map(response =>
        response mustEqual false
      )
    }
  }

  "get" must {

    val getUrl = s"/pension-administrator/test-only/get/$featureSwitch"

    "return correct toggle value when get called successfully" in {

      server.stubFor(
        get(urlEqualTo(getUrl))
          .willReturn(
            okJson("true")
          )
      )
      connector.get(featureSwitch).map { response =>
        response mustEqual Some(true)
      }
    }

    "return none when toggle is defined" in {
      server.stubFor(
        get(urlEqualTo(getUrl))
          .willReturn(
            noContent()
          )
      )
      connector.get(featureSwitch).map(response =>
        response mustEqual None
      )
    }
  }
}
