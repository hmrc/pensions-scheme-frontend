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

package models.register.establishers.individual

import models.addresslookup.Address
import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}
import play.api.libs.json.Json

class AddressSpec extends WordSpecLike with MustMatchers with OptionValues {

  "Reads" must {
    "successfully read address with town and county" in {

      val json = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "town" : "Anytown",
          |                "county" : "Somerset",
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.fromJson[Address](json).asOpt.value mustEqual
        Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = Some("Anytown"),
          addressLine4 = Some("Somerset"), postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")
    }

    "successfully read address without county" in {

      val json = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "town" : "Anytown",
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.fromJson[Address](json).asOpt.value mustEqual
        Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = Some("Anytown"),
          addressLine4 = None, postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")
    }

    "successfully read address without town" in {

      val json = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "county" : "Somerset",
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.fromJson[Address](json).asOpt.value mustEqual
        Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = None,
          addressLine4 = Some("Somerset"), postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")
    }

    "successfully read address without town and county" in {

      val json = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.fromJson[Address](json).asOpt.value mustEqual
        Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = None,
          addressLine4 = None, postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")
    }
  }

  "Writes" must {

    "successfully write ManualAddress with town and county" in {
      val manualAddress = Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = Some("Anytown"),
        addressLine4 = Some("Somerset"), postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")

      val resultJson = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "town" : "Anytown",
          |                "county" : "Somerset",
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.toJson[Address](manualAddress) mustEqual resultJson
    }

    "successfully write ManualAddress without town and county" in {
      val manualAddress = Address(addressLine1 = "10 Other Place", addressLine2 = "Some District", addressLine3 = None,
        addressLine4 = None, postcode = Some("ZZ1 1ZZ"), country = "United Kingdom")

      val resultJson = Json.parse(
        """{
          |                "lines" : [
          |                    "10 Other Place",
          |                    "Some District"
          |                ],
          |                "postcode" : "ZZ1 1ZZ",
          |                "country" : {
          |                    "name" : "United Kingdom"
          |                }
          |            }
        """.stripMargin
      )

      Json.toJson[Address](manualAddress) mustEqual resultJson
    }
  }
}
