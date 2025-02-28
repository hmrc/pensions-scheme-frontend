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

package utils.hstasklisthelper

import com.google.inject.Inject
import identifiers._
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models._
import utils.{Enumerable, UserAnswers}
import viewmodels._

abstract class HsTaskListHelper @Inject()(spokeCreationService: SpokeCreationService) extends Enumerable.Implicits {

  def taskList(ua: UserAnswers, viewOnly: Option[Boolean], srn: OptionalSchemeReferenceNumber, lastUpdatedDate: Option[LastUpdated]): SchemeDetailsTaskList

  protected[utils] def aboutSection(userAnswers: UserAnswers, mode: Mode, srn: OptionalSchemeReferenceNumber)
  : SchemeDetailsTaskListEntitySection = {
    val schemeName = userAnswers.get(SchemeNameId).getOrElse("")
    SchemeDetailsTaskListEntitySection(
      None,
      spokeCreationService.getAboutSpokes(userAnswers, mode, srn, schemeName, None),
      Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
    )
  }

  protected[utils] def establishersSection(userAnswers: UserAnswers, mode: Mode, srn: OptionalSchemeReferenceNumber)
  : Seq[SchemeDetailsTaskListEntitySection] = {
    val seqEstablishers = userAnswers.allEstablishers(mode)

    val nonDeletedEstablishers = for ((establisher, _) <- seqEstablishers.zipWithIndex) yield {
      if (establisher.isDeleted) None else {
        establisher.id match {
          case EstablisherCompanyDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getEstablisherCompanySpokes(userAnswers, mode, srn, establisher.name, Some
              (establisher.index)),
              Some(establisher.name))
            )

          case EstablisherNameId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getEstablisherIndividualSpokes(userAnswers, mode, srn, establisher.name, Some
              (establisher.index)),
              Some(establisher.name))
            )

          case EstablisherPartnershipDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, mode, srn, establisher.name, Some
              (establisher.index)),
              Some(establisher.name))
            )
          case _ =>
            throw new RuntimeException("Unknown section id:" + establisher.id)
        }
      }
    }
    nonDeletedEstablishers.flatten
  }

  protected[utils] def trusteesSection(userAnswers: UserAnswers, mode: Mode, srn: OptionalSchemeReferenceNumber)
  : Seq[SchemeDetailsTaskListEntitySection] = {
    val sections = userAnswers.allTrustees
    val notDeletedElements = for ((section, _) <- sections.zipWithIndex) yield {
      if (section.isDeleted) None else {
        section.id match {
          case TrusteeCompanyDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getTrusteeCompanySpokes(userAnswers, mode, srn, section.name, Some(section.index)),
              Some(section.name))
            )

          case TrusteeNameId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getTrusteeIndividualSpokes(userAnswers, mode, srn, section.name, Some(section
                .index)),
              Some(section.name))
            )

          case TrusteePartnershipDetailsId(_) =>
            Some(SchemeDetailsTaskListEntitySection(
              None,
              spokeCreationService.getTrusteePartnershipSpokes(userAnswers, mode, srn, section.name, Some(section
                .index)),
              Some(section.name))
            )

          case _ =>
            throw new RuntimeException("Unknown section id:" + section.id)
        }
      }
    }
    notDeletedElements.flatten
  }
}
