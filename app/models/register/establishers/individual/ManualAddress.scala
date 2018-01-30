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

import models.register.SchemeType
import models.register.SchemeType.Other
import play.api.libs.json._

case class Location(postCode: Option[String], country: String)

object Location {
  implicit val locationFormat: Format[Location] = Json.format[Location]
}


case class ManualAddress(addressLine1: String,
                             addressLine2: String,
                             addressLine3: Option[String],
                             addressLine4: Option[String],
                              location: Location)

object ManualAddress {

  implicit val readAddress: Reads[ManualAddress] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    (
      (__ \ "addressLine1").read[String] and
        (__ \ "addressLine2").read[String] and
        (__ \ "addressLine3").readNullable[String] and
        (__ \ "addressLine4").readNullable[String] and
        (__ \ "location" \ "postCode").readNullable[String] and
        (__ \ "location" \ "country").read[String]

      ) { (line1, line2, line3, line4, postcode, country) =>
      ManualAddress(line1, line2, line3, line4, Location(postcode, country))
    }
  }

  implicit val writeAddress: Writes[ManualAddress] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._
    (
      (__ \ "addressLine1").write[String] and
        (__ \ "addressLine2").write[String] and
        (__ \ "addressLine3").writeNullable[String] and
        (__ \ "addressLine4").writeNullable[String] and
        (__ \ "location" \ "postCode").writeNullable[String] and
        (__ \ "location" \ "country").write[String]
      ) { model =>
      (
        (model.addressLine1, model.addressLine2,
        model.addressLine3,
        model.addressLine4,
        model.location.postCode,
        model.location.country))
    }
  }
}
