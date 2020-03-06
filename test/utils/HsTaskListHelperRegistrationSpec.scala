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

package utils

import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import identifiers._
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import models.person.PersonName
import models.register.SchemeType
import utils.behaviours.HsTaskListHelperBehaviour
import utils.hstasklisthelper.HsTaskListHelperRegistration
import viewmodels.{SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListHeader, SchemeDetailsTaskListSection}

class HsTaskListHelperRegistrationSpec extends HsTaskListHelperBehaviour with Enumerable.Implicits {

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
    "return the before you start section correctly linking to scheme name page when not completed " in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.beforeYouStartSection(userAnswers) mustBe SchemeDetailsTaskListSection(
        isCompleted = Some(false),
        link = Link(
          messages("messages__schemeTaskList__before_you_start_link_text", schemeName),
          controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
        ),
        header = None
      )
    }

    "return the before you start section correctly linking to cya page when completed " in {
      val userAnswers = setCompleteBeforeYouStart(isComplete = true, UserAnswers()).schemeName(schemeName)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.beforeYouStartSection(userAnswers) mustBe SchemeDetailsTaskListSection(
        isCompleted = Some(true),
        link = Link(
          messages("messages__schemeTaskList__before_you_start_link_text", schemeName),
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, None).url
        ),
        header = None
      )
    }
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
      val helper = new HsTaskListHelperRegistration(userAnswers) // createTaskListHelper(userAnswers)
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController
            .onPageLoad(NormalMode, userAnswers.allEstablishers(NormalMode).size, None).url)), None)
    }

    "return the link to add establisher page when establishers are added" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url)), None)
    }
  }

  "addTrusteeHeader " must {
    "display correct link data when 2 trustees exist " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers) //createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addDeleteTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url)), None)
    }

    "display correct link data when trustee is optional and no trustee exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url)), None, None)
    }

    "display correct link data when trustee is mandatory and no trustees exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.MasterTrust).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url)), None,
          None)
    }


    s"display and link should go to trustee kind page when do you have any trustees is true and no trustees are added" in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url)), None)
    }

    "display and link should go to add trustees page when do you have any trustees is not present" +
      s"and trustees are added and completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url)), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      s"and trustees are added and not completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName"))
        .asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url)), None)
    }

    "not display when do you have any trustees is false " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers, NormalMode, Some("srn")) mustBe None
    }
  }

  "establishers" must {
    "return the seq of establishers sub sections" in {
      val userAnswers = establisherCompany()
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.establishersSection(userAnswers, NormalMode, None) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, None, 0).url), Some(false)),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, None, 0).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, None, 0).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_directors", "test company"),
                controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController.onPageLoad(NormalMode, None, 0).url), None)
            ), Some("test company"))
        )
    }
  }

  "trustees" must {

    "return the seq of trustees sub sections when all spokes are uninitiated" in {
      val userAnswers = trusteeCompany(false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.trusteesSection(userAnswers, NormalMode, None) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(NormalMode, 0, None).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(NormalMode, 0, None).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(NormalMode, 0, None).url), None)
            ), Some("test company"))
        )
    }

    "return the seq of trustees sub sections when all spokes are completed" in {
      val userAnswers = allAnswers
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.trusteesSection(userAnswers, NormalMode, None) mustBe
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

    protected def mustNotHaveDeclarationLink(helper: HsTaskListHelperRegistration, userAnswers: UserAnswers): Unit =
      helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe None)

  private def mustHaveDeclarationLinkEnabled(helper: HsTaskListHelperRegistration, userAnswers: UserAnswers, url: Option[String] = None): Unit = {
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe
      Some(Link(declarationLinkText, url.getOrElse(controllers.register.routes.DeclarationController.onPageLoad().url))))
  }

  "declaration" must {
    "have a declaration section" in {
      val userAnswers = answersData()
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }


    "have link when all the sections are completed without trustees and do you have trustees is false " in {
      val userAnswers = setCompleteBeforeYouStart(isComplete = true,
        setCompleteMembers(isComplete = true,
          setCompleteBank(isComplete = true,
            setCompleteBenefits(isComplete = true,
              setCompleteWorkingKnowledge(isComplete = true,
                allAnswers.set(InsuranceDetailsChangedId)(value = true).asOpt.value
              )
            )
          )))
      val helper = new HsTaskListHelperRegistration(userAnswers)
      mustHaveDeclarationLinkEnabled(helper, userAnswers)
    }

    "not have link when about bank details section not completed" in {
      val userAnswers = answersData(isCompleteAboutBank = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      mustNotHaveDeclarationLink(helper, userAnswers)
    }

    "not have link when working knowledge section not completed" in {
      val userAnswers = answersData(isCompleteWk = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      mustNotHaveDeclarationLink(helper, userAnswers)
    }
  }
}

