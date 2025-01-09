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
import models.SchemeReferenceNumber
import models.details._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
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

  "getPspSchemeDetails" should "return the PspSchemeDetails for a valid request/response" in {

    server.stubFor(
      get(urlEqualTo(pspSchemeDetailsUrl))
        .withHeader("srn", equalTo(idNumber))
        .withHeader("pspId", equalTo(psaId))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(validSchemeDetailsVariationsResponse)
        )
    )

    val connector = injector.instanceOf[SchemeDetailsConnector]

    connector.getPspSchemeDetails(pspId, idNumber).map(schemeDetails =>
      schemeDetails shouldBe psaSchemeDetailsVariationsResponse
    )
  }

  it should "throw BadRequestException for a 400 INVALID_IDTYPE response" in {

    server.stubFor(
      get(urlEqualTo(pspSchemeDetailsUrl))
        .withHeader("srn", equalTo(idNumber))
        .withHeader("pspId", equalTo(psaId))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_IDTYPE"))
        )
    )

    val connector = injector.instanceOf[SchemeDetailsConnector]
    recoverToSucceededIf[BadRequestException] {
      connector.getPspSchemeDetails(pspId, idNumber)
    }
  }

  it should "throw BadRequestException for a 400 INVALID_SRN response" in {

    server.stubFor(
      get(urlEqualTo(pspSchemeDetailsUrl))
        .withHeader("srn", equalTo(idNumber))
        .withHeader("pspId", equalTo(psaId))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_SRN"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getPspSchemeDetails(pspId, idNumber)
    }

  }
  it should "throw BadRequestException for a 400 INVALID_PSTR response" in {

    server.stubFor(
      get(urlEqualTo(pspSchemeDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_PSTR"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getPspSchemeDetails(pspId, idNumber)
    }

  }

  it should "throw BadRequest for a 400 INVALID_CORRELATIONID response" in {

    server.stubFor(
      get(urlEqualTo(pspSchemeDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_CORRELATIONID"))
        )
    )
    val connector = injector.instanceOf[SchemeDetailsConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getPspSchemeDetails(pspId, idNumber)
    }

  }

}

object SchemeDetailsConnectorSpec {

  private val psaId = "0000"
  private val pspId = "0000"
  private val schemeIdType = "pstr"
  private val idNumber = SchemeReferenceNumber("00000000AA")
  private val schemeDetailsUrl = s"/pensions-scheme/scheme"
  private val pspSchemeDetailsUrl = s"/pensions-scheme/psp-scheme"
  val mockSchemeDetails: SchemeDetails = SchemeDetails(Some(SchemeReferenceNumber("S9000000000")), Some("00000000AA"), "Open", "Benefits Scheme",
    isMasterTrust = true, typeOfScheme = None, otherTypeOfScheme = None, hasMoreThanTenTrustees = false,
    members = SchemeMemberNumbers("0", "0"), isInvestmentRegulated = false, isOccupational = false, benefits = "AD",
    country = "GB", areBenefitsSecured = false, insuranceCompany = None)

  val psaDetails1: PsaDetails = PsaDetails("A0000000",Some("partnetship name"),Some(Name(Some("Taylor"),Some("Middle"),Some("Rayon"))))
  val psaDetails2: PsaDetails = PsaDetails("A0000001",Some("partnetship name 1"),Some(Name(Some("Smith"),Some("A"),Some("Tony"))))

  val psaSchemeDetailsResponse: PsaSchemeDetails = PsaSchemeDetails(mockSchemeDetails, None, None, Seq(psaDetails1, psaDetails2))

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

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


