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
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteeCompany}
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

  case object TrusteeCompanyDetails extends Spoke

  case object TrusteeCompanyAddress extends Spoke

  case object TrusteeCompanyContactDetails extends Spoke

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
    case TrusteeCompanyDetails => answers.isTrusteeCompanyDetailsComplete(index)
    case TrusteeCompanyAddress => answers.isTrusteeCompanyAddressComplete(index)
    case TrusteeCompanyContactDetails => answers.isTrusteeCompanyContactDetailsComplete(index)
    case _ => None
  }

  private def getChangeLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails | TrusteeCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", _)
    case EstablisherCompanyAddress | TrusteeCompanyAddress => messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", _)
    case EstablisherCompanyContactDetails | TrusteeCompanyContactDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getAddLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails | TrusteeCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", _)
    case EstablisherCompanyAddress | TrusteeCompanyAddress => messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", _)
    case EstablisherCompanyContactDetails | TrusteeCompanyContactDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getChangeLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)
    case TrusteeCompanyDetails => trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, srn)
    case TrusteeCompanyAddress => trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, index, srn)
    case TrusteeCompanyContactDetails => trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, index, srn)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  private def getAddLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, index)
    case TrusteeCompanyDetails => trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index,srn)
    case TrusteeCompanyAddress => trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index, srn)
    case TrusteeCompanyContactDetails => trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index, srn)
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

  def getTrusteeCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isTrusteeNew = answers.get(IsTrusteeNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, TrusteeCompanyDetails, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeCompanyAddress, mode, srn, name, index, isTrusteeNew),
      createSpoke(answers, TrusteeCompanyContactDetails, mode, srn, name, index, isTrusteeNew)
    )
  }
}
