/*
 * Copyright 2020 HM Revenue & Customs
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

import models.{EntitySpoke, TaskListLink}
import play.api.libs.json.{Json, OFormat}

case class SchemeDetailsTaskList(beforeYouStart: SchemeDetailsTaskListSection,
                                 aboutHeader:Message,
                                 about: Seq[SchemeDetailsTaskListSection],
                                 workingKnowledge: Option[SchemeDetailsTaskListSection],
                                 addEstablisherHeader : Option[SchemeDetailsTaskListHeader],
                                 establishers: Seq[SchemeDetailsTaskListEntitySection],
                                 addTrusteeHeader : Option[SchemeDetailsTaskListHeader],
                                 trustees: Seq[SchemeDetailsTaskListEntitySection],
                                 declaration: Option[SchemeDetailsTaskListDeclarationSection] = None,
                                 h1: String,
                                 h2: Message,
                                 h3: Option[Message],
                                 pageTitle: Message,
                                 srn: Option[String]
                                ) {
}

case class SchemeDetailsTaskListDeclarationSection(header:String, declarationLink: Option[TaskListLink], incompleteDeclarationText: String*)

case class SchemeDetailsTaskListSection(isCompleted: Option[Boolean] = None, link: TaskListLink, header: Option[String] = None, p1: Option[String] = None)

case class SchemeDetailsTaskListEntitySection(isCompleted: Option[Boolean] = None,
                                              entities: Seq[EntitySpoke],
                                              header: Option[String] = None,
                                              p1: Option[String] = None)

case class SchemeDetailsTaskListHeader(isCompleted: Option[Boolean] = None, link: Option[TaskListLink] = None,
                                       header: Option[Message] = None, p1: Option[String] = None, plainText: Option[Message] = None)


