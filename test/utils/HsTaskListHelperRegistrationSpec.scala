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

import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import identifiers._
import identifiers.register.establishers.individual.EstablisherNameId
import models._
import models.address.Address
import models.person.PersonName
import utils.behaviours.HsTaskListHelperBehaviour
import utils.hstasklisthelper.{HsTaskListHelper, HsTaskListHelperRegistration}
import viewmodels.{SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListHeader, SchemeDetailsTaskListSection}

class HsTaskListHelperRegistrationSpec extends HsTaskListHelperBehaviour with Enumerable.Implicits {

  override val createTaskListHelper:
    (UserAnswers) => HsTaskListHelper = (ua) => new HsTaskListHelperRegistration(ua)

  "h1" must {
    "display appropriate heading" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.schemeName(name)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display appropriate text" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }

  "h3" must {
    "display Before You Start" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.taskList.h3 mustBe Some(messages("messages__schemeTaskList__before_you_start_header"))
    }
  }

  "about header" must {
    "display About Scheme name" in {
      val schemeName = "test scheme"
      val userAnswers = userAnswersWithSchemeName.set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.taskList.aboutHeader mustBe messages("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display Pension scheme details" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.taskList.pageTitle mustBe messages("messages__schemeTaskList__title")
    }
  }

  "beforeYouStartSection " must {
    behave like beforeYouStartSection(
      new HsTaskListHelperRegistration(_),
      beforeYouStartLinkText,
      NormalMode,
      None
    )
  }

  "aboutSection " must {
    "return the the Seq of members, bank details and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = userAnswersWithSchemeName.currentMembers(Members.One).
        ukBankAccount(ukBankAccount = true).occupationalPensionScheme(isOccupational = true)
      val helper = new HsTaskListHelperRegistration(userAnswers)
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
      val userAnswers = userAnswersWithSchemeName.currentMembers(Members.One).futureMembers(Members.One).
        ukBankAccount(ukBankAccount = false).occupationalPensionScheme(isOccupational = true).
        investmentRegulated(isInvestmentRegulated = true).typeOfBenefits(TypeOfBenefits.MoneyPurchase).benefitsSecuredByInsurance(isInsured = false)

      val helper = new HsTaskListHelperRegistration(userAnswers)
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
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.workingKnowledgeSection(userAnswers) mustBe None
    }

    "display and link should go to what you will need page when do you have working knowledge is false and section not completed " in {
      val userAnswers = setCompleteWorkingKnowledge(isComplete = false, userAnswersWithSchemeName).declarationDuties(haveWorkingKnowledge = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.workingKnowledgeSection(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(false), Link(workingKnowledgeLinkText,
          controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url), None)
    }

    "display and link should go to cya page when do you have working knowledge is false and section is completed " in {
      val userAnswers = setCompleteWorkingKnowledge(isComplete = true, userAnswersWithSchemeName).declarationDuties(haveWorkingKnowledge = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.workingKnowledgeSection(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(true), Link(workingKnowledgeLinkText,
          controllers.routes.AdviserCheckYourAnswersController.onPageLoad().url), None)
    }
  }

  "addEstablisherHeader " must {

    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = userAnswersWithSchemeName
      val helper = createTaskListHelper(userAnswers)
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, userAnswers.allEstablishers(NormalMode).size, None).url)), None)
    }

    "return the link to add establisher page when establishers are added" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url)), None)
    }
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader(NormalMode, None)

    "not display when do you have any trustees is false " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, Some("srn")) mustBe None
    }
  }

  "establishers" must {
    behave like establishersSection(NormalMode, None)
  }

  "trustees" must {

    behave like trusteesSection(NormalMode, None)

    "return the seq of trustees sub sections when all spokes are completed" in {
      val userAnswers = allAnswers
      val helper = createTaskListHelper(userAnswers)
      helper.trustees(userAnswers, NormalMode, None) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(NormalMode, 0, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(NormalMode, 0, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(NormalMode, 0, None).url), Some(true))
            ), Some("test company")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "firstName lastName"),
              trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(NormalMode, 1, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(NormalMode, 1, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(NormalMode, 1, None).url), Some(true))
            ), Some("firstName lastName")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test partnership"),
              trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(NormalMode, 2, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(NormalMode, 2, None).url), Some(true)),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(NormalMode, 2, None).url), Some(true))
            ), Some("test partnership"))
        )
    }
  }

  "declaration" must {
    "have a declaration section" in {
      val userAnswers = answersData()
      val helper = createTaskListHelper(userAnswers)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }

    behave like declarationSection()

    "not have link when about bank details section not completed" in {
      val userAnswers = answersData(isCompleteAboutBank = false)
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when working knowledge section not completed" in {
      val userAnswers = answersData(isCompleteWk = false)
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers), userAnswers)
    }
  }
}

