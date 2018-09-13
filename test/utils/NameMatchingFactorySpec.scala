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
import connectors.PSANameCacheConnector
import models.PSAName
import models.requests.OptionalDataRequest
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NameMatchingFactorySpec extends SpecBase with MockitoSugar {

  val psaNameCacheConnector = mock[PSANameCacheConnector]
  val schemeName = "My Scheme Reg"

  implicit val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest("", ""), "externalId", None, PsaId("A0000000"))

  private def nameMatchingFactory = new NameMatchingFactory(psaNameCacheConnector, ApplicationCrypto)

  implicit val hc = HeaderCarrier()

  private val encryptedPsaId = app.injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText("A0000000")).value

  "NameMatchingFactory" must {
    "return an instance of NameMatching" when {
      "PSA name is retrieved from PSA Id" in {
        when(psaNameCacheConnector.fetch(eqTo(encryptedPsaId))(any(), any())).thenReturn(Future(Some(Json.toJson(PSAName("My PSA", Some("test@test.com"))))))
        when(psaNameCacheConnector.fetch(eqTo("externalId"))(any(), any())).thenReturn(Future(None))

        val result = nameMatchingFactory.nameMatching(schemeName)

        await(result) mustEqual Some(NameMatching("My Scheme Reg", "My PSA"))
        verify(psaNameCacheConnector, times(1)).fetch(eqTo(encryptedPsaId))(any(), any())
        verify(psaNameCacheConnector, times(1)).fetch(eqTo("externalId"))(any(), any())
      }

      "PSA name is retrieved from external Id" in {
        reset(psaNameCacheConnector)
        when(psaNameCacheConnector.fetch(eqTo(encryptedPsaId))(any(), any())).thenReturn(Future(None))
        when(psaNameCacheConnector.fetch(eqTo("externalId"))(any(), any())).thenReturn(Future(Some(Json.toJson(PSAName("My PSA", Some("test@test.com"))))))

        val result = nameMatchingFactory.nameMatching(schemeName)

        await(result) mustEqual Some(NameMatching("My Scheme Reg", "My PSA"))
        verify(psaNameCacheConnector, times(1)).fetch(eqTo(encryptedPsaId))(any(), any())
        verify(psaNameCacheConnector, times(1)).fetch(eqTo("externalId"))(any(), any())
      }
    }

    "return None" when {
      reset(psaNameCacheConnector)
      "psa name returns None when fetched" in {
        when(psaNameCacheConnector.fetch(any())(any(), any())).thenReturn(Future(None)).
          thenReturn(Future(None))
        val result = nameMatchingFactory.nameMatching(schemeName)

        await(result) mustEqual None

      }

      "psa name is not a PSAName" in {
        reset(psaNameCacheConnector)
        when(psaNameCacheConnector.fetch(any())(any(), any())).thenReturn(Future(Some(Json.obj()))).
          thenReturn(Future(Some(Json.obj())))
        val result = nameMatchingFactory.nameMatching(schemeName)

        await(result) mustEqual None

      }
    }
  }

}
