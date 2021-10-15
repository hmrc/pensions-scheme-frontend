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

package models.reads

import models.CompanyDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._


class CompanyDetailsReadsSpec extends WordSpec with ArgumentMatchers with OptionValues {
  "Company Details json" should {

    "map correctly to company details" when {

      "We have company details with isDeleted defaulted to false when no isDeleted flag is in json" in {
        val payload = Json.obj("companyName" -> "test", "vatNumber" -> "testVat", "payeNumber" -> "testPaye")
        val result = payload.as[CompanyDetails]
        result.isDeleted mustBe false
      }

      "We have company details with isDeleted flag to true when isDeleted is present in json" in {
        val payload = Json.obj("companyName" -> "test", "vatNumber" -> "testVat", "payeNumber" -> "testPaye", "isDeleted" -> true)
        val result = payload.as[CompanyDetails]
        result.isDeleted mustBe true
      }
    }
  }
}
