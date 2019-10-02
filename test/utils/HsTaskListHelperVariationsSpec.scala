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
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherNameId}
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.establishers.{EstablisherKindId, IsEstablisherAddressCompleteId, IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.trustees.IsTrusteeNewId
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{DeclarationDutiesId, IsAboutBenefitsAndInsuranceCompleteId, IsAboutMembersCompleteId, SchemeNameId, _}
import models._
import models.person.{PersonDetails, PersonName}
import models.register.SchemeType
import models.register.establishers.EstablisherKind.Indivdual
import org.joda.time.LocalDate
import play.api.libs.json.JsResult
import utils.behaviours.HsTaskListHelperBehaviour
import utils.hstasklisthelper.{HsTaskListHelper, HsTaskListHelperVariations}
import viewmodels.{SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListHeader, SchemeDetailsTaskListSection}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import utils.HsTaskListHelperUtilsSpec.messages

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour with Enumerable.Implicits {

  private val srn = Some("test-srn")
  private val fakeFeatureManagementService = new FakeFeatureSwitchManagementService(false)

  override val createTaskListHelper: (UserAnswers, FeatureSwitchManagementService) => HsTaskListHelper =
    (ua, fs) => new HsTaskListHelperVariations(ua, viewOnly = false, srn = srn, fs)

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

    setTrusteeCompletionStatusJsResult(isComplete = isCompleteTrustees, 0,
      userAnswersWithSchemeName.set(IsBeforeYouStartCompleteId)(isCompleteBeforeStart).flatMap(
        _.set(IsAboutMembersCompleteId)(isCompleteAboutMembers).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(isCompleteAboutBank).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(isCompleteAboutBenefits).flatMap(
              _.set(BenefitsSecuredByInsuranceId)(!isCompleteAboutBenefits).flatMap(
                _.set(IsWorkingKnowledgeCompleteId)(isCompleteWk).flatMap(
                  _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                    _.set(IsEstablisherCompleteId(0))(isCompleteEstablishers)).flatMap(
                    _.set(IsEstablisherAddressCompleteId(0))(isCompleteEstablishers)).flatMap(
                    _.set(TrusteeNameId(0))(PersonName("firstName", "lastName")).flatMap(
                      _.set(InsuranceDetailsChangedId)(isChangedInsuranceDetails))
                  )
                )
              )
            )
          )
        )
      ).asOpt.value
    )
  }

  "h1" must {
    "have the name of the scheme" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display \"Scheme details\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }
  "h3" must {
    "display \"Scheme Information\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.h3 mustBe Some(messages("messages__schemeTaskList__scheme_information_link_text"))
    }
  }

  "about header" must {
    "display \"About\" with Pension scheme Name" in {
      val schemeName = "test scheme"
      val userAnswers = userAnswersWithSchemeName.set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.aboutHeader mustBe messages("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display \"Scheme details\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.pageTitle mustBe messages("messages__scheme_details__title")
    }
  }

  "schemeInfoSection " must {
    behave like beforeYouStartSection(
      new HsTaskListHelperVariations(_, viewOnly = false, srn, fakeFeatureManagementService),
      schemeInfoLinkText,
      UpdateMode,
      None
    )
  }

  "aboutSection " must {
    "return the the Seq of members and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = userAnswersWithSchemeName.set(IsAboutMembersCompleteId)(false).flatMap(
        _.set(IsAboutBenefitsAndInsuranceCompleteId)(false)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(None, Link(aboutMembersViewLinkText,
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), None),
          SchemeDetailsTaskListSection(None, Link(aboutBenefitsAndInsuranceViewLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )
    }

    "return the the Seq of members and benefits section with " +
      "links of the cya pages of individual sub sections when completed " in {
      val userAnswers = userAnswersWithSchemeName.set(IsAboutMembersCompleteId)(true).flatMap(
        _.set(BenefitsSecuredByInsuranceId)(false)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(None, Link(aboutMembersViewLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url), None),
          SchemeDetailsTaskListSection(None, Link(aboutBenefitsAndInsuranceViewLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    "not display when do you have working knowledge is false " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.workingKnowledge mustBe None
    }

    "not display when do you have working knowledge is true " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn, fakeFeatureManagementService)
      helper.taskList.workingKnowledge mustBe None
    }
  }

  "addEstablisherHeader " must {

    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = UserAnswers()
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addEstablisherHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(UpdateMode, userAnswers.allEstablishers(isHnS2Enabled, UpdateMode).size, srn).url)), None)
    }

    "return the link to add establisher page when establishers are added" in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addEstablisherHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(viewEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(UpdateMode, srn).url)), None)
    }

    "display plain text when scheme is locked and no establisher exists" in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn, fakeFeatureManagementService)
      helper.taskList.addEstablisherHeader.value mustBe
        SchemeDetailsTaskListHeader(None, None, None, None, Some(messages("messages__schemeTaskList__sectionEstablishers_no_establishers")))
    }

    "not display an add link when scheme is locked and establishers exist 2222" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn, fakeFeatureManagementService)
      helper.taskList.addEstablisherHeader mustBe None
    }

  }

  "addTrusteeHeader" must {
    behave like addTrusteeHeader(UpdateMode, srn,
      addDeleteLinkText = viewTrusteesLinkText,
      addLinkText = addTrusteesLinkText,
      changeLinkText = viewTrusteesLinkText)

    "display plain text when scheme is locked and no trustees exist" in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn, fakeFeatureManagementService)
      helper.taskList.addTrusteeHeader.value mustBe
        SchemeDetailsTaskListHeader(None, None, None, None, Some(messages("messages__schemeTaskList__sectionTrustees_no_trustees")))
    }

    "no links when scheme is locked and trustees exist" in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn, fakeFeatureManagementService)
      helper.taskList.addTrusteeHeader mustBe Some(SchemeDetailsTaskListHeader(header = Some(messages("messages__schemeTaskList__sectionTrustees_header"))))
    }
  }

  "establishers" must {

    behave like establishersSectionHnS(UpdateMode, srn)
  }

  "trustees" must {

    behave like trusteesSectionHnS(UpdateMode, srn)

    "return the seq of trustees sub sections when all spokes are completed" in {
      val userAnswers = allAnswersHnS
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.trustees(userAnswers, UpdateMode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__view_details", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_address", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_contact", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(UpdateMode, 0, srn).url), None)
            ), Some("test company")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__view_details", "firstName lastName"),
              trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(UpdateMode, 1, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_address", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(UpdateMode, 1, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_contact", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(UpdateMode, 1, srn).url), None)
            ), Some("firstName lastName")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__view_details", "test partnership"),
              trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_address", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(UpdateMode, 2, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__view_contact", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
            ), Some("test partnership"))
        )
    }
  }

  override def establishersSectionHnS(mode: Mode, srn: Option[String]): Unit = {

    def dynamicContentForChangeLink(srn:Option[String], name:String, registrationKey:String, variationsKey:String) =
    messages(if(srn.isDefined) variationsKey else registrationKey, name)

    def modeBasedCompletion(completion: Option[Boolean]): Option[Boolean] = if (mode == NormalMode) completion else None

    "return the seq of establishers sub sections" in {
      val userAnswers = establisherCompany()
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.establishers(userAnswers, mode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(dynamicContentForChangeLink(srn, "test company", "messages__schemeTaskList__change_details", "messages__schemeTaskList__view_details"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_directors", "test company"),
                controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, 0).url), None)
            ), Some("test company"))
        )
    }
  }

  def variationsTrusteeTests():Unit = {
    val fsm:FakeFeatureSwitchManagementService = fakeFeatureManagementService
    s"have a declaration section when viewonly is false" in {
      val userAnswers = answersData().asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }

    s"have incomplete link when about benefits and insurance section not completed" in {
      val userAnswers = answersData(isCompleteAboutBenefits = false).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have incomplete link when establishers section not completed" in {
      val userAnswers = answersData(isCompleteEstablishers = false).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have incomplete link when trustees section not completed" in {
      val userAnswers = answersData(isCompleteTrustees = false).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have link when all the sections are completed" in {
      val userAnswers =  allAnswersHnS.set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers,
        Some(controllers.routes.VariationDeclarationController.onPageLoad(srn).url))
    }

    s"have no link when all the sections are not completed and no user answers updated" in {
      val userAnswers = answersData(isChangedInsuranceDetails = false).asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.declarationSection(userAnswers).isDefined mustBe true
      mustNotHaveDeclarationLink(helper, userAnswers)
    }
  }

  "declaration" must {

    behave like variationsTrusteeTests()
  }

  //scalastyle:off method.length
  def establishersSection(): Unit = {

    "return the seq of establishers sub sections for non deleted establishers which are all completed" in {
      val userAnswers = allAnswers
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"), fakeFeatureManagementService)
      helper.establishers(userAnswers, UpdateMode, srn) mustBe
        Seq(
//          SchemeDetailsTaskListEntitySection(
//            None,
//            Seq(
//              EntitySpoke(Link(messages("messages__schemeTaskList__persons_details__link_text", "Test company name"), controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(UpdateMode, srn, 0).url), None)
//            ),
//            None
//          ),
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(
            messages("messages__schemeTaskList__persons_details__link_text", "Test individual name"),
            controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(UpdateMode, 1, srn).url), None)), None),
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(
            messages("messages__schemeTaskList__persons_details__link_text", "Test Partnership"),
            controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(UpdateMode, 2, srn).url), None)), None)
        )
    }

    "return the seq of establishers sub sections for non deleted establishers which are not completed" in {
      val userAnswers = allAnswersIncomplete
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"), fakeFeatureManagementService)
      helper.establishers(userAnswers, UpdateMode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(
            messages("messages__schemeTaskList__persons_details__link_text", "Test company name"),
            controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(UpdateMode, srn, 0).url), None)), None),
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(
            messages("messages__schemeTaskList__persons_details__link_text", "Test individual name"),
            controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(UpdateMode, 1, srn).url), None)), None),
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(
            messages("messages__schemeTaskList__persons_details__link_text", "Test Partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)), None)
        )
    }

    "return the seq of establishers sub sections after filtering out deleted establishers" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).flatMap(
        _.set(IsEstablisherCompleteId(0))(false).flatMap(
          _.set(IsEstablisherNewId(0))(true).flatMap(
            _.set(EstablisherCompanyDetailsId(1))(CompanyDetails("test company", true)).flatMap(
              _.set(IsEstablisherCompleteId(1))(true).flatMap(
                _.set(IsEstablisherNewId(1))(true).flatMap(
                  _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                    _.set(IsEstablisherNewId(2))(true)).flatMap(
                    _.set(IsEstablisherCompleteId(2))(false)
                  ))))))).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, Some("test-srn"), fakeFeatureManagementService)
      helper.establishers(userAnswers, UpdateMode, srn) mustBe
        Seq(SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(messages("messages__schemeTaskList__persons_details__link_text", "firstName lastName"),
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(UpdateMode, 0, srn).url), None)), None),
          SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(Link(messages("messages__schemeTaskList__persons_details__link_text", "test partnership"),
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)), None)
        )
    }
  }
}

class HsTaskListHelperVariationsViewOnlySpec extends HsTaskListHelperBehaviour {
  private val srn = Some("test-srn")
  private val fakeFeatureManagementService = new FakeFeatureSwitchManagementService(false)
  override val createTaskListHelper: (UserAnswers, FeatureSwitchManagementService) => HsTaskListHelper =
    (ua, fs) => new HsTaskListHelperVariations(ua, viewOnly = true, srn = srn, fs)

  "declaration" must {


    "NOT have a declaration section when viewonly is true" in {
      val userAnswers = answersData().asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.declarationSection(userAnswers).isDefined mustBe false
    }
  }
}

