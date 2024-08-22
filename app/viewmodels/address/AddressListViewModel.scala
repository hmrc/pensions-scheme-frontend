/*
 * Copyright 2024 HM Revenue & Customs
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

package viewmodels.address

import models.address.TolerantAddress
import play.api.mvc.Call
import viewmodels.Message

case class AddressListViewModel(
                                 postCall: Call,
                                 manualInputCall: Call,
                                 addresses: Seq[TolerantAddress],
                                 title: Message = Message("messages__select_the_address__title"),
                                 heading: Message = Message("messages__select_the_address__title"),
                                 selectAddress: Message = Message("messages__common__select_address"),
                                 selectAddressLink: Message = Message("messages__common__select_address_link"),
                                 srn: SchemeReferenceNumber = None,
                                 entityName: String
                               )
