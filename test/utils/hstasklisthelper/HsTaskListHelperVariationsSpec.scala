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

import base.{JsonFileReader, SpecBase}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import helpers.DataCompletionHelper
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteesCompany}
import identifiers.{DeclarationDutiesId, SchemeNameId, _}
import models._
import models.person.PersonName
import models.register.SchemeType
import org.scalatest.{MustMatchers, OptionValues}
import utils.{Enumerable, UserAnswers}
import viewmodels.{Message, SchemeDetailsTaskListEntitySection, SchemeDetailsTaskListHeader, SchemeDetailsTaskListSection}

class HsTaskListHelperVariationsSpec extends SpecBase
  with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader with Enumerable.Implicits {

  import HsTaskListHelperVariationsSpec._

  "h1" must {
    "have the name of the scheme" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display \"Scheme details\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h2 mustBe Message("messages__scheme_details__title")
    }
  }
  "h3" must {
    "display \"Scheme Information\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.h3 mustBe Some(Message("messages__schemeTaskList__scheme_information_link_text"))
    }
  }

  "about header" must {
    "display \"About\" with Pension scheme Name" in {
      val schemeName = "test scheme"
      val userAnswers = userAnswersWithSchemeName.set(SchemeNameId)(schemeName).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.aboutHeader mustBe Message("messages__schemeTaskList__about_scheme_header", schemeName)
    }
  }

  "page title" must {
    "display \"Scheme details\"" in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.pageTitle mustBe Message("messages__scheme_details__title")
    }
  }

  "beforeYouStartSection " must {
    "return the before you start section correctly linking to scheme name page when not completed " in {
      val userAnswers = userAnswersWithSchemeName
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.beforeYouStartSection(userAnswers) mustBe SchemeDetailsTaskListSection(
        isCompleted = None,
        link = TaskListLink(
          Message("messages__schemeTaskList__scheme_info_link_text"),
          controllers.routes.SchemeNameController.onPageLoad(NormalMode).url
        ),
        header = None
      )
    }

    "return the before you start section correctly linking to cya page when completed " in {
      val userAnswers = setCompleteBeforeYouStart(isComplete = true, UserAnswers()).schemeName(schemeName)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.beforeYouStartSection(userAnswers) mustBe SchemeDetailsTaskListSection(
        isCompleted = None,
        link = TaskListLink(
          Message("messages__schemeTaskList__scheme_info_link_text"),
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(UpdateMode, srn).url
        ),
        header = None
      )
    }
  }

  "aboutSection " must {
    "return the the Seq of members and benefits section with " +
      "links of the first pages of individual sub sections when not completed " in {
      val userAnswers = setCompleteMembers(isComplete = false,
        setCompleteBenefits(isComplete = false,
          userAnswersWithSchemeName))
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(None, TaskListLink(aboutMembersViewLinkText,
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url), None),
          SchemeDetailsTaskListSection(None, TaskListLink(aboutBenefitsAndInsuranceViewLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )
    }

    "return the the Seq of members and benefits section with " +
      "links of the cya pages of individual sub sections when completed " in {
      val userAnswers = setCompleteMembers(isComplete = true,
        setCompleteBenefits(isComplete = true, userAnswersWithSchemeName))
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(None, TaskListLink(aboutMembersViewLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url), None),
          SchemeDetailsTaskListSection(None, TaskListLink(aboutBenefitsAndInsuranceViewLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    "not display when do you have working knowledge is false " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.workingKnowledge mustBe None
    }

    "not display when do you have working knowledge is true " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.taskList.workingKnowledge mustBe None
    }
  }

  "addEstablisherHeader " must {
    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addEstablisherHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController
            .onPageLoad(UpdateMode, userAnswers.allEstablishers(UpdateMode).size, srn).url)), None)
    }

    "return the link to add establisher page when establishers are added" in {
      val userAnswers = UserAnswers()
        .set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addEstablisherHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(viewEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(UpdateMode, srn).url)), None)
    }

    "display plain text when scheme is locked and no establisher exists" in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.taskList.addEstablisherHeader.value mustBe
        SchemeDetailsTaskListHeader(None, None, None, None, Some(Message("messages__schemeTaskList__sectionEstablishers_no_establishers")))
    }

    "not display an add link when scheme is locked and establishers exist 2222" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value

      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.taskList.addEstablisherHeader mustBe None
    }

  }

  "addTrusteeHeader" must {
    "display correct link data when 2 trustees exist " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(viewTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url)), None)
    }

    "display correct link data when trustee is optional and no trustee exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, userAnswers.allTrustees.size, srn).url)), None, None)
    }

    "display correct link data when trustee is mandatory and no trustees exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.MasterTrust).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, userAnswers.allTrustees.size, srn).url)), None,
          None)
    }

    s"display and link should go to trustee kind page when do you have any trustees is true and no trustees are added" in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, userAnswers.allTrustees.size, srn).url)), None)
    }

    "display and link should go to add trustees page when do you have any trustees is not present" +
      s"and trustees are added and completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(viewTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url)), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      s"and trustees are added and not completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName"))
        .asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.addTrusteeHeader(userAnswers, UpdateMode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(TaskListLink(viewTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url)), None)
    }

    "display plain text when scheme is locked and no trustees exist" in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.taskList.addTrusteeHeader.value mustBe
        SchemeDetailsTaskListHeader(None, None, None, None, Some(Message("messages__schemeTaskList__sectionTrustees_no_trustees")))
    }

    "no links when scheme is locked and trustees exist" in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.taskList.addTrusteeHeader mustBe Some(SchemeDetailsTaskListHeader(header = Some(Message("messages__schemeTaskList__sectionTrustees_header"))))
    }
  }

  "establishers" must {
    "return the seq of establishers sub sections" in {
      val userAnswers = establisherCompany()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.establishersSection(userAnswers, UpdateMode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_details", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(UpdateMode, srn, 0).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(UpdateMode, srn, 0).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(UpdateMode, srn, 0).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_directors", "test company"),
                controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController
                  .onPageLoad(UpdateMode, srn, 0).url), None)
            ), Some("test company"))
        )
    }
  }

  "trustees" must {
    "return the seq of trustees sub sections when all spokes are uninitiated" in {
      val userAnswers = trusteeCompany(false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.trusteesSection(userAnswers, UpdateMode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(UpdateMode, 0, srn).url), None)
            ), Some("test company"))
        )
    }

    "return the seq of trustees sub sections when all spokes are completed" in {
      val userAnswers = allAnswers
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.trusteesSection(userAnswers, UpdateMode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_details", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_address", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(UpdateMode, 0, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_contact", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(UpdateMode, 0, srn).url), None)
            ), Some("test company")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_details", "firstName lastName"),
              trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(UpdateMode, 1, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_address", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(UpdateMode, 1, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_contact", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(UpdateMode, 1, srn).url), None)
            ), Some("firstName lastName")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_details", "test partnership"),
              trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(UpdateMode, 2, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_address", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(UpdateMode, 2, srn).url), None),
              EntitySpoke(TaskListLink(Message("messages__schemeTaskList__view_contact", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(UpdateMode, 2, srn).url), None)
            ), Some("test partnership"))
        )
    }
  }

  "declaration" must {
    s"have a declaration section when viewonly is false" in {
      val userAnswers = answersDataAllComplete()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.declarationSection(userAnswers).isDefined mustBe true
    }

    s"have incomplete link when about benefits and insurance section not completed" in {
      val userAnswers = answersDataAllComplete(isCompleteAboutBenefits = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      mustHaveDeclarationLinkEnabled(helper, userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have incomplete link when establishers section not completed" in {
      val userAnswers = answersDataAllComplete(isCompleteEstablishers = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      mustHaveDeclarationLinkEnabled(helper, userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have incomplete link when trustees section not completed" in {
      val userAnswers = answersDataAllComplete(isCompleteTrustees = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      mustHaveDeclarationLinkEnabled(helper, userAnswers,
        Some(controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url))
    }

    s"have link when all the sections are completed" in {
      val userAnswers = allAnswers.set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      mustHaveDeclarationLinkEnabled(helper, userAnswers,
        Some(controllers.routes.VariationDeclarationController.onPageLoad(srn).url))
    }

    s"have no link when all the sections are not completed and no user answers updated" in {
      val userAnswers = answersDataAllComplete(isChangedInsuranceDetails = false)
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = false, srn)
      helper.declarationSection(userAnswers).isDefined mustBe true
      mustNotHaveDeclarationLink(helper, userAnswers)
    }

    "NOT be presented to user when viewonly is true" in {
      val userAnswers = answersDataAllComplete()
      val helper = new HsTaskListHelperVariations(userAnswers, viewOnly = true, srn)
      helper.declarationSection(userAnswers).isDefined mustBe false
    }
  }
}

object HsTaskListHelperVariationsSpec extends SpecBase with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader {
  private val schemeName = "scheme"
  private val srn = Some("test-srn")

  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  private val aboutMembersViewLinkText: Message = Message("messages__schemeTaskList__about_members_link_text_view", schemeName)
  private val aboutBenefitsAndInsuranceViewLinkText: Message = Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName)
  private val addEstablisherLinkText: Message = Message("messages__schemeTaskList__sectionEstablishers_add_link")
  private val addTrusteesLinkText: Message = Message("messages__schemeTaskList__sectionTrustees_add_link")
  private val declarationLinkText: Message = Message("messages__schemeTaskList__declaration_link")
  private val viewEstablisherLinkText: Message = Message("messages__schemeTaskList__sectionEstablishers_view_link")
  private val viewTrusteesLinkText: Message = Message("messages__schemeTaskList__sectionTrustees_view_link")

  private def allAnswers: UserAnswers = UserAnswers(readJsonFromFile("/payload.json"))

  private def answersDataAllComplete(isCompleteBeforeStart: Boolean = true,
                                     isCompleteAboutMembers: Boolean = true,
                                     isCompleteAboutBank: Boolean = true,
                                     isCompleteAboutBenefits: Boolean = true,
                                     isCompleteWk: Boolean = true,
                                     isCompleteEstablishers: Boolean = true,
                                     isCompleteTrustees: Boolean = true,
                                     isChangedInsuranceDetails: Boolean = true,
                                     isChangedEstablishersTrustees: Boolean = true
                                    ): UserAnswers = {
    setCompleteBeforeYouStart(isCompleteBeforeStart,
      setCompleteMembers(isCompleteAboutMembers,
        setCompleteBank(isCompleteAboutBank,
          setCompleteBenefits(isCompleteAboutBenefits,
            setCompleteWorkingKnowledge(isCompleteWk,
              setTrusteeCompletionStatusJsResult(isComplete = isCompleteTrustees, 0, userAnswersWithSchemeName).asOpt.value))))).set(
      InsuranceDetailsChangedId
    )(isChangedInsuranceDetails).asOpt.value
  }

  private def establisherCompany(isCompleteEstablisher: Boolean = true): UserAnswers = {
    userAnswersWithSchemeName.set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsEstablisherNewId(0))(true).flatMap(
        _.set(establisherCompanyPath.HasCompanyPAYEId(0))(false)
      )).asOpt.value
  }

  private def trusteeCompany(isCompleteTrustee: Boolean = true): UserAnswers =
    userAnswersWithSchemeName.set(trusteesCompany.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsTrusteeNewId(0))(true)).asOpt.value

  private def mustNotHaveDeclarationLink(helper: HsTaskListHelperVariations, userAnswers: UserAnswers): Unit =
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe None)

  private def mustHaveDeclarationLinkEnabled(helper: HsTaskListHelperVariations, userAnswers: UserAnswers, url: Option[String] = None): Unit = {
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe
      Some(TaskListLink(declarationLinkText, url.getOrElse(controllers.register.routes.DeclarationController.onPageLoad().url))))
  }
}
