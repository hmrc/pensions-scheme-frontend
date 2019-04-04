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

import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.IsTrusteeCompleteId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{DeclarationDutiesId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId}
import models.person.PersonDetails
import models.{CompanyDetails, Link, NormalMode, PartnershipDetails}
import org.joda.time.LocalDate
import utils.behaviours.HsTaskListHelperBehaviour
import viewmodels.SchemeDetailsTaskListSection

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour {
  override val createTaskListHelper:UserAnswers => HsTaskListHelper = ua => new HsTaskListHelperVariations(ua)
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
  "h3" must {
    "display \"Scheme Information\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.h3 mustBe Some(messages("messages__schemeTaskList__scheme_information_link_text"))
    }
  }

  "about header" must {
    "display \"About\" with Pension scheme Name" in {
      val schemeName = "test scheme"
      val userAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.aboutHeader mustBe messages("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display \"Scheme details\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.taskList.pageTitle mustBe messages("messages__scheme_details__title")
    }
  }

  "schemeInfoSection " must {
    behave like beforeYouStartSection(
      new HsTaskListHelperVariations(_),
      schemeInfoLinkText
    )
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

  override def establishersSection(): Unit = {

    "return the seq of establishers sub sections for non deleted establishers which are all completed" in {
      val userAnswers = allEstablishers()
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(NormalMode, None, 1).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }

    "return the seq of establishers sub sections for non deleted establishers which are not completed" in {
      val userAnswers = allEstablishers(isCompleteEstablisher = false)
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, 1).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }

    "return the seq of establishers sub sections after filtering out deleted establishers" in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsEstablisherCompleteId(0))(false).flatMap(
          _.set(EstablisherCompanyDetailsId(1))(CompanyDetails("test company", None, None, true)).flatMap(
            _.set(IsEstablisherCompleteId(1))(true).flatMap(
              _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                _.set(IsEstablisherCompleteId(2))(false)
              ))))).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }
  }

  override def trusteesSection(): Unit = {

    "return the seq of trustees sub sections for non deleted trustees which are all completed" in {
      val userAnswers = allTrustees()
      val helper = createTaskListHelper(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }

    "return the seq of trustees sub sections for non deleted trustees which are not completed" in {
      val userAnswers = allTrustees(isCompleteTrustees = false)
      val helper = createTaskListHelper(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1, None).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }

    "return the seq of trustees sub sections after filtering out deleted trustees" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(false).flatMap(
          _.set(TrusteeCompanyDetailsId(1))(CompanyDetails("test company", None, None, true)).flatMap(
            _.set(IsTrusteeCompleteId(1))(false).flatMap(
              _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                _.set(IsPartnershipCompleteId(2))(false)
              ))))).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0, None).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(NormalMode, 2, None).url), None)
        )
    }
  }
}

