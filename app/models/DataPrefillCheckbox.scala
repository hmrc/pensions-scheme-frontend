/*
 * Copyright 2022 HM Revenue & Customs
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

///*
// * Copyright 2022 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package models
//
//import play.api.data.Form
//import uk.gov.hmrc.viewmodels.Text.Literal
//import models.prefill.{IndividualDetails => DataPrefillIndividualDetails}
//import uk.gov.hmrc.viewmodels.MessageInterpolators
//import viewmodels.forNunjucks.Checkboxes
//
//object DataPrefillCheckbox {
//
//  def checkboxes(form: Form[_], values: Seq[DataPrefillIndividualDetails]): Seq[Checkboxes.Item] = {
//    val noneValue = "-1"
//    val items = values.map(indvDetails => Checkboxes.Checkbox(Literal(indvDetails.fullName), indvDetails.index.toString, None, None))
//    val noneOfTheAbove = Checkboxes.Checkbox(msg"messages__prefill__label__none", noneValue, Some("exclusive"), None)
//    val divider = Checkboxes.Checkbox(msg"messages__prefill__label__divider", "divider", None, Some(msg"messages__prefill__label__divider"))
//    Checkboxes.set(form("value"), items :+ divider :+ noneOfTheAbove)
//  }
//}
