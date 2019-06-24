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

  def createSpoke(answers: UserAnswers,
                  spokeName: String,
                  mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {

    val isChangeLink = getCompleteFlag(answers, index, spokeName)
    val isComplete: Option[Boolean] = if (mode == NormalMode) isChangeLink else None

    isChangeLink match {
      case Some(true) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
      case _ => EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), isComplete)
    }
  }

  def createDirectorPartnerSpoke(entityList: Seq[Entity[_]],
                                 spokeName: String,
                                 mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {

    val isChangeLink = entityList.nonEmpty
    val isComplete: Option[Boolean] = if (mode == NormalMode && entityList.nonEmpty) Some(entityList.exists(!_.isCompleted)) else None

    if (isChangeLink)
      EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
    else
      EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), None)
  }

  private def getCompleteFlag(answers: UserAnswers, index: Int, spokeName: String): Option[Boolean] = spokeName match {
    case "establisherCompanyDetails" => answers.get(establisherCompany.IsCompanyDetailsCompleteId(index))
    case "establisherCompanyAddress" => answers.get(establisherCompany.IsCompanyAddressCompleteId(index))
    case "establisherCompanyContactDetails" => answers.get(establisherCompany.IsCompanyContactDetailsCompleteId(index))
    case _ => None
  }

  private def getChangeLinkText(spokeName: String): String => String = spokeName match {
    case "establisherCompanyDetails" => messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", _)
    case "establisherCompanyAddress" => messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", _)
    case "establisherCompanyContactDetails" => messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", _)
    case "establisherCompanyDirectors" => messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getAddLinkText(spokeName: String): String => String = spokeName match {
    case "establisherCompanyDetails" => messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", _)
    case "establisherCompanyAddress" => messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", _)
    case "establisherCompanyContactDetails" => messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", _)
    case "establisherCompanyDirectors" => messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", _)
    case _ => (_: String) => s"Not found link text for spoke $spokeName"
  }

  private def getChangeLink(spokeName: String)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case "establisherCompanyDetails" => establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index)
    case "establisherCompanyAddress" => establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index)
    case "establisherCompanyContactDetails" => establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case "establisherCompanyDirectors" => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  private def getAddLink(spokeName: String)(mode: Mode, srn: Option[String], index: Index): Call = spokeName match {
    case "establisherCompanyDetails" => establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index)
    case "establisherCompanyAddress" => establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index)
    case "establisherCompanyContactDetails" => establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index)
    case "establisherCompanyDirectors" => establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index)
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  def getEstablisherCompanySpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] =
    Seq(
      createSpoke(answers,"establisherCompanyDetails", mode, srn, name, index),
      createSpoke(answers,"establisherCompanyAddress", mode, srn, name, index),
      createSpoke(answers,"establisherCompanyContactDetails", mode, srn, name, index),
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(index),"establisherCompanyDirectors", mode, srn, name, index)
    )

}
