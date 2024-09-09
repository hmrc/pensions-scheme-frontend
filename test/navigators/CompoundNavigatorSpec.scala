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

package navigators

import identifiers.Identifier
import models.requests.{AuthenticatedRequest, IdentifiedRequest}
import models.{Mode, NormalMode, SchemeReferenceNumber}
import navigators.AboutBenefitsAndInsuranceNavigatorSpec.srn
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class CompoundNavigatorSpec extends AnyFreeSpec with Matchers {

  def navigator(call: Option[Call]): Navigator =
    new Navigator {
      override def nextPageOptional(id: Identifier, mode: Mode, userAnswers: UserAnswers, srn: SchemeReferenceNumber)
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

      val result = compoundNavigator.nextPage(TestIdentifier, NormalMode, UserAnswers(), srn)

      result mustEqual Call("GET", "www.example.com/1")
    }
  }
}
