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

package utils.hstasklisthelper


import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.register.Entity
import utils.{Enumerable, UserAnswers}

trait HsTaskListHelperUtils extends Enumerable.Implicits {

  self: AllSpokes =>

  protected val isHnSPhase2Enabled: Boolean

  def createSpoke(answers: UserAnswers,
                  spoke: Spoke,
                  mode: Mode, srn: Option[String], name: String, index: Int, isNew: Boolean): EntitySpoke = {

    val isChangeLink = spoke.completeFlag(answers, index, mode)
    val isComplete: Option[Boolean] = if (mode == NormalMode) isChangeLink else None


    (isChangeLink, isNew) match {
      case (_, false) => EntitySpoke(spoke.changeLink(name)(mode, srn, index))
      case (Some(true), _) => EntitySpoke(spoke.changeLink(name)(mode, srn, index), isComplete)
      case (Some(false), _) => EntitySpoke(spoke.incompleteChangeLink(name)(mode, srn, index), isComplete)
      case _ => EntitySpoke(spoke.addLink(name)(mode, srn, index))
    }
  }

  def createDirectorPartnerSpoke(entityList: Seq[Entity[_]],
                                 spoke: Spoke,
                                 mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {

    val isComplete: Option[Boolean] = if (mode == NormalMode && entityList.nonEmpty) Some(entityList.forall(_.isCompleted)) else None

    if (entityList.isEmpty)
      EntitySpoke(spoke.addLink(name)(mode, srn, index), None)
    else
      EntitySpoke(spoke.changeLink(name)(mode, srn, index), isComplete)
  }

  def getEstablisherCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherCompanyDetails, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherCompanyAddress, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherCompanyContactDetails, mode, srn, name, index, isEstablisherNew),
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(index), EstablisherCompanyDirectors, mode, srn, name, index)
    )
  }

  def getEstablisherIndividualSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherIndividualDetails, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherIndividualAddress, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherIndividualContactDetails, mode, srn, name, index, isEstablisherNew)
    )
  }

  def getEstablisherPartnershipSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherPartnershipDetails, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherPartnershipAddress, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherPartnershipContactDetails, mode, srn, name, index, isEstablisherNew),
      createDirectorPartnerSpoke(answers.allPartnersAfterDelete(index, true), EstablisherPartnershipPartner, mode, srn, name, index)
    )
  }

  def getTrusteeCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteeCompanyDetails, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeCompanyAddress, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeCompanyContactDetails, mode, srn, name, index, isTrusteeNew)
    )
  }

  def getTrusteeIndividualSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteeIndividualDetails, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeIndividualAddress, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeIndividualContactDetails, mode, srn, name, index, isTrusteeNew)
    )
  }

  def getTrusteePartnershipSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteePartnershipDetails, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteePartnershipAddress, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteePartnershipContactDetails, mode, srn, name, index, isTrusteeNew)
    )
  }
}
