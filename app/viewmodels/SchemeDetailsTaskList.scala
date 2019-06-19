/*
 * Copyright 2019 HM Revenue & Customs
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

import models.Link
import play.api.libs.json.{Json, OFormat}

case class SchemeDetailsTaskList(beforeYouStart: SchemeDetailsTaskListSection,
                                 aboutHeader:String,
                                 about: Seq[SchemeDetailsTaskListSection],
                                 workingKnowledge: Option[SchemeDetailsTaskListSection],
                                 addEstablisherHeader : Option[SchemeDetailsTaskListHeader],
                                 establishers: Seq[SchemeDetailsTaskListEntitySection],
                                 addTrusteeHeader : Option[SchemeDetailsTaskListHeader],
                                 trustees: Seq[SchemeDetailsTaskListSection],
                                 declaration: Option[SchemeDetailsTaskListDeclarationSection] = None,
                                 h1: String,
                                 h2: String,
                                 h3: Option[String],
                                 pageTitle: String,
                                 srn: Option[String]
                                ) {
}

case class SchemeDetailsTaskListDeclarationSection(header:String, declarationLink: Option[Link], incompleteDeclarationText: String*)

object SchemeDetailsTaskListDeclarationSection {
  implicit val formats: OFormat[SchemeDetailsTaskListDeclarationSection] = Json.format[SchemeDetailsTaskListDeclarationSection]
}

object SchemeDetailsTaskList {
  implicit val formats: OFormat[SchemeDetailsTaskList] = Json.format[SchemeDetailsTaskList]
}

case class SchemeDetailsTaskListSection(isCompleted: Option[Boolean] = None, link: Link, header: Option[String] = None, p1: Option[String] = None)

object SchemeDetailsTaskListSection {
  implicit val formats: OFormat[SchemeDetailsTaskListSection] = Json.format[SchemeDetailsTaskListSection]
}

case class SchemeDetailsTaskListEntitySection(isCompleted: Option[Boolean] = None,
                                              entities: Seq[EntityItem],
                                              header: Option[String] = None,
                                              p1: Option[String] = None)

object SchemeDetailsTaskListEntitySection {
  implicit val formats: OFormat[SchemeDetailsTaskListEntitySection] = Json.format[SchemeDetailsTaskListEntitySection]
}

case class EntityItem(link: Link, isCompleted: Option[Boolean] = None)

object EntityItem {
  implicit val formats: OFormat[EntityItem] = Json.format[EntityItem]
}

case class SchemeDetailsTaskListHeader(isCompleted: Option[Boolean] = None, link: Option[Link] = None,
                                       header: Option[String] = None, p1: Option[String] = None, plainText: Option[String] = None)

object SchemeDetailsTaskListHeader {
  implicit val formats: OFormat[SchemeDetailsTaskListHeader] = Json.format[SchemeDetailsTaskListHeader]
}

