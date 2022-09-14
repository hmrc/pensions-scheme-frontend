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


import identifiers.HaveAnyTrusteesId
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.IsTrusteeNewId
import models.Index.indexToInt
import models._
import models.register.Entity
import play.api.mvc.Call
import utils.hstasklisthelper.spokes._
import utils.{Enumerable, UserAnswers}
import viewmodels.Message

class SpokeCreationService extends Enumerable.Implicits {

  def getBeforeYouStartSpoke(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                             index: Option[Index]): Seq[EntitySpoke] = {
    Seq(
      createSpoke(answers, BeforeYouStartSpoke, mode, srn, name, index, None)
    )
  }

  def getAboutSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Option[Index])
  : Seq[EntitySpoke] = {
    Seq(
      createSpoke(answers, AboutMembersSpoke, mode, srn, name, index, None),
      createSpoke(answers, AboutBenefitsAndInsuranceSpoke, mode, srn, name, index, None)
    ) ++ (if (srn.isEmpty) Seq(createSpoke(answers, AboutBankDetailsSpoke, mode, srn, name, index, None)) else Nil)
  }

  def getWorkingKnowledgeSpoke(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                               index: Option[Index]): Seq[EntitySpoke] = {
    Seq(
      createSpoke(answers, WorkingKnowledgeSpoke, mode, srn, name, index, None)
    )
  }

  def  getEstablisherCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                                  index: Option[Index]): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherCompanyDetails, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherCompanyAddress, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherCompanyContactDetails, mode, srn, name, index, Some(isEstablisherNew)),
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(indexToInt(index.getOrElse(Index(0)))),
        EstablisherCompanyDirectors, mode, srn, name, index)
    )
  }

  def createDirectorPartnerSpoke(entityList: Seq[Entity[_]], spoke: Spoke, mode: Mode, srn: Option[String], name: String, index: Option[Index]): EntitySpoke = {
    val isComplete: Option[Boolean] = {
      (mode, entityList.isEmpty) match {
        case (NormalMode | UpdateMode, true) => Some(false)
        case (NormalMode | UpdateMode, false) if spoke == EstablisherPartnershipPartner && entityList.size == 1 =>
          Some(false)
        case (NormalMode, false) =>
          Some(entityList.forall(_.isCompleted))
        case (UpdateMode, false) if entityList.exists(!_.isCompleted) =>
          Some(false)
        case _ => None
      }
    }

    (entityList.isEmpty, isComplete) match {
      case (true, _) =>
        EntitySpoke(spoke.addLink(name)(mode, srn, index), isComplete)
      case (false, Some(false)) =>
        EntitySpoke(spoke.incompleteChangeLink(name)(mode, srn, index), isComplete)
      case _ =>
        EntitySpoke(spoke.changeLink(name)(mode, srn, index), isComplete)
    }
  }

  def getEstablisherIndividualSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                                     index: Option[Index]): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherIndividualDetails, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherIndividualAddress, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherIndividualContactDetails, mode, srn, name, index, Some(isEstablisherNew))
    )
  }

  def getEstablisherPartnershipSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                                      index: Option[Index]): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherPartnershipDetails, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherPartnershipAddress, mode, srn, name, index, Some(isEstablisherNew)),
      createSpoke(answers, EstablisherPartnershipContactDetails, mode, srn, name, index, Some(isEstablisherNew)),
      createDirectorPartnerSpoke(answers.allPartnersAfterDelete(indexToInt(index.getOrElse(Index(0)))),
        EstablisherPartnershipPartner, mode, srn, name, index)
    )
  }

  def getTrusteeCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                              index: Option[Index]): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteeCompanyDetails, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteeCompanyAddress, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteeCompanyContactDetails, mode, srn, name, index, Some(isTrusteeNew))
    )
  }

  def getTrusteeIndividualSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                                 index: Option[Index]): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteeIndividualDetails, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteeIndividualAddress, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteeIndividualContactDetails, mode, srn, name, index, Some(isTrusteeNew))
    )
  }

  def getTrusteePartnershipSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String,
                                  index: Option[Index]): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(indexToInt(index.getOrElse(Index(0))))).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteePartnershipDetails, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteePartnershipAddress, mode, srn, name, index, Some(isTrusteeNew)),
      createSpoke(answers, TrusteePartnershipContactDetails, mode, srn, name, index, Some(isTrusteeNew))
    )
  }

  def createSpoke(answers: UserAnswers,
                  spoke: Spoke,
                  mode: Mode, srn: Option[String], name: String, index: Option[Index], isNew: Option[Boolean])
  : EntitySpoke = {

    val isChangeLink = spoke.completeFlag(answers, index, mode)
    val isComplete: Option[Boolean] = (mode, isChangeLink) match {
      case (NormalMode, Some(false) | None) => Some(false)
      case (NormalMode, _) => isChangeLink
      case (UpdateMode, Some(false) | None) => Some(false)
      case _ => None
    }

    (isChangeLink, isNew) match {
      case (_, Some(false)) => EntitySpoke(spoke.changeLink(name)(mode, srn, index), isComplete)
      case (Some(true), _) => EntitySpoke(spoke.changeLink(name)(mode, srn, index), isComplete)
      case (Some(false), _) => EntitySpoke(spoke.incompleteChangeLink(name)(mode, srn, index), isComplete)
      case _ => EntitySpoke(spoke.addLink(name)(mode, srn, index), isComplete)
    }
  }

  def getAddEstablisherHeaderSpokesToggleOff(answers: UserAnswers, mode: Mode, srn: Option[String], viewOnly: Boolean)
  : Seq[EntitySpoke] = {
    (answers.allEstablishersAfterDelete(mode).isEmpty, viewOnly) match {
      case (_, true) =>
        Nil
      case (true, false) =>
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, answers
              .allEstablishers(mode).size, srn).url), None)
        )
      case (false, false) if srn.isDefined =>
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_view_link"),
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url), None)
        )
      case (false, false) =>
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionEstablishers_change_link"),
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url),
          None
        ))
    }
  }

  def getAddEstablisherHeaderSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], viewOnly: Boolean)
  : Seq[EntitySpoke] = {

    val establishers = answers.allEstablishersAfterDelete(mode)
    val isAllEstablishersComplete = if (establishers.isEmpty) None else Some(establishers.forall(_.isCompleted))

    if (establishers.isEmpty) {
      Seq(EntitySpoke(
        TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, answers
            .allEstablishers(mode).size, srn).url), None)
      )
    } else if(establishers.nonEmpty && !establishers.forall(_.isCompleted)) {
      Seq(EntitySpoke(
        TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_continue_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url), isAllEstablishersComplete)
      )
    }
    else {
      Seq(EntitySpoke(
        TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_change_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url), isAllEstablishersComplete)
      )
    }
  }

  def getAddTrusteeHeaderSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], viewOnly: Boolean)
  : Seq[EntitySpoke] = {
    (answers.get(HaveAnyTrusteesId), answers.allTrusteesAfterDelete.isEmpty, viewOnly) match {
      case (None | Some(true), false, false) if srn.isDefined =>
        Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url), None)
        )
      case (None | Some(true), false, false) =>
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url),
          None
        ))
      case (None | Some(true), true, false) =>
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionTrustees_add_link"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, answers.allTrustees.size,
              srn).url),
          None
        ))
      case _ =>
        Nil
    }
  }

  def getDeclarationSpoke(call: Call): Seq[EntitySpoke] = {
    Seq(
      EntitySpoke(
        TaskListLink(
          Message("messages__schemeTaskList__declaration_link"),
          call.url)
      )
    )
  }
}
