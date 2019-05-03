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

import base.SpecBase
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{DeclarationDutiesId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId, _}
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import play.api.libs.json.JsResult
import utils.behaviours.HsTaskListHelperBehaviour
import viewmodels.{SchemeDetailsTaskListHeader, SchemeDetailsTaskListSection}

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour {
  private val srn = Some("test-srn")
  override val createTaskListHelper:UserAnswers => HsTaskListHelper = ua => new HsTaskListHelperVariations(ua, viewOnly = false, srn = srn)
  override def answersData(isCompleteBeforeStart: Boolean = true,
                           isCompleteAboutMembers: Boolean = true,
                           isCompleteAboutBank: Boolean = true,
                           isCompleteAboutBenefits: Boolean = true,
                           isCompleteWk: Boolean = true,
                           isCompleteEstablishers: Boolean = true,
                           isCompleteTrustees: Boolean = true,
                           isChangedInsuranceDetails: Boolean = true,
                           isChangedEstablishersTrustees: Boolean = true
                          ): JsResult[UserAnswers] = {
    UserAnswers().set(IsBeforeYouStartCompleteId)(isCompleteBeforeStart).flatMap(
      _.set(IsAboutMembersCompleteId)(isCompleteAboutMembers).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(isCompleteAboutBank).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(isCompleteAboutBenefits).flatMap(
            _.set(IsWorkingKnowledgeCompleteId)(isCompleteWk).flatMap(
              _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                _.set(IsEstablisherCompleteId(0))(isCompleteEstablishers)).flatMap(
                _.set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(IsTrusteeCompleteId(0))(isCompleteTrustees)).flatMap(
                  _.set(InsuranceDetailsChangedId)(isChangedInsuranceDetails)).flatMap(
                  _.set(InsuranceDetailsChangedId)(isChangedEstablishersTrustees))
              )
            )
          )
        )
      )
    )
  }

  "h1" must {
    "have the name of the scheme" in {
      val name = "scheme name 1"
      val userAnswers = UserAnswers().set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display \"Scheme details\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }
  "h3" must {
    "display \"Scheme Information\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h3 mustBe Some(messages("messages__schemeTaskList__scheme_information_link_text"))
    }
  }

  "about header" must {
    "display \"About\" with Pension scheme Name" in {
      val schemeName = "test scheme"
      val userAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.aboutHeader mustBe messages("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display \"Scheme details\"" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.pageTitle mustBe messages("messages__scheme_details__title")
    }
  }

  "schemeInfoSection " must {
    behave like beforeYouStartSection(
      new HsTaskListHelperVariations(_, viewOnly = false, srn),
      schemeInfoLinkText,
      UpdateMode,
      None
    )
  }

  "aboutSection " must {
    "return the the Seq of members and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(false).flatMap(
        _.set(IsAboutBenefitsAndInsuranceCompleteId)(false)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
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
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(true), Link(aboutMembersLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    "not display when do you have working knowledge is false " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.workingKnowledge mustBe None
    }

    "not display when do you have working knowledge is true " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.workingKnowledge mustBe None
    }
  }

  "addEstablisherHeader " must {

    behave like addEstablisherHeader(UpdateMode, srn)
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader(UpdateMode, srn)

    "display plain text when scheme is locked and no trustees exist" in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.taskList.addTrusteeHeader.value mustBe
        SchemeDetailsTaskListHeader(None, None, None, None, Some(messages("messages__schemeTaskList__sectionTrustees_no_trustees")))
    }
  }

  "establishers" must {

    behave like establishersSection()
  }

  "trustees" must {

    behave like trusteesSection()
  }

  "declaration" must {
    "have a declaration section when viewonly is false" in {
      val userAnswers = answersData().asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }

    behave like declarationSection()
  }

  def establishersSection(): Unit = {

    "return the seq of establishers sub sections for non deleted establishers which are all completed" in {
      val userAnswers = allEstablishers()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"))
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(UpdateMode, srn, 1).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }

    "return the seq of establishers sub sections for non deleted establishers which are not completed" in {
      val userAnswers = allEstablishers(isCompleteEstablisher = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"))
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(UpdateMode, srn, 1).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }

    "return the seq of establishers sub sections after filtering out deleted establishers" in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsEstablisherCompleteId(0))(false).flatMap(
        _.set(IsEstablisherNewId(0))(true).flatMap(
          _.set(EstablisherCompanyDetailsId(1))(CompanyDetails("test company", true)).flatMap(
            _.set(IsEstablisherCompleteId(1))(true).flatMap(
            _.set(IsEstablisherNewId(1))(true).flatMap(
              _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                _.set(IsEstablisherNewId(2))(true)).flatMap(
                _.set(IsEstablisherCompleteId(2))(false)
              ))))))).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"))
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }
  }

  def trusteesSection(): Unit = {

    "return the seq of trustees sub sections for non deleted trustees which are all completed" in {
      val userAnswers = allTrustees()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn = Some("test-srn"))
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(UpdateMode, 1, srn).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }

    "return the seq of trustees sub sections for non deleted trustees which are not completed" in {
      val userAnswers = allTrustees(isCompleteTrustees = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn = Some("test-srn"))
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test company"),
            controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(UpdateMode, 1, srn).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }

    "return the seq of trustees sub sections after filtering out deleted trustees" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(false).flatMap(
          _.set(IsTrusteeNewId(0))(true).flatMap(
           _.set(TrusteeCompanyDetailsId(1))(CompanyDetails("test company", true)).flatMap(
            _.set(IsTrusteeCompleteId(1))(false).flatMap(
              _.set(IsTrusteeNewId(1))(true).flatMap(
                _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                  _.set(IsTrusteeNewId(2))(true)).flatMap(
                  _.set(IsPartnershipCompleteId(2))(false)
                ))))))).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn = Some("test-srn"))
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
          SchemeDetailsTaskListSection(Some(false), Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
        )
    }
  }
}

class HsTaskListHelperVariationsViewOnlySpec extends HsTaskListHelperBehaviour {
  private val srn = Some("test-srn")
  override val createTaskListHelper: UserAnswers => HsTaskListHelper = ua => new HsTaskListHelperVariations(ua, viewOnly = true, srn = srn)
  "declaration" must {
    "NOT have a declaration section when viewonly is true" in {
      val userAnswers = answersData().asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.declarationSection(userAnswers).isDefined mustBe false
    }
  }
}


