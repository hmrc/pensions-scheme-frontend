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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import models.enumerations.SchemeJourneyType
import models.register.SchemeSubmissionResponse
import org.scalatest.{AsyncFlatSpec, OptionValues, Matchers}
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsResultException, Json, JsBoolean}
import play.api.test.FakeRequest
import uk.gov.hmrc.http._
import utils.{UserAnswers, WireMockHelper}

class PensionsSchemeConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import PensionsSchemeConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  "registerScheme" should "return right schemeSubmissionResponse for a valid request/response" in {

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

    connector.registerScheme(userAnswers, "test-psa-id", SchemeJourneyType.NON_RAC_DAC_SCHEME).map(response =>
      response shouldBe schemeSubmissionResponse
    )

  }

  it should "return exception for a 400 response" in {

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

    recoverToSucceededIf[BadRequestException] {
      connector.registerScheme(userAnswers, "test-psa-id", SchemeJourneyType.NON_RAC_DAC_SCHEME)
    }

  }

  it should "return exception for a 500 response" in {

    server.stubFor(
      post(urlEqualTo(registerSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[UpstreamErrorResponse] {
      connector.registerScheme(userAnswers, "test-psa-id", SchemeJourneyType.NON_RAC_DAC_SCHEME)
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
      connector.registerScheme(userAnswers, "test-psa-id", SchemeJourneyType.NON_RAC_DAC_SCHEME)
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
      connector.registerScheme(userAnswers, "test-psa-id", SchemeJourneyType.NON_RAC_DAC_SCHEME)
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

  it should "return exception for a 400 response" in {

    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.updateSchemeDetails(psaId, pstr, userAnswers)
    }

  }

  it should "return exception for a 500 response" in {

    server.stubFor(
      post(urlEqualTo(updateSchemeUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[UpstreamErrorResponse] {
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

  it should "return left INTERNAL_SERVER_ERROR where 500 response is received" in {
    implicit val request = FakeRequest("GET", "/")

    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(INTERNAL_SERVER_ERROR)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.checkForAssociation(psaId, pstr).map(response =>
      response.left.get.status shouldBe INTERNAL_SERVER_ERROR
    )
  }

  it should "return left BAD_REQUEST where 400 response is received" in {
    implicit val request = FakeRequest("GET", "/")
    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(BAD_REQUEST)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.checkForAssociation(psaId, pstr).map(response =>
      response.left.get.status shouldBe BAD_REQUEST
    )
  }

  it should "return left NOT_FOUND where 404 response is received" in {
    implicit val request = FakeRequest("GET", "/")
    server.stubFor(
      get(urlEqualTo(checkAssociationUrl))
        .willReturn(
          aResponse.withStatus(NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.checkForAssociation(psaId, pstr).map(response =>
      response.left.get.status shouldBe NOT_FOUND
    )
  }
}

object PensionsSchemeConnectorSpec extends OptionValues {

  private val registerSchemeUrl = "/pensions-scheme/register-scheme/non-rac-dac"

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
