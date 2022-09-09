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

package utils.hstasklisthelper

import com.google.inject.Inject
import config.FrontendAppConfig
import identifiers._
import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.MoreThanTenTrusteesId
import models.{LastUpdated, Mode, NormalMode}
import utils.{Enumerable, UserAnswers}
import models.register.establishers.EstablisherKind
import viewmodels._


import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class HsTaskListHelperRegistration @Inject()(spokeCreationService: SpokeCreationService, appConfig: FrontendAppConfig) extends HsTaskListHelper(
  spokeCreationService){

  import HsTaskListHelperRegistration._

  //DATE FORMATIING HELPER METHODS

  private val formatter = DateTimeFormatter.ofPattern("dd MMMM YYYY")

  private def createFormattedDate(dt: LastUpdated, daysToAdd: Int): String =
    new Timestamp(dt.timestamp).toLocalDateTime.plusDays(daysToAdd).format(formatter)

  override def taskList(answers: UserAnswers, viewOnly: Option[Boolean], srn: Option[String],
                        lastUpdatedDate: Option[LastUpdated]): SchemeDetailsTaskList = {
    val expiryDate = lastUpdatedDate.map(createFormattedDate(_, appConfig.daysDataSaved))
    SchemeDetailsTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      None,
      beforeYouStartSection(answers),
      aboutSection(answers, NormalMode, srn),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, srn),
      establishersSection(answers, NormalMode, srn),
      addTrusteeHeader(answers, NormalMode, srn),
      trusteesSection(answers, NormalMode, srn),
      declarationSection(answers),
      None,
      Some(StatsSection(completedSectionCount(answers), totalSections(answers), expiryDate))
    )
  }

  def taskListToggleOn(answers: UserAnswers, viewOnly: Option[Boolean], srn: Option[String],
                       lastUpdatedDate: Option[LastUpdated]): SchemeDetailsTaskList = {
    val expiryDate = lastUpdatedDate.map(createFormattedDate(_, appConfig.daysDataSaved))
    SchemeDetailsTaskList(
      answers.get(SchemeNameId).getOrElse(""),
      None,
      beforeYouStartSection(answers),
      aboutSection(answers, NormalMode, srn),
      workingKnowledgeSection(answers),
      addEstablisherHeader(answers, NormalMode, srn),
      Nil,
      addTrusteeHeader(answers, NormalMode, srn),
      trusteesSection(answers, NormalMode, srn),
      declarationSection(answers),
      None,
      Some(StatsSection(completedSectionCount(answers), totalSections(answers), expiryDate))
    )
  }

  protected[utils] def establisherSection(userAnswers: UserAnswers, mode: Mode, srn: Option[String], index: Int)
  : SchemeDetailsTaskListEntitySection = {
    val seqEstablishers = userAnswers.allEstablishers(mode)

    val establisher = seqEstablishers(index)
    if (establisher.isDeleted) throw new RuntimeException("Establisher has been deleted.") else {
      establisher.id match {
        case EstablisherCompanyDetailsId(_) =>
          SchemeDetailsTaskListEntitySection(
            None,
            spokeCreationService.getEstablisherCompanySpokes(userAnswers, mode, srn, establisher.name, Some
            (establisher.index)),
            Some(establisher.name))


        case EstablisherNameId(_) =>
          SchemeDetailsTaskListEntitySection(
            None,
            spokeCreationService.getEstablisherIndividualSpokes(userAnswers, mode, srn, establisher.name, Some
            (establisher.index)),
            Some(establisher.name))

        case EstablisherPartnershipDetailsId(_) =>
          SchemeDetailsTaskListEntitySection(
            None,
            spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, mode, srn, establisher.name, Some
            (establisher.index)),
            Some(establisher.name))
        case _ =>
          throw new RuntimeException("Unknown section id:" + establisher.id)
      }
    }
  }

    def taskListEstablishers(answers: UserAnswers, viewOnly: Option[Boolean], srn: Option[String], establisherIndex: Int): SchemeDetailsTaskListEstablishers = {
      SchemeDetailsTaskListEstablishers(
        answers.get(SchemeNameId).getOrElse(""),
        None,
        establisherSection(answers, NormalMode, srn, establisherIndex),
        isAllEstablishersCompleted(answers, NormalMode),
        Some(StatsSection(completedSectionCountEstablishers(answers), totalSectionsEstablisher(answers, establisherIndex), None))
      )
    }

    private[utils] def beforeYouStartSection(userAnswers: UserAnswers): SchemeDetailsTaskListEntitySection = {
      SchemeDetailsTaskListEntitySection(None,
        spokeCreationService.getBeforeYouStartSpoke(userAnswers, NormalMode, None, userAnswers.get(SchemeNameId)
          .getOrElse(""), None),
        Some(Message("messages__schemeTaskList__before_you_start_header"))
      )
    }

    private[utils] def addEstablisherHeader(userAnswers: UserAnswers,
                                            mode: Mode,
                                            srn: Option[String]): Option[SchemeDetailsTaskListEntitySection] = {
      Some(SchemeDetailsTaskListEntitySection(None, spokeCreationService.getAddEstablisherHeaderSpokes(userAnswers,
        mode, srn, viewOnly = false), None))
    }

    private[utils] def addTrusteeHeader(userAnswers: UserAnswers, mode: Mode, srn: Option[String])
    : Option[SchemeDetailsTaskListEntitySection] = {
      spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, mode, srn, viewOnly = false) match {
        case Nil => None
        case trusteeHeaderSpokes => Some(
          SchemeDetailsTaskListEntitySection(None, trusteeHeaderSpokes, None))
      }
    }

    private[utils] def workingKnowledgeSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListEntitySection] =
      userAnswers.get(DeclarationDutiesId) match {
        case Some(false) =>
          Some(
            SchemeDetailsTaskListEntitySection(None,
              spokeCreationService.getWorkingKnowledgeSpoke(userAnswers, NormalMode, None, userAnswers.get
              (SchemeNameId).getOrElse(""), None),
              None
            )
          )
        case _ =>
          None
      }

    private[utils] def declarationSection(userAnswers: UserAnswers): Option[SchemeDetailsTaskListEntitySection] = {
      val declarationSpoke = if (declarationEnabled(userAnswers)) {
        spokeCreationService.getDeclarationSpoke(controllers.register.routes.DeclarationController.onPageLoad)
      } else {
        Nil
      }
      Some(
        SchemeDetailsTaskListEntitySection(None,
          declarationSpoke,
          Some("messages__schemeTaskList__sectionDeclaration_header"),
          "messages__schemeTaskList__sectionDeclaration_incomplete"
        ))
    }

    def declarationEnabled(userAnswers: UserAnswers): Boolean = {
      Seq(
        Some(userAnswers.isBeforeYouStartCompleted(NormalMode)),
        userAnswers.isMembersCompleted,
        userAnswers.isBankDetailsCompleted,
        userAnswers.isBenefitsAndInsuranceCompleted,
        userAnswers.isWorkingKnowledgeCompleted,
        Some(isAllEstablishersCompleted(userAnswers, NormalMode)),
        Some(userAnswers.get(HaveAnyTrusteesId).contains(false) | isAllTrusteesCompleted(userAnswers)),
        Some(userAnswers.allTrusteesAfterDelete.size < 10 || userAnswers.get(MoreThanTenTrusteesId).isDefined)
      ).forall(_.contains(true))
    }
  }

  object HsTaskListHelperRegistration extends Enumerable.Implicits {

    private[utils] def totalSectionsEstablisher(userAnswers: UserAnswers, index: Int): Int = {
      userAnswers.get(EstablisherKindId(index)) match {
        case Some(EstablisherKind.Indivdual) => 3
        case _ => 4
      }
    }

  private[utils] def totalSections(userAnswers: UserAnswers): Int = {
    (userAnswers.get(HaveAnyTrusteesId), userAnswers.get(DeclarationDutiesId)) match {
      case (Some(false), Some(false)) => 6
      case (Some(true), Some(true)) => 6
      case (Some(true), Some(false)) => 7
      case (Some(false), Some(true)) => 5
      case (Some(false), _) => 6
      case (_, Some(false)) => 7
      case (_, Some(true)) => 6
      case _ => 7
    }
  }

    private def toInt(completionQ1: Boolean, completionQ2: Boolean = true): Int = if (completionQ1) {
      if (completionQ2) 1 else 0
    } else {
      0
    }

    private[utils] def completedSectionCount(userAnswers: UserAnswers): Int = {
      val trusteesCount = toInt(userAnswers.get(HaveAnyTrusteesId).contains(true), isAllTrusteesCompleted(userAnswers))
      val workingKnowledgeCount = toInt(userAnswers.get(DeclarationDutiesId).contains(false), userAnswers.isWorkingKnowledgeCompleted.contains(true))
      val beforeYouStartCount = toInt(userAnswers.isBeforeYouStartCompleted(NormalMode))
      val membersCount = toInt(userAnswers.isMembersCompleted.contains(true))
      val bankCount = toInt(userAnswers.isBankDetailsCompleted.contains(true))
      val benefitsCount = toInt(userAnswers.isBenefitsAndInsuranceCompleted.contains(true))
      val estCount = toInt(isAllEstablishersCompleted(userAnswers, NormalMode))

      //println(s"\nCounts:\ntrustees=$trusteesCount\nworkingknowledge=$workingKnowledgeCount\nbefore=$beforeYouStartCount\nmembers=$membersCount\nbank=$bankCount\nbenefits=$benefitsCount\nest=$estCount")

      val totalCount = trusteesCount +
        workingKnowledgeCount +
        beforeYouStartCount +
        membersCount +
        bankCount +
        benefitsCount +
        estCount
      totalCount
    }

    private[utils] def completedSectionCountEstablishers(userAnswers: UserAnswers): Int = {
      toInt(isAllEstablishersCompleted(userAnswers, NormalMode))
    }

    private def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean = {
      userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)
    }

    private def isAllEstablishersCompleted(userAnswers: UserAnswers, mode: Mode): Boolean =
      userAnswers.allEstablishersAfterDelete(mode).nonEmpty &&
        userAnswers.allEstablishersAfterDelete(mode).forall(_.isCompleted)
  }
