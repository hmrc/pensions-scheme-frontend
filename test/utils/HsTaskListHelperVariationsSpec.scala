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

package utils

import identifiers.{IsAboutBankDetailsCompleteId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId}
import models.NormalMode
import utils.behaviours.HsTaskListHelperBehaviour
import viewmodels.{Link, SchemeDetailsTaskListSection}

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour {

  "beforeYouStartSection " must {
    behave like beforeYouStartSection()
  }

  "aboutSection " must {
    "return the the Seq of members and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(false).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(false)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(false), Link(aboutMembersLinkText,
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), None),
          SchemeDetailsTaskListSection(Some(false), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url), None)
        )
    }

    "return the the Seq of members and benefits section with " +
      "links of the cya pages of individual sub sections when completed " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(true)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(true), Link(aboutMembersLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, None).url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    behave like workingKnowledgeSection()
  }

  "addEstablisherHeader " must {

    behave like addEstablisherHeader()
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader()
  }

  "establishers" must {

    behave like establishersSection()
  }

  "trustees" must {

    behave like trusteesSection()
  }

  "declarationEnabled" must {

    behave like declarationEnabled()
  }

  "declarationLink" must {

    behave like declarationLink()
  }
}

