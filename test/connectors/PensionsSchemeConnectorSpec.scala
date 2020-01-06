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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import models.register.SchemeSubmissionResponse
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.http.Status
import play.api.libs.json.{JsBoolean, JsResultException, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, Upstream5xxResponse}
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

    connector.registerScheme(userAnswers, "test-psa-id").map(subscription =>
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
      connector.registerScheme(userAnswers, "test-psa-id")
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
      connector.registerScheme(userAnswers, "test-psa-id")
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
      connector.registerScheme(userAnswers, "test-psa-id")
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

    recoverToSucceededIf[InvalidPayloadException] {
      connector.registerScheme(userAnswers, "test-psa-id")
    }

  }

  "updateSchemeDetails" should "return without exceptions for a valid request/response" in {
    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("psaId", equalTo(psaId))
        .withHeader("pstr", equalTo(pstr))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    noException shouldBe thrownBy {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }
  }

  it should "return BadRequestException where invalid psaid response is received" in {
    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[InvalidPayloadException] {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }
  }


  it should "return BadRequestException where not found response is received" in {
    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          aResponse.withStatus(404)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[NotFoundException] {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }
  }

  it should "return BadRequestException where 400 response is received" in {
    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          aResponse.withStatus(400)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }
  }

  it should "return Upstream5xxResponse where 500 response is received" in {
    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          aResponse.withStatus(500)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[Upstream5xxResponse] {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }
  }


  "checkForAssociation" should "return without exceptions for a valid request/response" in {
    implicit val request = FakeRequest("GET", "/")

    val validResponse =
      Json.stringify(
        JsBoolean(true)
      )

    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .withHeader("Content-Type", equalTo("application/json"))
        .withHeader("psaId", equalTo(psaId))
        .withHeader("schemeReferenceNumber", equalTo(schemeId))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    noException shouldBe thrownBy {
      connector.checkForAssociation(psaId, pstr)
    }
  }

  it should "return Upstream5xxResponse where 500 response is received" in {
    implicit val request = FakeRequest("GET", "/")

    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(500)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[Upstream5xxResponse] {
      connector.checkForAssociation(psaId, pstr)
    }
  }

  it should "return BadRequestException where 400 response is received" in {
    implicit val request = FakeRequest("GET", "/")
    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(400)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.checkForAssociation(psaId, pstr)
    }
  }

  it should "return false where 400 response is received with text INVALID_SRN" in {
    implicit val request = FakeRequest("GET", "/")
    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(400).withBody("INVALID_SRN")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

      connector.checkForAssociation(psaId, pstr).map { result =>
        result shouldBe false
      }
  }

  it should "return false where 404 response is received" in {
    implicit val request = FakeRequest("GET", "/")
    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(404)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.checkForAssociation(psaId, pstr).map { result =>
      result shouldBe false
    }
  }


}

object PensionsSchemeConnectorSpec extends OptionValues {

  private val registerSchemeUrl = "/pensions-scheme/register-scheme"
  
  private val updateSchemeUrl = "/pensions-scheme/update-scheme"
  private val checkAssociationUrl = "/pensions-scheme/is-psa-associated"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val userAnswers = UserAnswers()
  private val schemeId = "test-scheme-id"
  private val psaId = "test-psa-id"
  private val pstr = "test-pstr"
  private val schemeSubmissionResponse = SchemeSubmissionResponse(schemeId)

  private val validResponse =
    Json.stringify(
      Json.obj(
        "processingDate" -> "1969-07-20T20:18:00Z",
        "formBundle" -> "test-form-bundle",
        "schemeReferenceNumber" -> schemeId
      )
    )

  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_PAYLOAD",
        "reason" -> "test-reason"
      )
    )

}
