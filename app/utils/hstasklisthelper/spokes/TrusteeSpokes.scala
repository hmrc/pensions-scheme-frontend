/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import models.Index.indexToInt
import models.{Index, Mode}
import play.api.mvc.Call
import utils.UserAnswers

case object TrusteeCompanyDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeCompanyDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteeCompanyAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeCompanyAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteeCompanyContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeCompanyContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteeIndividualDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeIndividualDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteeIndividualAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, index.getOrElse(Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeIndividualAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteeIndividualContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index.getOrElse(Index
    (0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, index.getOrElse(Index
    (0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteeIndividualContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteePartnershipDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteePartnershipDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteePartnershipAddress extends AddressSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index.getOrElse(Index(0)),
      srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteePartnershipAddressComplete(indexToInt(index.getOrElse(Index(0))))
}

case object TrusteePartnershipContactDetails extends ContactDetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index.getOrElse
    (Index(0)), srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Option[Index]): Call =
    trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index.getOrElse
    (Index(0)), srn)

  override def completeFlag(answers: UserAnswers, index: Option[Index], mode: Mode): Option[Boolean] =
    answers.isTrusteePartnershipContactDetailsComplete(indexToInt(index.getOrElse(Index(0))))
}


