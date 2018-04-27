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
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsString

import scala.concurrent.Future

class NameMatchingFactorySpec extends SpecBase with MockitoSugar {

  "NameMatchingFactory" must {
    "return an instance of NameMatching" when {
      "scheme name and PSA name are retrieved" in {

        val nameMatchingFactory = new NameMatchingFactory(
          mock[PSANameCacheConnector]
        )

        when(nameMatchingFactory.pSANameCacheConnector.fetch(any())(any(),any()))
          .thenReturn(Future.successful(Some(JsString("My PSA"))))

      }
    }
  }

}
