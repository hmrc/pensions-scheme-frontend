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

import config.FeatureSwitchManagementService
import identifiers._
import models._
import utils.behaviours.HsTaskListHelperBehaviour
import utils.hstasklisthelper.{HsTaskListHelper, HsTaskListHelperRegistration}
import viewmodels.{SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListSection}

class HsTaskListHelperRegistrationSpec extends HsTaskListHelperBehaviour with Enumerable.Implicits {

  private val fakeFeatureManagementService = new FakeFeatureSwitchManagementService(false)
  override val createTaskListHelper:
    (UserAnswers, FeatureSwitchManagementService) => HsTaskListHelper = (ua, fs) => new HsTaskListHelperRegistration(ua, fs)

  "h1" must {
    "display appropriate heading" in {
      val name = "scheme name 1"
      val userAnswers = UserAnswers().set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display appropriate text" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }

  "h3" must {
    "display Before You Start" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.taskList.h3 mustBe Some(messages("messages__schemeTaskList__before_you_start_header"))
    }
  }

  "about header" must {
    "display About Scheme name" in {
      val schemeName = "test scheme"
      val userAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.taskList.aboutHeader mustBe messages("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display Pension scheme details" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.taskList.pageTitle mustBe messages("messages__schemeTaskList__title")
    }
  }

  "beforeYouStartSection " must {
    behave like beforeYouStartSection(
      new HsTaskListHelperRegistration(_, fakeFeatureManagementService),
      beforeYouStartLinkText,
      NormalMode,
      None
    )
  }

  "aboutSection " must {
    "return the the Seq of members, bank details and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(false).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(false).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(false)
        )
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(false), Link(aboutMembersLinkText,
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), None),
          SchemeDetailsTaskListSection(Some(false), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url), None),
          SchemeDetailsTaskListSection(Some(false), Link(aboutBankDetailsLinkText,
            controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url), None)
        )
    }

    "return the the Seq of members, bank details and benefits section with " +
      "links of the cya pages of individual sub sections when completed " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(true).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(true)
        )
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(true), Link(aboutMembersLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(aboutBankDetailsLinkText,
            controllers.routes.CheckYourAnswersBankDetailsController.onPageLoad().url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    "not display when do you have working knowledge is true " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.workingKnowledgeSection(userAnswers) mustBe None
    }

    "display and link should go to what you will need page when do you have working knowledge is false and section not completed " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(false).flatMap(
        _.set(IsWorkingKnowledgeCompleteId)(false)
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.workingKnowledgeSection(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(false), Link(workingKnowledgeLinkText,
          controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url), None)
    }

    "display and link should go to cya page when do you have working knowledge is false and section is completed " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(false).flatMap(
        _.set(IsWorkingKnowledgeCompleteId)(true)
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers, fakeFeatureManagementService)
      helper.workingKnowledgeSection(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(true), Link(workingKnowledgeLinkText,
          controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url), None)
    }
  }

  "addEstablisherHeader " must {

    behave like addEstablisherHeader(NormalMode, None)
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader(NormalMode, None)

    "not display when do you have any trustees is false " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, NormalMode, Some("srn")) mustBe None
    }
  }

  "establishers" must {
    behave like establishersSectionHnS(NormalMode, None)
  }

  "trustees" must {

    behave like trusteesSectionHnS(NormalMode, None)
  }

  "declaration" must {
    "have a declaration section" in {
      val userAnswers = answersData().asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }

    behave like declarationSection()

    "not have link when about bank details section not completed" in {
      val userAnswers = answersData(isCompleteAboutBank = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }

    "not have link when working knowledge section not completed" in {
      val userAnswers = answersData(isCompleteWk = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }
  }
}

