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

package forms.mappings

import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain._

case class CrnRegex(crn: String) extends TaxIdentifier with SimpleName {

  val name = "crn"
  require(CrnRegex.isValid(crn), s"$crn is not a valid company registration number.")

  override def toString = crn
  
  def value = crn
}

object CrnRegex extends (String => CrnRegex) {

  implicit val crnWrite: Writes[CrnRegex] = new SimpleObjectWrites[CrnRegex](_.value)
  implicit val crnRead: Reads[CrnRegex] = new SimpleObjectReads[CrnRegex]("crn", CrnRegex.apply)

  val validCrnString = "^(\\d{7}|[A-Z]\\d{6}|[A-Z][A-Z]\\d{6})$"

  def isValid(crn: String) = crn != null && crn.matches(validCrnString)

}
