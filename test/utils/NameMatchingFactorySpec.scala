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

package utils

import base.SpecBase
import connectors.FakeDataCacheConnector
import models.PSAName
import models.requests.OptionalDataRequest
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class NameMatchingFactorySpec extends SpecBase {

  class FakePSANameCacheConnector(fetchResponse: Option[JsValue]) extends FakeDataCacheConnector {
    override def fetch(cacheId: String)(implicit
                                        ec: ExecutionContext,
                                        hc: HeaderCarrier
    ): Future[Option[JsValue]] = Future.successful(fetchResponse)
  }

  val schemeName = "My Scheme Reg"

  implicit val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest("", ""), "externalId", None, PsaId("A0000000"))

  def nameMatchingFactory(fetchResponse: Option[JsValue]) = new NameMatchingFactory(new FakePSANameCacheConnector(fetchResponse))

  implicit val hc = HeaderCarrier()

  "NameMatchingFactory" must {
    "return an instance of NameMatching" when {
      "PSA name is retrieved" in {

        val result = nameMatchingFactory(Some(Json.toJson(PSAName("My PSA", Some("test@test.com"))))).nameMatching(schemeName)

        await(result) mustEqual Some(NameMatching("My Scheme Reg", "My PSA"))

      }
    }

    "return None" when {

      "psa name returns None when fetched" in {

        val result = nameMatchingFactory(None).nameMatching(schemeName)

        await(result) mustEqual None

      }

      "psa name is not a PSAName" in {

        val result = nameMatchingFactory(Some(Json.obj())).nameMatching(schemeName)

        await(result) mustEqual None

      }
    }
  }

}
