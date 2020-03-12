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
import helpers.DataCompletionHelper
import identifiers._
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import models.person.PersonName
import models.register.SchemeType
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import utils.{Enumerable, UserAnswers}
import viewmodels.{Message, SchemeDetailsTaskListEntitySection}

class HsTaskListHelperRegistrationSpec extends SpecBase with MockitoSugar
  with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader with Enumerable.Implicits {

  import HsTaskListHelperRegistrationSpec._

  private val mockAllSpokes = mock[AllSpokes]
  private val helper = new HsTaskListHelperRegistration(mockAllSpokes)

  "h1" must {
    "display appropriate heading" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.schemeName(name)
      helper.taskList(userAnswers, None, None).h1 mustBe name
    }
  }

  "beforeYouStartSection " must {
    "return correct the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
        controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), Some(false)))
      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedSpoke)

      val expectedBeforeYouStartSection = SchemeDetailsTaskListEntitySection(None, expectedSpoke, beforeYouStartHeader)
      helper.beforeYouStartSection(userAnswers) mustBe expectedBeforeYouStartSection
    }
  }

  "aboutSection " must {
    "return the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedSpoke = Seq(
        EntitySpoke(TaskListLink(addMembersLinkText, whatYouWillNeedMemberPage), None),
        EntitySpoke(TaskListLink(addBenefitsAndInsuranceLinkText, whatYouWillNeedBenefitsInsurancePage), None),
        EntitySpoke(TaskListLink(addBankDetailsLinkText, whatYouWillNeedBankDetailsPage), None)
      )
      val expectedAboutSection = SchemeDetailsTaskListEntitySection(None, expectedSpoke, aboutHeader)
      when(mockAllSpokes.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedSpoke)

      helper.aboutSection(userAnswers, NormalMode, None) mustBe expectedAboutSection
    }
  }

  "workingKnowledgeSection " must {

    "be empty when do you have working knowledge is true " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(true).asOpt.value
      helper.workingKnowledgeSection(userAnswers) mustBe None
    }

    "have correct entity section when do you have working knowledge is false " in {
      val userAnswers = userAnswersWithSchemeName.set(DeclarationDutiesId)(false).asOpt.value
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(wkAddLinkText, wkWynPage), None))
      val expectedWkSection = SchemeDetailsTaskListEntitySection(None, expectedSpoke, None)
      when(mockAllSpokes.getWorkingKnowledgeSpoke(any(), any(), any(), any(), any())).thenReturn(expectedSpoke)

      helper.workingKnowledgeSection(userAnswers).value mustBe expectedWkSection
    }
  }

  "addEstablisherHeader " must {
    "have a link to establishers kind page when no establishers are added " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
              controllers.register.establishers.routes.EstablisherKindController
                .onPageLoad(NormalMode, userAnswers.allEstablishers(NormalMode).size, None).url))),
        None
      )
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe expectedAddEstablisherHeader
    }

    "have a link to add establishers page when establishers are added" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_change_link"),
              controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None).url))),
        None
      )
      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe expectedAddEstablisherHeader
    }
  }

  "addTrusteeHeader " must {
    "have change link to go to add trustees page when trustees are not mandatory(have any trustees queation not asked)" +
      "but there are one or more trustees " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
              controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url))),
        None
      )
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have change link to go to add trustees page when trustees is mandatory(have any trustees is true) and there are one or more trustees " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value

      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link"),
              controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url))),
        None
      )
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have add link to go to trustee kind page when when trustees are not mandatory(have any trustees question not asked)" +
      " and there are no trustees " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
              controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url))),
        None
      )
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "have add link to go to trustee kind page when trustees is mandatory(have any trustees is true) and there are no trustees " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(value = true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value

      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(
        None,
        Seq(
          EntitySpoke(
            TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
              controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url))),
        None
      )
      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }

    "not be displayed when do you have any trustees is false " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(false).asOpt.value
      helper.addTrusteeHeader(userAnswers, NormalMode, None) mustBe None
    }
  }

  "establishersSection" must {
    "return the seq of establishers without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompany(index = 0).
        establisherCompany(index = 1, isDeleted = true).
        establisherIndividual(index = 2).
        establisherIndividual(index = 3, isDeleted = true).
        establisherPartnership(index = 4, isDeleted = true).
        establisherPartnership(index = 5)

      val testCompanyEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test company link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))
      val testIndividualEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test individual link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))
      val testPartnershipEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))

      when(mockAllSpokes.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getEstablisherIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.establishersSection(userAnswers, NormalMode, None)
      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 2 last 2")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 5")))
    }
  }

  "trusteesSection" must {
    "return the seq of trustees without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.trusteeCompany(index = 0, isDeleted = true).
        trusteeCompany(index = 1).
        trusteeIndividual(index = 2, isDeleted = true).
        trusteeIndividual(index = 3).
        trusteePartnership(index = 4).
        trusteePartnership(index = 5, isDeleted = true)

      val testCompanyEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test company link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))
      val testIndividualEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test individual link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))
      val testPartnershipEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None))

      when(mockAllSpokes.getTrusteeCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getTrusteeIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getTrusteePartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.trusteesSection(userAnswers, NormalMode, None)
      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 1")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 3 last 3")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 4")))
    }
  }

  "declaration section" must {

    "have a link when declaration is enabled with trustees completed" in {
      val userAnswers = answersDataAllComplete()
      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }

    "have link when all the seqEstablishers are completed without trustees and do you have trustees is false " in {
      val userAnswers = setCompleteBeforeYouStart(isComplete = true,
        setCompleteMembers(isComplete = true,
          setCompleteBank(isComplete = true,
            setCompleteBenefits(isComplete = true,
              setCompleteEstIndividual(0,
                setCompleteWorkingKnowledge(isComplete = true, userAnswersWithSchemeName)))))).
        haveAnyTrustees(false)

      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }

    "not have link when about bank details section not completed" in {
      val userAnswers = answersDataAllComplete(isCompleteAboutBank = false)
      helper.declarationSection(userAnswers).value mustBe SchemeDetailsTaskListEntitySection(None, Nil,
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete"
      )
    }
  }

  "task list" must {
    "" in {
      val userAnswers = userAnswersWithSchemeName.
        trusteeCompany(index = 1)
      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
        controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), Some(false))))
      when(mockAllSpokes.getTrusteeCompanySpokes(any(), any(), any(), any(), any())).thenReturn(Seq(EntitySpoke(TaskListLink(Message("test company link"),
        controllers.routes.SessionExpiredController.onPageLoad().url), None)))

      val result = helper.taskList(userAnswersWithSchemeName, None, None)

    }
  }
}

