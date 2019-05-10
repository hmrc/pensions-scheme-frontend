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

import identifiers.TypedIdentifier
import identifiers.register.establishers.{company => _, partnership => _}
import identifiers.register.trustees.{company => _}
import models.{NormalMode, UpdateMode}
import models.requests.DataRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId

class AllowChangeHelperSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  private val id = new TypedIdentifier[Boolean] {
    override def toString: String = "testId"
  }

  def request(viewOnly:Boolean, ua:UserAnswers): DataRequest[AnyContent] =
    DataRequest(FakeRequest(), "id", ua, PsaId("A0000000"), viewOnly = viewOnly)

  private val uaWithId = UserAnswers(Json.obj(
    id.toString -> true
  ))

  private val uaWithoutId = UserAnswers()

  private val ach = new AllowChangeHelperImpl

  "hideSaveAndContinueButton" must {
    "return false where not viewOnly and id is present and mode is normal mode" in {
      ach.hideSaveAndContinueButton(request(viewOnly = false, ua = uaWithId), id, NormalMode) mustBe false
    }
    "return true where not viewOnly and id is not present and mode is normal mode" in {
      ach.hideSaveAndContinueButton(request(viewOnly = false, ua = uaWithoutId), id, NormalMode) mustBe false
    }
    "return true where not viewOnly and id is not present and mode is update mode" in {
      ach.hideSaveAndContinueButton(request(viewOnly = false, ua = uaWithoutId), id, UpdateMode) mustBe true
    }
    "return true where viewOnly and id is present and mode is normal mode" in {
      ach.hideSaveAndContinueButton(request(viewOnly = true, ua = uaWithId), id, NormalMode) mustBe true
    }
    "return true where viewOnly and id is not present and mode is normal mode" in {
      ach.hideSaveAndContinueButton(request(viewOnly = true, ua = uaWithoutId), id, NormalMode) mustBe true
    }
  }

}


