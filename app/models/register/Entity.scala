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

package models.register

import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models.Mode
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind

sealed trait Entity[ID] {
  def id: ID

  def name: String

  def isDeleted: Boolean

  def isCompleted: Boolean

  def editLink(mode: Mode, srn: Option[String]): String

  def deleteLink(mode: Mode, srn: Option[String]): String

  def index: Int
}

case class DirectorEntity(id: DirectorDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Entity[DirectorDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.establishers.company.director.routes.CheckYourAnswersController.onPageLoad(id.establisherIndex, id.directorIndex, mode, srn).url
    }
    else {
      controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(mode, id.establisherIndex, id.directorIndex, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(id.establisherIndex, id.directorIndex, mode, srn).url

  override def index: Int = id.directorIndex
}

case class PartnerEntity(id: PartnerDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Entity[PartnerDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.establishers.partnership.partner.routes.CheckYourAnswersController.onPageLoad(mode, id.establisherIndex, id.partnerIndex, srn).url
    }
    else {
      controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(mode, id.establisherIndex, id.partnerIndex, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController.onPageLoad(mode, id.establisherIndex, id.partnerIndex, srn).url

  override def index: Int = id.partnerIndex
}

sealed trait Establisher[T] extends Entity[T]

case class EstablisherCompanyEntity(id: EstablisherCompanyDetailsId,
                                    name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[EstablisherCompanyDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(mode, srn, id.index).url
    }
    else {
      controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(mode, srn, id.index).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index, EstablisherKind.Company, srn).url

  override def index: Int = id.index
}

case class EstablisherIndividualEntity(id: EstablisherDetailsId,
                                       name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[EstablisherDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(mode, id.index, srn).url
    }
    else {
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(mode, id.index, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index, EstablisherKind.Indivdual, srn).url

  override def index: Int = id.index
}

case class EstablisherPartnershipEntity(id: PartnershipDetailsId,
                                        name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[PartnershipDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.establishers.partnership.routes.CheckYourAnswersController.onPageLoad(mode, id.index, srn).url
    }
    else {
      controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(mode, id.index, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(mode, id.index, EstablisherKind.Partnership, srn).url

  override def index: Int = id.index
}

case class EstablisherSkeletonEntity(id: EstablisherKindId) extends Establisher[EstablisherKindId] {

  override def index: Int = id.index

  override def name: String = ""

  override def isDeleted: Boolean = true

  override def isCompleted: Boolean = false

  override def editLink(mode: Mode, srn: Option[String]): String = ""

  override def deleteLink(mode: Mode, srn: Option[String]): String = ""
}

sealed trait Trustee[T] extends Entity[T]

case class TrusteeCompanyEntity(id: TrusteeCompanyDetailsId,
                                name: String, isDeleted: Boolean, isCompleted: Boolean) extends Trustee[TrusteeCompanyDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(mode, id.index, srn).url
    }
    else {
      controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(mode, id.index, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index, TrusteeKind.Company, srn).url

  override def index: Int = id.index
}

case class TrusteeIndividualEntity(id: TrusteeDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Trustee[TrusteeDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(mode, id.index, srn).url
    }
    else {
      controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(mode, id.index, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index, TrusteeKind.Individual, srn).url

  override def index: Int = id.index
}

case class TrusteePartnershipEntity(id: TrusteePartnershipDetailsId,
                                    name: String, isDeleted: Boolean, isCompleted: Boolean) extends Trustee[TrusteePartnershipDetailsId] {
  override def editLink(mode: Mode, srn: Option[String]): String = {
    if (isCompleted) {
      controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(mode, id.index, srn).url
    }
    else {
      controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(mode, id.index, srn).url
    }
  }

  override def deleteLink(mode: Mode, srn: Option[String]): String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(mode, id.index, TrusteeKind.Partnership, srn).url

  override def index: Int = id.index
}

case class TrusteeSkeletonEntity(id: TrusteeKindId) extends Trustee[TrusteeKindId] {
  override def name: String = ""

  override def isDeleted: Boolean = true

  override def isCompleted: Boolean = false

  override def editLink(mode: Mode, srn: Option[String]): String = ""

  override def deleteLink(mode: Mode, srn: Option[String]): String = ""

  override def index: Int = id.index
}
