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
import identifiers.register.trustees.MoreThanTenTrusteesId
import models.{LastUpdated, Mode, NormalMode}
import utils.UserAnswers
import viewmodels._

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class HsTaskListHelperRegistration @Inject()(spokeCreationService: SpokeCreationService, appConfig: FrontendAppConfig) extends HsTaskListHelper(
  spokeCreationService) {

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

  private[utils] def completedSectionCount(userAnswers: UserAnswers): Int = {
    Seq(
      userAnswers.isBeforeYouStartCompleted(NormalMode) && userAnswers.isWorkingKnowledgeCompleted.getOrElse(false),
      userAnswers.isMembersCompleted.contains(true),
      userAnswers.isBankDetailsCompleted.contains(true),
      userAnswers.isBenefitsAndInsuranceCompleted.contains(true),
      isAllEstablishersCompleted(userAnswers, NormalMode),
      userAnswers.get(HaveAnyTrusteesId).contains(true) && isAllTrusteesCompleted(userAnswers)
    ).count(identity)
  }

  private[utils] def totalSections(userAnswers: UserAnswers): Int = {
    userAnswers.get(HaveAnyTrusteesId) match {
      case Some(false) => 5
      case _ => 6
    }
  }
}

object HsTaskListHelperRegistration {

  private def isAllTrusteesCompleted(userAnswers: UserAnswers): Boolean =
    userAnswers.allTrusteesAfterDelete.nonEmpty && userAnswers.allTrusteesAfterDelete.forall(_.isCompleted)

  private def isAllEstablishersCompleted(userAnswers: UserAnswers, mode: Mode): Boolean =
    userAnswers.allEstablishersAfterDelete(mode).nonEmpty &&
      userAnswers.allEstablishersAfterDelete(mode).forall(_.isCompleted)
}
