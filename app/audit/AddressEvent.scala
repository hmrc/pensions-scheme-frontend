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

package audit

import models.address.{Address, TolerantAddress}
import play.api.libs.json.Json

case class AddressEvent(externalId: String, action: AddressAction.Value, context: String, address: Address) extends
  AuditEvent {

  override def auditType: String = "AddressEvent"

  override def details: Map[String, String] =
    Map(
      "externalId" -> externalId,
      "action" -> action.toString,
      "context" -> context,
      "address" -> Json.stringify(Json.toJson(address))
    )
}

object AddressEvent {

  def addressEntryEvent(
                         externalId: String,
                         address: Address,
                         was: Option[Address],
                         selected: Option[TolerantAddress],
                         context: String
                       ): Option[AddressEvent] = {

    val hasChanged = (address, was) match {
      case (a, Some(w)) if a == w => false
      case _ => true
    }

    if (hasChanged) {
      if (selected.isDefined) {
        val matchesSelected = (address, selected) match {
          case (a, Some(s)) if s.equalsAddress(a) => true
          case _ => false
        }

        if (matchesSelected) {
          Some(AddressEvent(externalId, AddressAction.Lookup, context, address))
        }
        else {
          Some(AddressEvent(externalId, AddressAction.LookupChanged, context, address))
        }
      }
      else {
        Some(AddressEvent(externalId, AddressAction.Manual, context, address))
      }
    }
    else {
      None
    }
  }

}
