/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.register.establishers.partnership.partner.{routes => establisherPartnershipPartnerRoutes}
import controllers.register.establishers.partnership.{routes => establisherPartnershipRoutes}
import models.{Mode, TaskListLink}
import play.api.mvc.Call
import utils.UserAnswers
import viewmodels.Message

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

case object EstablisherCompanyDirectors extends Spoke {

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    Message("messages__schemeTaskList__add_directors", name),
    establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, index).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink( name, srn, "messages__schemeTaskList__change_directors", "messages__schemeTaskList__view_directors"),
    establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = changeLink(name)(mode, srn, index)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = None
}

case object EstablisherIndividualDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.CheckYourAnswersDetailsController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherIndividualDetailsComplete(index)
}

case object EstablisherIndividualAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.CheckYourAnswersAddressController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherIndividualAddressComplete(index)
}

case object EstablisherIndividualContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    establisherIndividualRoutes.CheckYourAnswersContactDetailsController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isEstablisherIndividualContactDetailsComplete(index)
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
    establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index, srn)

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

case object EstablisherPartnershipPartner extends Spoke {

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    Message("messages__schemeTaskList__add_partners", name),
    establisherPartnershipPartnerRoutes.WhatYouWillNeedPartnerController.onPageLoad(mode, index, srn).url
  )

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = TaskListLink(
    dynamicContentForChangeLink( name, srn, "messages__schemeTaskList__change_partners", "messages__schemeTaskList__view_partners"),
    establisherPartnershipRoutes.AddPartnersController.onPageLoad(mode, index, srn).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Int): TaskListLink = changeLink(name)(mode, srn, index)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = None
}

