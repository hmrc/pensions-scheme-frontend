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

package viewmodels

import models.EntitySpoke

case class SchemeDetailsTaskList(h1: String,
                                 srn: Option[String],
                                 beforeYouStart: SchemeDetailsTaskListEntitySection,
                                 about: SchemeDetailsTaskListEntitySection,
                                 workingKnowledge: Option[SchemeDetailsTaskListEntitySection],
                                 addEstablisherHeader: Option[SchemeDetailsTaskListEntitySection],
                                 establishers: Seq[SchemeDetailsTaskListEntitySection],
                                 addTrusteeHeader: Option[SchemeDetailsTaskListEntitySection],
                                 trustees: Seq[SchemeDetailsTaskListEntitySection],
                                 declaration: Option[SchemeDetailsTaskListEntitySection],
                                 isAllSectionsComplete: Option[Boolean]
                                )

case class SchemeDetailsTaskListEntitySection(isCompleted: Option[Boolean],
                                              entities: Seq[EntitySpoke],
                                              header: Option[Message],
                                              p1: Message*
                                             )


