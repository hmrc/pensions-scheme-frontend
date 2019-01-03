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

package identifiers.register

import identifiers.register.adviser._
import models.address.{Address, TolerantAddress}
import models.register.AdviserDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class DeclarationDutiesIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "cleanup" when {
    val answers = UserAnswers(Json.obj())
      .set(DeclarationDutiesId)(false)
      .flatMap(_.set(AdviserDetailsId)(AdviserDetails("name", "email", "phone")))
      .flatMap(_.set(AdviserEmailId)("a@a.c"))
      .flatMap(_.set(AdviserPhoneId)("0000"))
      .flatMap(_.set(AdviserNameId)("xxx"))
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(AdviserAddressId)(Address("", "", None, None, None, "")))
      .flatMap(_.set(AdviserAddressListId)(TolerantAddress(Some("addr1"),Some("addr2"),Some("addr3"),Some("addr4"),Some("postcode"),Some("country"))))
      .asOpt.value

    "`DeclarationDuties` set to `true`" must {
      val result = answers.set(DeclarationDutiesId)(true).asOpt.value

      "remove the data for `adviser details`" in {
        result.get(AdviserDetailsId) mustNot be(defined)
      }

      "remove the data for `adviser address`" in {
        result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
        result.get(AdviserAddressId) mustNot be(defined)
      }

      "remove the data for `adviser name`" in {
        result.get(AdviserNameId) mustNot be(defined)
      }

      "remove the data for `adviser email`" in {
        result.get(AdviserEmailId) mustNot be(defined)
      }

      "remove the data for `adviser phone`" in {
        result.get(AdviserPhoneId) mustNot be(defined)
      }

      "remove the data for `adviser address list`" in {
        result.get(AdviserAddressListId) mustNot be(defined)
      }

    }

    "`DeclarationDuties` set to `false`" must {
      val result = answers.set(DeclarationDutiesId)(false).asOpt.value

      "not remove the data for `adviser details`" in {
        result.get(AdviserDetailsId) mustBe defined
      }

      "not remove the data for `adviser address`" in {
        result.get(AdviserAddressPostCodeLookupId) mustBe defined
        result.get(AdviserAddressId) mustBe defined
      }

      "not remove the data for `adviser name`" in {
        result.get(AdviserNameId) mustBe defined
      }

      "not remove the data for `adviser email`" in {
        result.get(AdviserEmailId) mustBe defined
      }

      "not remove the data for `adviser phone`" in {
        result.get(AdviserPhoneId) mustBe defined
      }

      "not remove the data for `adviser address list`" in {
        result.get(AdviserAddressListId) mustBe defined
      }

    }
  }
}
