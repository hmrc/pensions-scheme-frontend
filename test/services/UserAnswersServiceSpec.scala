/*
 * Copyright 2019 HM Revenue & Customs
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

package services

import com.github.tomakehurst.wiremock.client.WireMock._
import identifiers.TypedIdentifier
import models.{CheckMode, NormalMode, UpdateMode}
import models.requests.DataRequest
import org.scalatest.{AsyncWordSpec, MustMatchers}
import play.api.libs.json.{JsBoolean, Json}
import play.api.mvc.AnyContent
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeDataRequest, UserAnswers, WireMockHelper}

class UserAnswersServiceSpec extends AsyncWordSpec with MustMatchers with WireMockHelper {

  protected object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  protected implicit val hc: HeaderCarrier = HeaderCarrier()
  protected implicit val request: DataRequest[AnyContent] = FakeDataRequest(UserAnswers(Json.obj()))
  private val id = "test-external-id"
  private val srn = "S1234567890"

  protected def urlNormal(id: String): String = s"/pensions-scheme/journey-cache/scheme/$id"
  protected def urlUpdate(id: String): String = s"/pensions-scheme/journey-cache/update-scheme/$id"

  protected def lastUpdatedUrl(id: String) = s"/pensions-scheme/journey-cache/scheme/$id/lastUpdated"

  protected lazy val service: UserAnswersService = injector.instanceOf[UserAnswersService]


  ".save" must {

    "save data with userAnswersCacheConnector in NormalMode" in {

      val json = Json.obj(
        "fake-identifier" -> "foobar"
      )
      val value = Json.stringify(json)

      server.stubFor(
        get(urlEqualTo(urlNormal(id)))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(urlNormal(id)))
          .withRequestBody(equalTo(value))
          .willReturn(
            ok
          )
      )

      service.save(NormalMode, None, FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save data with userAnswersCacheConnector in CheckMode" in {

      val json = Json.obj(
        "fake-identifier" -> "foobar"
      )
      val value = Json.stringify(json)

      server.stubFor(
        get(urlEqualTo(urlNormal(id)))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(urlNormal(id)))
          .withRequestBody(equalTo(value))
          .willReturn(
            ok
          )
      )

      service.save(CheckMode, None, FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save data with updateSchemeCacheConnector in UpdateMode" in {

      val json = Json.obj(
        "fake-identifier" -> "foobar"
      )

      val updatedJson = Json.obj(
        "fake-identifier" -> "foobar",
        "changeOfEstablisherOrTrustDetails" -> true
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(urlUpdate(srn)))
          .willReturn(
            notFound()
          )
      )

      server.stubFor(
        post(urlEqualTo(urlUpdate(srn)))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      service.save(UpdateMode, Some(srn), FakeIdentifier, "foobar") map {result =>
        result mustEqual updatedJson
      }
    }

  }

  ".remove" must {
    "remove existing data in NormalMode" in {
      val json = Json.obj(
        FakeIdentifier.toString -> "fake value",
        "other-key" -> "meh"
      )

      val updatedJson = Json.obj(
        "other-key" -> "meh"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(urlNormal(id)))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(urlNormal(id)))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      service.remove(NormalMode, None, FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }
  }


}
