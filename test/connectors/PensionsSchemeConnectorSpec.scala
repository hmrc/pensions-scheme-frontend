/*
 * Copyright 2018 HM Revenue & Customs
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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import models.register.SchemeSubmissionResponse
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{UserAnswers, WireMockHelper}

class PensionsSchemeConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import PensionsSchemeConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  "registerScheme" should "return the Scheme submission confirmation for a valid request/response" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(validResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.registerScheme(userAnswers).map( subscription =>
      subscription shouldBe schemeSubmissionResponse
    )

  }

  it should "throw IllegalArgumentException if the response status is not 200 OK" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.CREATED)
            .withHeader("Content-Type", "application/json")
            .withBody(validResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerScheme(userAnswers)
    }

  }

  it should "throw JsonParseException if there are JSON parse errors" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody("this-is-not-valid-json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[JsonParseException] {
      connector.registerScheme(userAnswers)
    }

  }

  it should "throw JsResultException if the JSON is not valid" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody("{}")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[JsResultException] {
      connector.registerScheme(userAnswers)
    }

  }

  it should "throw SubmissionUnsuccessful exception for a 400 response" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[SchemeSubmissionUnsuccessful] {
      connector.registerScheme(userAnswers)
    }

  }

}

object PensionsSchemeConnectorSpec extends OptionValues {

  private val registerSchemeUrl = "/pensions-scheme/register-scheme"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val userAnswers = UserAnswers()
  private val schemeId = "test-scheme-id"
  private val schemeSubmissionResponse = SchemeSubmissionResponse(schemeId)

  private val validResponse =
    Json.stringify(
      Json.obj(
        "processingDate" -> "1969-07-20T20:18:00Z",
        "formBundle" -> "test-form-bundle",
        "schemeId" -> schemeId
      )
    )

  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_PAYLOAD",
        "reason" -> "test-reason"
      )
    )

  private val invalidCorrelationIdResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_CORRELATION_ID",
        "reason" -> "test-reason"
      )
    )

  private val invalidBusinessPartnerResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_BUSINESS_PARTNER",
        "reason" -> "test-reason"
      )
    )

  private val duplicateSubmissionResponse =
    Json.stringify(
      Json.obj(
        "code" -> "DUPLICATE_SUBMISSION",
        "reason" -> "test-reason"
      )
    )

}
