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

package identifiers.register.establishers.partnership.partner

import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.{OtherPartnersId, PartnershipDetailsId}
import models.PartnershipDetails
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class PartnerDetailsIdSpec extends WordSpec with MustMatchers with OptionValues{
  val userAnswersWithTenPartners = UserAnswers(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails("TestCompanyName"),
        "partner" -> Json.arr(
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "One", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Two", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Three", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Four", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Five", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Six", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Seven", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Eight", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Nine", LocalDate.now())),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("Tim", None, "Ten", LocalDate.now(), isDeleted = true)),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("Tim", None, "Eleven", LocalDate.now(), isDeleted = true)),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("Tim", None, "Twelve", LocalDate.now(), isDeleted = true)),
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "Thirteen", LocalDate.now()))
        )
      ))))

  val userAnswersWithOnePartner = UserAnswers(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> PartnershipDetails("TestCompanyName"),
        "partner" -> Json.arr(
          Json.obj(PartnerDetailsId.toString -> PersonDetails("John", None, "One", LocalDate.now()))
        )
      ))))

  "Cleanup" must {

    "remove MoreThanTenDirectorsId" when {

      "there are fewer than 10 partners" in {

        val result: UserAnswers = userAnswersWithOnePartner
          .set(OtherPartnersId(0))(true).asOpt.value
          .remove(PartnerDetailsId(0,0)).asOpt.value

        result.get(OtherPartnersId(0)) must not be defined

      }

      "there are 10 partners" in {

        val result: UserAnswers = userAnswersWithTenPartners
          .set(OtherPartnersId(0))(true).asOpt.value
          .remove(PartnerDetailsId(0,0)).asOpt.value

        result.get(OtherPartnersId(0)) must not be defined

      }

    }

  }
}
