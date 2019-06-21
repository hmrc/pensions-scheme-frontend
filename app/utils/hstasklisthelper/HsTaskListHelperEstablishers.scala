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
import identifiers.register.establishers.company.{IsCompanyAddressCompleteId, IsCompanyContactDetailsCompleteId, IsCompanyDetailsCompleteId}
import models.{EntitySpoke, Index, Link, Mode, NormalMode}
import play.api.i18n.Messages
import play.api.mvc.Call
import utils.{Enumerable, UserAnswers}

trait HsTaskListHelperEstablishers  extends Enumerable.Implicits {

  implicit val messages: Messages

  private def getChangeLinkText(spokeName: String): String => String = spokeName match {
    case "establisherCompanyDetails" => messages ("messages__schemeTaskList__sectionEstablishersCompany_change_details", _)
    case "establisherCompanyAddress" => messages ("messages__schemeTaskList__sectionEstablishersCompany_change_address", _)
    case "establisherCompanyContactDetails" => messages ("messages__schemeTaskList__sectionEstablishersCompany_change_contact", _)
    case "establisherCompanyDirectors" => messages ("messages__schemeTaskList__sectionEstablishersCompany_change_directors", _)
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
    createSpoke(answers.get(IsCompanyDetailsCompleteId(index)),"establisherCompanyDetails", mode, srn, name, index),
    createSpoke(answers.get(IsCompanyAddressCompleteId(index)),"establisherCompanyAddress", mode, srn, name, index),
    createSpoke(answers.get(IsCompanyContactDetailsCompleteId(index)),"establisherCompanyContactDetails", mode, srn, name, index),
    createSpoke(Some(answers.allDirectorsAfterDelete(index).nonEmpty),"establisherCompanyDirectors", mode, srn, name, index)
  )

  private def createSpoke(isChangeLink: Option[Boolean],
                           spokeName: String,
                           mode: Mode, srn: Option[String], name: String, index: Int): EntitySpoke = {
    //TODO Speak to Mark about In Progress logic of directors and create alternate method to be used for setting isComplete flag for directors/partners
    val isComplete: Option[Boolean] = if(mode==NormalMode || spokeName!="establisherCompanyDirectors") isChangeLink else None

    isChangeLink match {
      case Some(true) => EntitySpoke(Link(getChangeLinkText(spokeName)(name), getChangeLink(spokeName)(mode, srn, index).url), isComplete)
      case _ => EntitySpoke(Link(getAddLinkText(spokeName)(name), getAddLink(spokeName)(mode, srn, index).url), isComplete)
    }
  }

}
