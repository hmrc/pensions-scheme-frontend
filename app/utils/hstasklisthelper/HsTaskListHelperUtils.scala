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


import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import models.register.Entity
import models._
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}
import identifiers.register.establishers.{company => establisherCompany}

trait HsTaskListHelperUtils extends Enumerable.Implicits {

  implicit val messages: Messages
  
  sealed trait Spoke
  case object EstablisherCompanyDetails extends Spoke
  case object EstablisherCompanyAddress extends Spoke
  case object EstablisherCompanyContactDetails extends Spoke
  case object EstablisherCompanyDirectors extends Spoke

  def createSpoke(answers: UserAnswers,
                  spokeName: Spoke,
                  mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {

    val isChangeLink = getCompleteFlag(answers, index, spokeName)
    val isComplete: Option[Boolean] = if (mode == NormalMode) isChangeLink else None
    println("\n\n\n isisChangeLink : "+isChangeLink)

    isChangeLink match {
      case Some(true) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
      case Some(false) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), isComplete)
      case None => EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), None)
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

  private def getCompleteFlag(answers: UserAnswers, index: Int, spokeName: Spoke): Option[Boolean] = spokeName match {
    case EstablisherCompanyDetails => {
      println("\n\n1..."+answers.get(establisherCompany.IsDetailsCompleteId(index)))
      answers.get(establisherCompany.IsDetailsCompleteId(index))
    }
    case EstablisherCompanyAddress
 => answers.get(establisherCompany.IsAddressCompleteId(index))
    case EstablisherCompanyContactDetails => answers.get(establisherCompany.IsContactDetailsCompleteId(index))
    case _ => None
  }

  private def getChangeLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", _)
    case EstablisherCompanyAddress => messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", _)
    case EstablisherCompanyContactDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getAddLinkText(spokeName: Spoke): String => String = spokeName match {
    case EstablisherCompanyDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", _)
    case EstablisherCompanyAddress => messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", _)
    case EstablisherCompanyContactDetails => messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", _)
    case EstablisherCompanyDirectors => messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getChangeLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  private def getAddLink(spokeName: Spoke)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case EstablisherCompanyDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyAddress => establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index)
    case EstablisherCompanyContactDetails => establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case EstablisherCompanyDirectors => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  def getEstablisherCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] =
    Seq(
      createSpoke(answers, EstablisherCompanyDetails, mode, srn, name, index),
      createSpoke(answers, EstablisherCompanyAddress, mode, srn, name, index),
      createSpoke(answers, EstablisherCompanyContactDetails, mode, srn, name, index),
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(index), EstablisherCompanyDirectors, mode, srn, name, index)
    )

}
