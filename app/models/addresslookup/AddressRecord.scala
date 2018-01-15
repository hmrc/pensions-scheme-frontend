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

case class AddressRecord(id: String,  address: Address)

object AddressRecord {
  implicit val addressRecordFormat: Format[AddressRecord] = Json.format[AddressRecord]
}


/**
  * Address typically represents a postal address.
  * For UK addresses, 'town' will always be present.
  * For non-UK addresses, 'town' may be absent and there may be an extra line instead.
  */
case class Address(lines: List[String],
                   town: Option[String],
                   county: Option[String],
                   postcode: String,
                   subdivision: Option[Country],
                   country: Country)

object Address {
  implicit val addressFormats: Format[Address] = Json.format[Address]
}

/** Represents a country as per ISO3166. */
case class Country(
                    // ISO3166-1 or ISO3166-2 code, e.g. "GB" or "GB-ENG" (note that "GB" is the official
                    // code for UK although "UK" is a reserved synonym and may be used instead)
                    // See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2
                    // and https://en.wikipedia.org/wiki/ISO_3166-2:GB
                    code: String,
                    // The printable name for the country, e.g. "United Kingdom"
                    name: String)

object Country {
  implicit val addressFormats: Format[Country] = Json.format[Country]
}
