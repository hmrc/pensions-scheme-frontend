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
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.http.Status
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class EmailConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import EmailConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.email.port"

  "sendEmail" should "return the EmailSent status for a valid request" in {

    server.stubFor(
      post(urlEqualTo(emailUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[EmailConnector]

    connector.sendEmail(validEmailString, templateId, params, psaId).map(response =>
      response shouldBe expectedResponse
    )

  }

  it should "return EmailNotSent for a service unavailable response" in {

    server.stubFor(
      post(urlEqualTo(emailUrl))
        .willReturn(
          serviceUnavailable()
        )
    )

    val connector = injector.instanceOf[EmailConnector]

    connector.sendEmail(validEmailString, templateId, params, psaId).map(response =>
      response shouldBe notSentResponse
    )
  }

  it should "return EmailNotSent for a no content response" in {

    server.stubFor(
      post(urlEqualTo(emailUrl))
        .willReturn(
          noContent()
        )
    )

    val connector = injector.instanceOf[EmailConnector]

    connector.sendEmail(validEmailString, templateId, params, psaId).map(response =>
      response shouldBe notSentResponse
    )
  }

}

object EmailConnectorSpec extends OptionValues {

  val psaId = PsaId("A7654321")

  private val emailUrl = "/hmrc/email"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val invalidEmailString = "test@test1.com"
  val validEmailString = "test@mail.com"
  val templateId = "pods_submit"
  val params = Map("testParam" -> "testParam")

  private val expectedResponse = EmailSent

  private val notSentResponse = EmailNotSent
}


