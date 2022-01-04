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

package utils.hstasklisthelper.spokes

import controllers.register.establishers.company.director.{routes => establisherCompanyDirectorRoutes}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.establishers.individual.{routes => establisherIndividualRoutes}
import controllers.register.establishers.partnership.partner.{routes => establisherPartnershipPartnerRoutes}
import controllers.register.establishers.partnership.{routes => establisherPartnershipRoutes}
import models.Index.indexToInt
import models.{Index, Mode, TaskListLink}
import play.api.mvc.Call
import utils.UserAnswers
import viewmodels.Message

case object EstablisherCompanyDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index.getOrElse(Index(0)))

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index.getOrElse(Index(0)))

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherCompanyDetailsComplete(indexToInt(index.getOrElse(Index(0))), mode)
}

case object EstablisherCompanyAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index.getOrElse(Index(0)))

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index.getOrElse(Index(0)))

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherCompanyAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherCompanyContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index.getOrElse
    (Index(0)))

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index.getOrElse
    (Index(0)))

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherCompanyContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherCompanyDirectors extends Spoke {

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_directors", name),
    establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, index.getOrElse(Index(0)
    )).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    changeLink(name)(mode, srn, index)

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
    dynamicLinkText(name, srn,
      registrationLinkText = "messages__schemeTaskList__change_directors",
      variationsLinkText = "messages__schemeTaskList__view_directors"),
    establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index.getOrElse(Index(0))).url
  )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] = None
}

case object EstablisherIndividualDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index.getOrElse(Index(0))
      , srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.CheckYourAnswersDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherIndividualDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherIndividualAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index.getOrElse(Index(0))
      , srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.CheckYourAnswersAddressController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherIndividualAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherIndividualContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index.getOrElse
    (Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherIndividualRoutes.CheckYourAnswersContactDetailsController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherIndividualContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherPartnershipDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, index.getOrElse
    (Index(0)))

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index.getOrElse(Index
    (0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherPartnershipDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherPartnershipAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index.getOrElse(Index
    (0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index.getOrElse(Index
    (0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherPartnershipAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherPartnershipContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index.getOrElse
    (Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    establisherPartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index.getOrElse
    (Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isEstablisherPartnershipContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object EstablisherPartnershipPartner extends Spoke {

  override def addLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
    Message("messages__schemeTaskList__add_partners", name),
    establisherPartnershipPartnerRoutes.WhatYouWillNeedPartnerController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn).url
  )

  override def incompleteChangeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    changeLink(name)(mode, srn, index)

  override def changeLink(name: String)(mode: Mode, srn: Option[String], index: Option[Index]): TaskListLink =
    TaskListLink(
    dynamicLinkText(name, srn,
      registrationLinkText = "messages__schemeTaskList__change_partners",
      variationsLinkText = "messages__schemeTaskList__view_partners"),
    establisherPartnershipRoutes.AddPartnersController.onPageLoad(mode, indexToInt(index.getOrElse(Index(0))), srn).url
  )

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] = None
}

