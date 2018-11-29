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

import play.api.libs.json.{Json, OFormat}

case class JourneyTaskList(about: JourneyTaskListSection,
                           establishers: Seq[JourneyTaskListSection],
                           trustees: Seq[JourneyTaskListSection],
                           workingKnowledge: JourneyTaskListSection,
                           declaration: Option[Link] = None){

}

object JourneyTaskList {
  implicit val formats: OFormat[JourneyTaskList] = Json.format[JourneyTaskList]
}

case class JourneyTaskListSection(isCompleted: Option[Boolean] = None, link: Link, header: Option[String] = None) {
  def status: Option[String] =
    isCompleted match {
      case Some(true) => Some("messages__schemeTaskList__completed")
      case Some(false) => Some("messages__schemeTaskList__inProgress")
      case _ => None
    }
}

case class Link(text: String, target: String)

object Link {
  implicit val formats: OFormat[Link] = Json.format[Link]
}

object JourneyTaskListSection {
  implicit val formats: OFormat[JourneyTaskListSection] = Json.format[JourneyTaskListSection]
}
