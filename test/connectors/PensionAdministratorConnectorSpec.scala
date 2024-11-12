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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class PensionAdministratorConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  override protected lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      )
      .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    server.start()
  }

  override def afterEach(): Unit = {
    server.stop()
    super.afterEach()
  }

  "GetPSAEmail" should "return email" ignore {

    server.stubFor(
      get(urlEqualTo("/pension-administrator/get-email"))
        .willReturn(
          ok("e@mail.com")
        )
    )

    val connector = injector.instanceOf[PensionAdministratorConnector]

    connector.getPSAEmail map { email =>
      email shouldBe "e@mail.com"
    }

  }

  it should "throw IllegalArgumentException when email cannot be found" in {

    server.stubFor(
      get(urlEqualTo("/pension-administrator/get-email"))
        .willReturn(
          notFound
        )
    )

    val connector = injector.instanceOf[PensionAdministratorConnector]

    recoverToSucceededIf[IllegalArgumentException]{
      connector.getPSAEmail
    }

  }

  "GetPSAName" should "return name" ignore {

    server.stubFor(
      get(urlEqualTo("/pension-administrator/get-name"))
        .willReturn(
          ok("PSA Name")
        )
    )

    val connector = injector.instanceOf[PensionAdministratorConnector]

    connector.getPSAName map { name =>
      name shouldBe "PSA Name"
    }

  }

  it should "throw IllegalArgumentException when name cannot be found" in {

    server.stubFor(
      get(urlEqualTo("/pension-administrator/get-name"))
        .willReturn(
          notFound
        )
    )

    val connector = injector.instanceOf[PensionAdministratorConnector]

    recoverToSucceededIf[IllegalArgumentException]{
      connector.getPSAName
    }

  }

}

