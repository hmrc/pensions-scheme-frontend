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

package utils

import base.SpecBase
import connectors.PensionAdministratorConnector
import models.requests.OptionalDataRequest
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.RecoverMethods
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NameMatchingFactorySpec extends SpecBase with MockitoSugar with ScalaFutures with RecoverMethods {
  val pensionAdministratorConnector: PensionAdministratorConnector = mock[PensionAdministratorConnector]
  val schemeName = "My Scheme Reg"

  implicit val request: OptionalDataRequest[AnyContent] = OptionalDataRequest(FakeRequest("", ""), "externalId", None, PsaId("A0000000"))

  implicit val hc = HeaderCarrier()

  "NameMatchingFactory" must {
    "return an instance of NameMatching when PSA name is retrieved from PSA Id" which {
      "uses Get PSA Minimal Details" in {

        lazy val app = new GuiceApplicationBuilder()
          .overrides(bind[PensionAdministratorConnector].toInstance(pensionAdministratorConnector))
          .build()

        val nameMatchingFactory = app.injector.instanceOf[NameMatchingFactory]

        when(pensionAdministratorConnector.getPSAName(any(), any()))
          .thenReturn(Future.successful("My PSA"))

        whenReady(nameMatchingFactory.nameMatching(schemeName)) { result =>

          result mustEqual NameMatching("My Scheme Reg", "My PSA")
          verify(pensionAdministratorConnector, times(1)).getPSAName(any(), any())

        }
      }
    }

  }

}
