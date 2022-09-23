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

package models.register

import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerNameId
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models._
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind

sealed trait Entity[ID] {
  def id: ID

  def name: String

  def isDeleted: Boolean

  def isCompleted: Boolean

  def editLink(mode: Mode, srn: Option[String]): Option[String]

  def deleteLink(mode: Mode, srn: Option[String]): Option[String]

  def index: Int
}

case class DirectorEntity(id: DirectorNameId, name: String, isDeleted: Boolean,
                          isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int) extends Entity[DirectorNameId] {
  override def editLink(mode: Mode, srn: Option[String]): Option[String] = (isNewEntity, isCompleted) match {
    case (false, _) => Some(controllers.register.establishers.company.director.routes.CheckYourAnswersController
      .onPageLoad(
      id.establisherIndex, id.directorIndex, mode, srn).url)
    case (_, true) => Some(controllers.register.establishers.company.director.routes.CheckYourAnswersController
      .onPageLoad(
      id.establisherIndex, id.directorIndex, mode, srn).url)
    case (_, false) => Some(controllers.register.establishers.company.director.routes.DirectorNameController.onPageLoad(
      mode, id.establisherIndex, id.directorIndex, srn).url)
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(
          id.establisherIndex, id.directorIndex, mode, srn).url)
      case UpdateMode | CheckUpdateMode if noOfRecords > 1 =>
        Some(controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(
          id.establisherIndex, id.directorIndex, mode, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.directorIndex
}

sealed trait Partner[T] extends Entity[T]

case class PartnerEntity(id: PartnerNameId, name: String, isDeleted: Boolean,
                         isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int) extends Partner[PartnerNameId] {
  override def editLink(mode: Mode, srn: Option[String]): Option[String] = (isNewEntity, isCompleted) match {
    case (false, _) => Some(controllers.register.establishers.partnership.partner.routes.CheckYourAnswersController
      .onPageLoad(
      mode, id.establisherIndex, id.partnerIndex, srn).url)
    case (_, true) => Some(controllers.register.establishers.partnership.partner.routes.CheckYourAnswersController
      .onPageLoad(
      mode, id.establisherIndex, id.partnerIndex, srn).url)
    case (_, false) => Some(controllers.register.establishers.partnership.partner.routes.PartnerNameController
      .onPageLoad(
      mode, id.establisherIndex, id.partnerIndex, srn).url)
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController.onPageLoad(
          mode, id.establisherIndex, id.partnerIndex, srn).url)
      case UpdateMode | CheckUpdateMode if noOfRecords > 1 =>
        Some(controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController.onPageLoad(
          mode, id.establisherIndex, id.partnerIndex, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.partnerIndex
}

sealed trait Establisher[T] extends Entity[T]

case class EstablisherCompanyEntity(id: EstablisherCompanyDetailsId, name: String, isDeleted: Boolean,
                                    isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int) extends
  Establisher[EstablisherCompanyDetailsId] {

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(id.index).url)
      case _ => None
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Company, srn).url)
      case UpdateMode | CheckUpdateMode if noOfRecords > 1 =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Company, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.index
}

case class EstablisherIndividualEntity(id: EstablisherNameId, name: String, isDeleted: Boolean,
                                       isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int) extends
  Establisher[EstablisherNameId] {
  override def editLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(id.index).url)
      case _ => None
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Indivdual, srn).url)
      case UpdateMode | CheckUpdateMode if noOfRecords > 1 =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Indivdual, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.index
}

case class EstablisherPartnershipEntity(id: PartnershipDetailsId, name: String, isDeleted: Boolean,
                                        isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int) extends
  Establisher[PartnershipDetailsId] {

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(id.index).url)
      case _ => None
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Partnership, srn).url)
      case UpdateMode | CheckUpdateMode if noOfRecords > 1 =>
        Some(controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index,
          EstablisherKind.Partnership, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.index
}

case class EstablisherSkeletonEntity(id: EstablisherKindId) extends Establisher[EstablisherKindId] {

  override def index: Int = id.index

  override def name: String = ""

  override def isDeleted: Boolean = true

  override def isCompleted: Boolean = false

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = None

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = None
}

sealed trait Trustee[T] extends Entity[T]

case class TrusteeCompanyEntity(id: TrusteeCompanyDetailsId, name: String, isDeleted: Boolean,
                                isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int,
                                schemeType: Option[String]) extends Trustee[TrusteeCompanyDetailsId] {

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = mode match {
    case NormalMode | CheckMode =>
      Some(controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(id.index).url)
    case _ => None
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {

    val isSingleOrMaster: Boolean = schemeType.fold(false)(scheme => Seq("single", "master").exists(_.equals(scheme)))

    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Company, srn).url)
      case UpdateMode | CheckUpdateMode if !isSingleOrMaster =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Company, srn).url)
      case UpdateMode | CheckUpdateMode if isSingleOrMaster && noOfRecords > 1 =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Company, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.index
}

case class TrusteeIndividualEntity(id: TrusteeNameId, name: String, isDeleted: Boolean,
                                   isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int,
                                   schemeType: Option[String]) extends Trustee[TrusteeNameId] {

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = mode match {
    case NormalMode | CheckMode =>
      Some(controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(id.trusteeIndex).url)
    case _ => None
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] =
    Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.trusteeIndex,
      TrusteeKind.Individual, srn).url)

  override def index: Int = id.trusteeIndex
}

case class TrusteePartnershipEntity(id: TrusteePartnershipDetailsId, name: String, isDeleted: Boolean,
                                    isCompleted: Boolean, isNewEntity: Boolean, noOfRecords: Int,
                                    schemeType: Option[String]) extends Trustee[TrusteePartnershipDetailsId] {

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = {
    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.trustees.routes.PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(id.index).url)
      case _ => None
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = {

    val isSingleOrMaster: Boolean = schemeType.fold(false)(scheme => Seq("single", "master").exists(_.equals(scheme)))

    mode match {
      case NormalMode | CheckMode =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Partnership, srn).url)
      case UpdateMode | CheckUpdateMode if !isSingleOrMaster =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Partnership, srn).url)
      case UpdateMode | CheckUpdateMode if isSingleOrMaster && noOfRecords > 1 =>
        Some(controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index,
          TrusteeKind.Partnership, srn).url)
      case _ => None
    }
  }

  override def index: Int = id.index
}

case class TrusteeSkeletonEntity(id: TrusteeKindId) extends Trustee[TrusteeKindId] {
  override def name: String = ""

  override def isDeleted: Boolean = true

  override def isCompleted: Boolean = false

  override def editLink(mode: Mode, srn: Option[String]): Option[String] = None

  override def deleteLink(mode: Mode, srn: Option[String]): Option[String] = None

  override def index: Int = id.index
}
