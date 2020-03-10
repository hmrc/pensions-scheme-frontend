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

import identifiers._
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.establishers.{company => establisherCompany}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models._
import utils.{Enumerable, UserAnswers}
import viewmodels._

abstract class HsTaskListHelper(answers: UserAnswers
                               ) extends Enumerable.Implicits with HsTaskListHelperUtils with AllSpokes {

  protected def schemeName: String = answers.get(SchemeNameId).getOrElse("")

  protected val addEstablisherLinkText: Message = Message("messages__schemeTaskList__sectionEstablishers_add_link")
  protected val addTrusteesLinkText: Message = Message("messages__schemeTaskList__sectionTrustees_add_link")
  protected def workingKnowledgeLinkText: Message = Message("messages__schemeTaskList__change_details", schemeName)
  protected val declarationLinkText: Message = Message("messages__schemeTaskList__declaration_link")

  protected[utils] def establishersSection(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allEstablishers(mode)
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case establisherCompany.CompanyDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherCompanySpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case EstablisherNameId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherIndividualSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case EstablisherPartnershipDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getEstablisherPartnershipSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )
          case _ =>
            throw new RuntimeException("Unknown section id:" + section.id)
        }
      }
    }
    notDeletedElements.flatten
  }

  protected[utils] def trusteesSection(userAnswers: UserAnswers, mode: Mode, srn: Option[String]): Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allTrustees
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case TrusteeCompanyDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteeCompanySpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case TrusteeNameId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteeIndividualSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case TrusteePartnershipDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              getTrusteePartnershipSpokes(userAnswers, mode, srn, section.name, section.index),
              Some(section.name))
            )

          case _ =>
            throw new RuntimeException("Unknown section id:" + section.id)
        }
      }
    }
    notDeletedElements.flatten
  }

  def taskList: SchemeDetailsTaskList
}
