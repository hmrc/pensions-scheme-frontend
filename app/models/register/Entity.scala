/*
 * Copyright 2018 HM Revenue & Customs
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

import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.establishers.partnership.partner.PartnerDetailsId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.NormalMode
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind

sealed trait Entity[ID] {
  def id: ID
  def name: String
  def isDeleted: Boolean
  def isCompleted: Boolean
  def editLink: String
  def deleteLink: String
  def index: Int
}

case class DirectorEntity(id: DirectorDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Entity[DirectorDetailsId] {
  override def editLink: String = {
    if (isCompleted){
      controllers.register.establishers.company.director.routes.CheckYourAnswersController.onPageLoad(id.establisherIndex, id.directorIndex).url
    }
    else{
      controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, id.establisherIndex, id.directorIndex).url
    }
  }

  override def deleteLink: String =
    controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(id.establisherIndex, id.directorIndex).url

  override def index: Int = id.directorIndex
}

case class PartnerEntity(id: PartnerDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Entity[PartnerDetailsId] {
  override def editLink: String = {
    if (isCompleted){
      controllers.register.establishers.partnership.partner.routes.CheckYourAnswersController.onPageLoad(id.establisherIndex, id.partnerIndex).url
    }
    else{
      controllers.register.establishers.partnership.partner.routes.PartnerDetailsController.onPageLoad(NormalMode, id.establisherIndex, id.partnerIndex).url
    }
  }

  override def deleteLink: String =
    controllers.register.establishers.partnership.partner.routes.ConfirmDeletePartnerController.onPageLoad(id.establisherIndex, id.partnerIndex).url

  override def index: Int = id.partnerIndex
}

sealed trait Establisher[T] extends Entity[T]

case class EstablisherCompanyEntity(id: EstablisherCompanyDetailsId,
                                    name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[EstablisherCompanyDetailsId] {
  override def editLink: String = {
    if (isCompleted) {
      controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(id.index).url
    }
    else {
      controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id.index).url
    }
  }

  override def deleteLink: String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(id.index, EstablisherKind.Company).url

  override def index: Int = id.index
}

case class EstablisherIndividualEntity(id: EstablisherDetailsId,
                                       name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[EstablisherDetailsId] {
  override def editLink: String = {
    if(isCompleted){
      controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(id.index).url
    }
    else{
      controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, id.index).url
    }
  }

  override def deleteLink: String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(id.index, EstablisherKind.Indivdual).url

  override def index: Int = id.index
}

case class EstablisherPartnershipEntity(id: PartnershipDetailsId,
                                        name: String, isDeleted: Boolean, isCompleted: Boolean) extends Establisher[PartnershipDetailsId] {
  override def editLink: String = {
    if (isCompleted) {
      controllers.register.establishers.partnership.routes.CheckYourAnswersController.onPageLoad(id.index).url
    }
    else {
      controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, id.index).url
    }
  }

  override def deleteLink: String =
    controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(id.index, EstablisherKind.Partnership).url

  override def index: Int = id.index
}

sealed trait Trustee[T] extends Entity[T]

case class TrusteeCompanyEntity(id: TrusteeCompanyDetailsId,
                                name: String, isDeleted: Boolean, isCompleted: Boolean) extends Trustee[TrusteeCompanyDetailsId] {
  override def editLink: String = {
    if(isCompleted){
      controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(id.index).url
    }
    else {
      controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, id.index).url
    }
  }

  override def deleteLink: String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(id.index, TrusteeKind.Company).url

  override def index: Int = id.index
}

case class TrusteeIndividualEntity(id: TrusteeDetailsId, name: String, isDeleted: Boolean, isCompleted: Boolean) extends Trustee[TrusteeDetailsId] {
  override def editLink: String = {
    if(isCompleted){
      controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(id.index).url
    }
    else{
      controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, id.index).url
    }
  }

  override def deleteLink: String =
    controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(id.index, TrusteeKind.Individual).url

  override def index: Int = id.index
}
