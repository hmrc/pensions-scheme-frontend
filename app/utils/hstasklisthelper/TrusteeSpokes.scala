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

import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import models.Mode
import play.api.mvc.Call
import utils.UserAnswers

case object TrusteeCompanyDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeCompanyDetailsComplete(index)
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

case object TrusteeIndividualDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteeIndividualDetailsComplete(index)
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

case object TrusteePartnershipDetails extends DetailsSpoke {
  override def addLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index, srn)

  override def changeLinkUrl(mode: Mode, srn: Option[String], index: Int): Call =
    trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index, srn)

  override def completeFlag(answers: UserAnswers, index: Int, mode: Mode): Option[Boolean] = answers.isTrusteePartnershipDetailsComplete(index)
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


