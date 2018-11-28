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

package viewmodels

import base.SpecBase
import org.scalatest.{MustMatchers, OptionValues}

class JourneyTaskListSpec extends SpecBase with MustMatchers with OptionValues {

  private def sectionLink(sectionHeader: String): Link =
    Link(s"$sectionHeader link", s"$sectionHeader target")

  private def genJourneyTaskListSection(header: String, isCompleted: Option[Boolean] = None) =
    JourneyTaskListSection(isCompleted = isCompleted, link = sectionLink(header), header = Some(header))

  private val aboutHeader = "about"

  private val about = genJourneyTaskListSection(header = aboutHeader, isCompleted = Some(true))

  "JourneyTaskListSection" when {

    "calling status" must {

      "return the correct status if completed" in {
        about.status mustBe Some("messages__schemeTaskList__completed")
      }

      "return the correct status if in progress" in {
        about.copy(isCompleted = Some(false)).status mustBe Some("messages__schemeTaskList__inProgress")
      }

      "return the correct status if not defined" in {
        about.copy(isCompleted = None).status mustBe None
      }
    }
  }
}
