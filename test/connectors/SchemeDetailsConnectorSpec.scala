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
import models.details._
import org.scalatest.{AsyncFlatSpec, Matchers}
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import utils.{UserAnswers, WireMockHelper}

class SchemeDetailsConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import SchemeDetailsConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  "getSchemeDetailsVariations" should "return the SchemeDetails for a valid request/response" in {

    server.stubFor(
      get(urlEqualTo(schemeDetailsUrl))
        .withHeader("schemeIdType", equalTo(schemeIdType))
        .withHeader("idNumber", equalTo(idNumber))
        .withHeader("PSAId", equalTo(psaId))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(validSchemeDetailsVariationsResponse)
        )
    )

    val connector = injector.instanceOf[SchemeDetailsConnector]

    connector.getSchemeDetails(psaId, schemeIdType, idNumber).map(schemeDetails =>
      schemeDetails shouldBe psaSchemeDetailsVariationsResponse
    )
  }

  it should "throw BadRequestException for a 400 INVALID_IDTYPE response" in {

    server.stubFor(
      get(urlEqualTo(schemeDetailsUrl))
        .withHeader("schemeIdType", equalTo(schemeIdType))
        .withHeader("idNumber", equalTo(idNumber))
        .withHeader("PSAId", equalTo(psaId))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_IDTYPE"))
        )
    )

    val connector = injector.instanceOf[SchemeDetailsConnector]
    recoverToSucceededIf[BadRequestException] {
      connector.getSchemeDetails(psaId, schemeIdType, idNumber)
    }
  }

  it should "throw BadRequestException for a 400 INVALID_SRN response" in {

    server.stubFor(
      get(urlEqualTo(schemeDetailsUrl))
        .withHeader("schemeIdType", equalTo(schemeIdType))
        .withHeader("idNumber", equalTo(idNumber))
        .withHeader("PSAId", equalTo(psaId))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_SRN"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getSchemeDetails(psaId, schemeIdType, idNumber)
    }

  }
  it should "throw BadRequestException for a 400 INVALID_PSTR response" in {

    server.stubFor(
      get(urlEqualTo(schemeDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_PSTR"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getSchemeDetails(psaId, schemeIdType, idNumber)
    }

  }

  it should "throw BadRequest for a 400 INVALID_CORRELATIONID response" in {

    server.stubFor(
      get(urlEqualTo(schemeDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_CORRELATIONID"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getSchemeDetails(psaId, schemeIdType, idNumber)
    }

  }

}

object SchemeDetailsConnectorSpec {

  private val psaId = "0000"
  private val schemeIdType = "pstr"
  private val idNumber = "00000000AA"
  private val schemeDetailsUrl = s"/pensions-scheme/scheme"
  val mockSchemeDetails = SchemeDetails(Some("S9000000000"), Some("00000000AA"), "Open", "Benefits Scheme", true, None, None, false,
    SchemeMemberNumbers("0","0"), false, false, "AD", "GB", false, None)

  val psaDetails1 = PsaDetails("A0000000",Some("partnetship name"),Some(Name(Some("Taylor"),Some("Middle"),Some("Rayon"))))
  val psaDetails2 = PsaDetails("A0000001",Some("partnetship name 1"),Some(Name(Some("Smith"),Some("A"),Some("Tony"))))

  val psaSchemeDetailsResponse = PsaSchemeDetails(mockSchemeDetails, None, None, Seq(psaDetails1, psaDetails2))

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val validSchemeDetailsResponse = Json.toJson(psaSchemeDetailsResponse).toString()


  private val validSchemeDetailsVariationsResponse = """
        { "somedata": "somevalue" }
      """.stripMargin

  private val psaSchemeDetailsVariationsResponse = UserAnswers(Json.parse(validSchemeDetailsVariationsResponse))


  def errorResponse(code: String): String = {
    Json.stringify(
      Json.obj(
        "code" -> code,
        "reason" -> s"Reason for $code"
      )
    )
  }

}


