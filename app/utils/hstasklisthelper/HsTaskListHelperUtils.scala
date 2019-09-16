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


import controllers.register.establishers.company.director.{routes => establisherCompanyDirectorRoutes}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.establishers.individual.{routes => establisherIndividualRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.IsTrusteeNewId
import models._
import models.register.Entity
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

trait HsTaskListHelperUtils extends Enumerable.Implicits {

  implicit val messages: Messages
  protected val isHnSEnabled: Boolean

  sealed trait Spoke

  case object EstablisherCompanyDetails extends Spoke
  case object EstablisherCompanyAddress extends Spoke
  case object EstablisherCompanyContactDetails extends Spoke
  case object EstablisherCompanyDirectors extends Spoke

  case object EstablisherIndividualDetails extends Spoke
  case object EstablisherIndividualAddress extends Spoke
  case object EstablisherIndividualContactDetails extends Spoke

  case object TrusteeCompanyDetails extends Spoke
  case object TrusteeCompanyAddress extends Spoke
  case object TrusteeCompanyContactDetails extends Spoke

  case object TrusteeIndividualDetails extends Spoke
  case object TrusteeIndividualAddress extends Spoke
  case object TrusteeIndividualContactDetails extends Spoke

  case object TrusteePartnershipDetails extends Spoke
  case object TrusteePartnershipAddress extends Spoke
  case object TrusteePartnershipContactDetails extends Spoke


  def createSpoke(answers: UserAnswers,
                  spokeName: Spoke,
                  mode: Mode, srn: Option[String], name: String, index: Int, isNew: Boolean): EntitySpoke = {

    val isChangeLink = getCompleteFlag(answers, index, spokeName, mode)
    val isComplete: Option[Boolean] = if (mode == NormalMode) isChangeLink else None

    (isChangeLink, isNew) match {
      case (_, false) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), None)
      case (Some(true), _) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
      case (Some(false), _) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), isComplete)
      case _ => EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), None)
    }
  }

  def createDirectorPartnerSpoke(entityList: Seq[Entity[_]],
                                 spokeName: Spoke,
                                 mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {

    val isComplete: Option[Boolean] = if (mode == NormalMode && entityList.nonEmpty) Some(entityList.forall(_.isCompleted)) else None

    if (entityList.isEmpty)
      EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), None)
    else
      EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
  }

  private def getCompleteFlag(answers: UserAnswers, index: Int, spokeName: Spoke, mode: Mode): Option[Boolean] = spokeName match {
    case EstablisherCompanyDetails => answers.isEstablisherCompanyDetailsComplete(index, mode)
    case EstablisherCompanyAddress => answers.isEstablisherCompanyAddressComplete(index)
    case EstablisherCompanyContactDetails => answers.isEstablisherCompanyContactDetailsComplete(index)
      
    case EstablisherIndividualDetails => answers.isEstablisherIndividualDetailsComplete(index)
    case EstablisherIndividualAddress => answers.isEstablisherIndividualAddressComplete(index)
    case EstablisherIndividualContactDetails => answers.isEstablisherIndividualContactDetailsComplete(index)
      
    case TrusteeCompanyDetails => answers.isTrusteeCompanyDetailsComplete(index)
    case TrusteeCompanyAddress => answers.isTrusteeCompanyAddressComplete(index)
    case TrusteeCompanyContactDetails => answers.isTrusteeCompanyContactDetailsComplete(index)
      
    case TrusteeIndividualDetails => answers.isTrusteeIndividualDetailsComplete(index)
    case TrusteeIndividualAddress => answers.isTrusteeIndividualAddressComplete(index)
    case TrusteeIndividualContactDetails => answers.isTrusteeIndividualContactDetailsComplete(index)
      
    case TrusteePartnershipDetails => answers.isTrusteePartnershipDetailsComplete(index)
    case TrusteePartnershipAddress => answers.isTrusteePartnershipAddressComplete(index)
    case TrusteePartnershipContactDetails => answers.isTrusteePartnershipContactDetailsComplete(index)
    case _ => None
  }

  private def getChangeLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails | TrusteeCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", _)
    case EstablisherIndividualDetails | TrusteeIndividualDetails | TrusteePartnershipDetails => messages("messages__schemeTaskList__change_details",  _)

    case EstablisherCompanyAddress | EstablisherIndividualAddress | TrusteeCompanyAddress | TrusteeIndividualAddress | TrusteePartnershipAddress =>
      messages("messages__schemeTaskList__change_address", _)

    case EstablisherCompanyContactDetails | EstablisherIndividualContactDetails |
         TrusteeCompanyContactDetails | TrusteeIndividualContactDetails | TrusteePartnershipContactDetails =>
      messages("messages__schemeTaskList__change_contact", _)

    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getAddLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails | TrusteeCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", _)
    case EstablisherIndividualDetails | TrusteeIndividualDetails | TrusteePartnershipDetails => messages("messages__schemeTaskList__add_details",  _)

    case EstablisherCompanyAddress | EstablisherIndividualAddress | TrusteeCompanyAddress | TrusteeIndividualAddress | TrusteePartnershipAddress =>
      messages("messages__schemeTaskList__add_address", _)

    case EstablisherCompanyContactDetails | EstablisherIndividualContactDetails |
         TrusteeCompanyContactDetails | TrusteeIndividualContactDetails | TrusteePartnershipContactDetails =>
      messages("messages__schemeTaskList__add_contact", _)

    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getChangeLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)

    case EstablisherIndividualDetails => establisherIndividualRoutes.CheckYourAnswersDetailsController.onPageLoad(mode, index, srn)
    case EstablisherIndividualAddress => establisherIndividualRoutes.CheckYourAnswersAddressController.onPageLoad(mode, index, srn)
    case EstablisherIndividualContactDetails => establisherIndividualRoutes.CheckYourAnswersContactDetailsController.onPageLoad(mode, index, srn)

    case TrusteeCompanyDetails => trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, srn)
    case TrusteeCompanyAddress => trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, index, srn)
    case TrusteeCompanyContactDetails => trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, index, srn)
      
    case TrusteeIndividualDetails => trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, index, srn)
    case TrusteeIndividualAddress => trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn)
    case TrusteeIndividualContactDetails => trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, index, srn)
      
    case TrusteePartnershipDetails => trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)
    case TrusteePartnershipAddress => trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)
    case TrusteePartnershipContactDetails => trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index, srn)
      
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  private def getAddLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, index)
    
    case EstablisherIndividualDetails => establisherIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index,srn) //change the route
    case EstablisherIndividualAddress => establisherIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index, srn) //change the route
    case EstablisherIndividualContactDetails => establisherIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index, srn) //change the route
    
    case TrusteeCompanyDetails => trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index,srn)
    case TrusteeCompanyAddress => trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index, srn)
    case TrusteeCompanyContactDetails => trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index, srn)
    
    case TrusteeIndividualDetails => trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index,srn) //change the route
    case TrusteeIndividualAddress => trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index, srn) //change the route
    case TrusteeIndividualContactDetails => trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index, srn) //change the route
    
    case TrusteePartnershipDetails => trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index,srn)
    case TrusteePartnershipAddress => trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index, srn)
    case TrusteePartnershipContactDetails => trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index, srn)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  def getEstablisherCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherCompanyDetails, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherCompanyAddress, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherCompanyContactDetails, mode, srn, name, index, isEstablisherNew),
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(index, isHnSEnabled), EstablisherCompanyDirectors, mode, srn, name, index)
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