object HsTaskListHelperRegistrationSpec extends DataCompletionHelper with Enumerable.Implicits {

  private val schemeName = "scheme"
  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  private val beforeYouStartLinkText = Message("messages__schemeTaskList__before_you_start_link_text", schemeName)
  private val beforeYouStartHeader = Some(Message("messages__schemeTaskList__before_you_start_header"))
  private val aboutHeader = Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
  private val whatYouWillNeedMemberPage = controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url
  private val whatYouWillNeedBenefitsInsurancePage = controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad().url
  private val whatYouWillNeedBankDetailsPage = controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad().url
  private val addMembersLinkText = Message("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private val addBenefitsAndInsuranceLinkText = Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_add", schemeName)
  private val addBankDetailsLinkText = Message("messages__schemeTaskList__about_bank_details_link_text_add", schemeName)

  private val wkAddLinkText = Message("messages__schemeTaskList__add_details_wk")
  private val wkWynPage = controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad().url

  val declarationSectionWithLink = SchemeDetailsTaskListEntitySection(None,
    Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__declaration_link"),
      controllers.register.routes.DeclarationController.onPageLoad().url))),
    Some("messages__schemeTaskList__sectionDeclaration_header"),
    "messages__schemeTaskList__sectionDeclaration_incomplete"
  )

  implicit class UserAnswerOps(answers: UserAnswers) {
    def establisherCompany(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establisherCompanyDetails(index, CompanyDetails(s"test company $index", isDeleted)).
        isEstablisherNew(index, flag = true).
        establisherKind(index, EstablisherKind.Company)
    }

    def establisherIndividual(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establishersIndividualName(index, PersonName(s"first $index", s"last $index", isDeleted)).
        isEstablisherNew(index, flag = true).isEstablisherNew(index, flag = true)
    }

    def establisherPartnership(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.establisherPartnershipDetails(index, PartnershipDetails(s"test partnership $index", isDeleted)).
        isEstablisherNew(index, flag = true).isEstablisherNew(index, flag = true)
    }

    def trusteeCompany(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteesCompanyDetails(index, CompanyDetails(s"test company $index", isDeleted)).
        isTrusteeNew(index, flag = true).
        trusteeKind(index, TrusteeKind.Company)
    }

    def trusteeIndividual(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteeName(index, PersonName(s"first $index", s"last $index", isDeleted)).
        isTrusteeNew(index, flag = true).trusteeKind(index, TrusteeKind.Company)
    }

    def trusteePartnership(index: Int, isDeleted: Boolean = false): UserAnswers = {
      answers.trusteePartnershipDetails(index, PartnershipDetails(s"test partnership $index", isDeleted)).
        isTrusteeNew(index, flag = true).trusteeKind(index, TrusteeKind.Company)
    }
  }


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
            setCompleteEstIndividual(0,
              setCompleteTrusteeIndividual(0,
                setCompleteWorkingKnowledge(isCompleteWk, userAnswersWithSchemeName)))))))
  }
}
