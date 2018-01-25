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

package models.addresslookup

import play.api.libs.json._

case class AddressRecord(address: Address)

object AddressRecord {
  implicit val addressRecordFormat: Format[AddressRecord] = Json.format[AddressRecord]
}


/**
  * Address typically represents a postal address.
  * For UK addresses, 'town' will always be present.
  * For non-UK addresses, 'town' may be absent and there may be an extra line instead.
  */
case class Address(lines: List[String],
                   town: Option[String] = None,
                   county: Option[String] = None,
                   postcode: String,
                   country: Country)

object Address {
  implicit val addressFormats: Format[Address] = Json.format[Address]
}

/** Represents a country as per ISO3166. */
case class Country(// The printable name for the country, e.g. "United Kingdom"
                    name: String)

object Country {
  implicit val addressFormats: Format[Country] = Json.format[Country]
}
