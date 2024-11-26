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
import models.MinimalPSA
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.scalatest.flatspec.AsyncFlatSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import utils.WireMockHelper

class MinimalPsaConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import MinimalPsaConnectorSpec._

  override protected lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      )
      .build()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "getMinimalFlags" should "return suspended flag and the deceased flag for the PSA subscription for a valid request" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    connector.getMinimalFlags().map { minimalFlags =>
      minimalFlags.isSuspended shouldBe true
      minimalFlags.isDeceased shouldBe true
      minimalFlags.rlsFlag shouldBe true
    }

  }

  it should "throw JsResultException if the response status is not 200 OK" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          ok(invalidPayloadResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    recoverToSucceededIf[JsResultException] {
      connector.getMinimalFlags()
    }

  }

  it should "throw NotFoundException" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          notFound()
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    recoverToSucceededIf[NotFoundException] {
      connector.getMinimalFlags()
    }

  }

  "getMinimalPsaDetails" should "return successfully when the backend has returned OK" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    connector.getMinimalPsaDetails() map {
      _ shouldBe MinimalPSA(email, isPsaSuspended = true, Some("test ltd"), None)
    }
  }

  it should "return BadRequestException when the backend has returned anything other than ok" in {
    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    recoverToSucceededIf[BadRequestException] {
      connector.getMinimalPsaDetails()
    }
  }
}

object MinimalPsaConnectorSpec extends OptionValues {

  private val minimalPsaDetailsUrl = "/pension-administrator/get-minimal-details-self"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val email = "test@test.com"

  private val validResponse =
    Json.stringify(
      Json.obj(
        "email" -> email,
        "isPsaSuspended" -> true,
        "deceasedFlag" -> true,
        "rlsFlag" -> true,
        "organisationName" -> "test ltd"
      )
    )

  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "isPsaSuspended" -> "reason"
      )
    )
}
