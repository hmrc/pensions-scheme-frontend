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

package utils

import identifiers.SchemeNameId
import models.requests.IdentifiedRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Request, WrappedRequest}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId

class IDataFromRequestSpec extends WordSpecLike with MustMatchers with OptionValues {

  trait TestDataFromRequest extends IDataFromRequest {
    override def existingSchemeName[A <:WrappedRequest[AnyContent]](implicit request:A):Option[String] =
      super.existingSchemeName
  }

  case class OtherDataRequest[A](request: Request[A], externalId: String, psaId: PsaId)
    extends WrappedRequest[A](request) with IdentifiedRequest

  class FakeOtherDataRequest(request: Request[AnyContentAsEmpty.type], externalId: String, psaId: PsaId)
    extends OtherDataRequest[AnyContent](request, externalId, psaId)

  object FakeOtherDataRequest {
    def apply(): FakeOtherDataRequest = {
      new FakeOtherDataRequest(FakeRequest("", ""), "test-external-id", PsaId("A0000000"))
    }
  }

  val test = new TestDataFromRequest{}

  val validData = Json.obj(SchemeNameId.toString -> "Test Scheme")

  "existingSchemeName" must{

    "return data if calling with DataRequest" in {
      implicit val request = FakeDataRequest(UserAnswers(validData))
      test.existingSchemeName mustBe  Some("Test Scheme")
    }

    "return data if calling with OptionalDataRequest" in {
      implicit val request = FakeOptionalDataRequest(Some(UserAnswers(validData)))
      test.existingSchemeName mustBe  Some("Test Scheme")
    }

    "return none if calling with FakeOptionalDataRequest" in {
      implicit val request = FakeOptionalDataRequest(Some(UserAnswers(Json.obj())))
      test.existingSchemeName mustBe None
    }

    "return none if calling with FakeOtherDataRequest" in {
      implicit val request = FakeOtherDataRequest()
      test.existingSchemeName mustBe None
    }
  }

}
