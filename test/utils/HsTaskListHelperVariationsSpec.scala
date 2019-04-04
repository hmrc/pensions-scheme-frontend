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

import identifiers.{DeclarationDutiesId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId}
import models.{Link, NormalMode}
import utils.behaviours.HsTaskListHelperBehaviour
import viewmodels.SchemeDetailsTaskListSection

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour {
  "h1" must {
    "have the name of the scheme" in {
      val name = "scheme name 1"
      val userAnswers = UserAnswers().set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display \"Scheme details\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }

  "page title" must {
    "display \"Scheme details\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.pageTitle mustBe messages("messages__scheme_details__title")
    }
  }

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
    "not display when do you have working knowledge is false " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.workingKnowledge mustBe None
    }

    "not display when do you have working knowledge is true " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.workingKnowledge mustBe None
    }
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

