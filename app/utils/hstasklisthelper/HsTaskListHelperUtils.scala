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
import controllers.register.establishers.partnership.partner.{routes => establisherPartnershipPartnerRoutes}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.establishers.partnership.{routes => establisherPartnershipRoutes}
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
      createDirectorPartnerSpoke(answers.allDirectorsAfterDelete(index, isHnSEnabled), EstablisherCompanyDirectors, mode, srn, name, index)
    )
  }

  def getEstablisherPartnershipSpokes(answers: UserAnswers, mode: Mode, srn: Option[String], name: String, index: Int): Seq[EntitySpoke] = {
    val isEstablisherNew = answers.get(IsEstablisherNewId(index)).getOrElse(false)
    Seq(
      createSpoke(answers, EstablisherPartnershipDetails, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherPartnershipAddress, mode, srn, name, index, isEstablisherNew),
      createSpoke(answers, EstablisherPartnershipContactDetails, mode, srn, name, index, isEstablisherNew)
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

  sealed trait Spoke {
    def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link
    def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link
    def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link
    def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean]
  }

  sealed trait DetailsSpoke extends Spoke {
    def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call
    def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

    override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__add_details", name),
      addLinkUrl(mode, srn, index).url
    )

    override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_details", name),
      changeLinkUrl(mode, srn, index).url
    )

    override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_details", name),
      addLinkUrl(mode, srn, index).url
    )
  }

  sealed trait AddressSpoke extends Spoke {
    def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call
    def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

    override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__add_address", name),
      addLinkUrl(mode, srn, index).url
    )

    override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_address", name),
      changeLinkUrl(mode, srn, index).url
    )

    override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_address", name),
      addLinkUrl(mode, srn, index).url
    )
  }

  sealed trait ContactDetailsSpoke extends Spoke {
    def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call
    def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call

    override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__add_contact_details", name),
      addLinkUrl(mode, srn, index).url
    )

    override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_contact_details", name),
      changeLinkUrl(mode, srn, index).url
    )

    override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_contact_details", name),
      addLinkUrl(mode, srn, index).url
    )
  }

  case object EstablisherCompanyDetails extends DetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherCompanyDetailsComplete(index, mode)
  }

  case object EstablisherCompanyAddress extends AddressSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherCompanyAddressComplete(index)
  }

  case object EstablisherCompanyContactDetails extends ContactDetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherCompanyContactDetailsComplete(index)
  }

  case object EstablisherCompanyDirectors extends Spoke{

    override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__add_directors", name),
      establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, index).url
    )

    override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = Link(
      messages("messages__schemeTaskList__change_directors", name),
      establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index).url
    )

    override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): Link = changeLink(name)(mode, srn, index)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = None
  }

  case object EstablisherPartnershipDetails extends DetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, index)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherPartnershipDetailsComplete(index)
  }

  case object EstablisherPartnershipAddress extends AddressSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, srn, index)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherPartnershipAddressComplete(index)
  }

  case object EstablisherPartnershipContactDetails extends ContactDetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      establisherPartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherPartnershipContactDetailsComplete(index)
  }

  case object TrusteeCompanyAddress extends AddressSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeCompanyAddressComplete(index)
  }

  case object TrusteeCompanyContactDetails extends ContactDetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeCompanyContactDetailsComplete(index)
  }

  case object TrusteeCompanyDetails extends DetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeCompanyDetailsComplete(index)
  }

  case object TrusteeIndividualAddress extends AddressSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeIndividualAddressComplete(index)
  }

  case object TrusteeIndividualContactDetails extends ContactDetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeIndividualContactDetailsComplete(index)
  }

  case object TrusteeIndividualDetails extends DetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeIndividualDetailsComplete(index)
  }

  case object TrusteePartnershipAddress extends AddressSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteePartnershipAddressComplete(index)
  }

  case object TrusteePartnershipContactDetails extends ContactDetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteePartnershipContactDetailsComplete(index)
  }

  case object TrusteePartnershipDetails extends DetailsSpoke {
    override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index, srn)

    override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
      trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)

    override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteePartnershipDetailsComplete(index)
  }
}
