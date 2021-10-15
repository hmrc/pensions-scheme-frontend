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

package navigators

import identifiers.Identifier
import models.{Mode, NormalMode}
import models.requests.{AuthenticatedRequest, IdentifiedRequest}
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

class CompoundNavigatorSpec extends FreeSpec with ArgumentMatchers {

  def navigator(call: Option[Call]): Navigator =
    new Navigator {
      override def nextPageOptional(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: Option[String])
                                   (implicit ex: IdentifiedRequest, ec: ExecutionContext, hc: HeaderCarrier): Option[Call] =
        call
    }

  object TestIdentifier extends Identifier

  "a CompoundNavigator" - {

    implicit val identifiedRequest: IdentifiedRequest =
      AuthenticatedRequest(FakeRequest(), "foo", Some(PsaId("A1234567")))

    implicit val hc: HeaderCarrier = HeaderCarrier()

    "must delegate to the bound Navigators" in {

      val navigators = Set(
        navigator(None),
        navigator(Some(Call("GET", "www.example.com/1"))),
        navigator(None)
      )

      val compoundNavigator = new CompoundNavigator(navigators.asJava)

      val result = compoundNavigator.nextPage(TestIdentifier, NormalMode, UserAnswers(), None)

      result mustEqual Call("GET", "www.example.com/1")
    }
  }
}
