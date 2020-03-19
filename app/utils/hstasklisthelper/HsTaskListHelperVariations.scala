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

package utils.hstasklisthelper

import com.google.inject.Inject
import identifiers.SchemeNameId
import models._
import utils.UserAnswers
import viewmodels._

class HsTaskListHelperVariations @Inject() (allSpokes: SpokeCreationService) extends HsTaskListHelper(allSpokes) {

  private[utils] def beforeYouStartSection(userAnswers: UserAnswers, srn: Option[String]): SchemeDetailsTaskListEntitySection = {
    SchemeDetailsTaskListEntitySection(None,
      allSpokes.getBeforeYouStartSpoke(userAnswers, UpdateMode, srn, userAnswers.get(SchemeNameId).getOrElse(""), None),
      Some(Message("messages__schemeTaskList__scheme_information_link_text"))
    )
  }

  private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                          mode: Mode, srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    if(userAnswers.allEstablishersAfterDelete(mode).isEmpty && viewOnly) {
      Some(
        SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionEstablishers_no_establishers"))
      )
    } else {
      allSpokes.getAddEstablisherHeaderSpokes(userAnswers, mode, srn, viewOnly) match {
        case Nil =>
          None
        case establisherHeaderSpokes =>
          Some(SchemeDetailsTaskListEntitySection(None, establisherHeaderSpokes, None))
      }
    }
  }

  private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode,
                                      srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    if(userAnswers.allTrusteesAfterDelete.isEmpty && viewOnly) {
      Some(
        SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionTrustees_no_trustees"))
      )
    } else {
      allSpokes.getAddTrusteeHeaderSpokes(userAnswers, mode, srn, viewOnly) match {
        case Nil =>
          None
        case trusteeHeaderSpokes =>
          Some(SchemeDetailsTaskListEntitySection(None, trusteeHeaderSpokes, None))
      }
    }
  }

  private[utils] def declarationSection(userAnswers: UserAnswers, srn: Option[String], viewOnly: Boolean): Option[SchemeDetailsTaskListEntitySection] = {
    if (viewOnly) {
      None
    } else {
      Some(SchemeDetailsTaskListEntitySection(None,
        allSpokes.getVariationDeclarationSpoke(userAnswers, srn),
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2")
      )
    }
  }

  override def taskList(answers: UserAnswers, viewOnlyOpt: Option[Boolean],
                        srn: Option[String]): SchemeDetailsTaskList = {
    val viewOnly = viewOnlyOpt.getOrElse(false)
    SchemeDetailsTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      srn,
      beforeYouStartSection(answers, srn),
      aboutSection(answers, UpdateMode, srn),
      None,
      addEstablisherHeader(answers, UpdateMode, srn, viewOnly),
      establishersSection(answers, UpdateMode, srn),
      addTrusteeHeader(answers, UpdateMode, srn, viewOnly),
      trusteesSection(answers, UpdateMode, srn),
      declarationSection(answers, srn, viewOnly)
    )
  }
}

