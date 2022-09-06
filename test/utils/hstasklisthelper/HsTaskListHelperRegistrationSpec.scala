/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import helpers.DataCompletionHelper
import identifiers._
import identifiers.register.trustees.individual.TrusteeNameId
import models._
import models.person.PersonName
import models.register.establishers.EstablisherKind
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.{Enumerable, UserAnswers}
import viewmodels.{Message, SchemeDetailsTaskList, SchemeDetailsTaskListEntitySection, StatsSection}

class HsTaskListHelperRegistrationSpec extends AnyWordSpec with Matchers with MockitoSugar with DataCompletionHelper with BeforeAndAfterEach {

  import HsTaskListHelperRegistrationSpec._

  private val mockSpokeCreationService = mock[SpokeCreationService]
  private val mockAppConfig = mock[FrontendAppConfig]
  private val helper = new HsTaskListHelperRegistration(mockSpokeCreationService, mockAppConfig)

  override protected def beforeEach(): Unit = {
    reset(mockAppConfig, mockSpokeCreationService)
    when(mockAppConfig.daysDataSaved).thenReturn(10)
  }

  "h1" must {
    "display appropriate heading" in {
      val name = "scheme name 1"
      val userAnswers = userAnswersWithSchemeName.schemeName(name)
      when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(Nil)
      helper.taskList(userAnswers, None, None, None).h1 mustBe name
    }
  }

  "beforeYouStartSection " must {
    "return correct the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      val expectedBeforeYouStartSection = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader)

      helper.beforeYouStartSection(userAnswers) mustBe expectedBeforeYouStartSection
    }
  }

  /*
   SchemeDetailsTaskListEntitySection(None,List(EntitySpoke(TaskListLink(Resolvable(messages__schemeTaskList__before_you_start_link_text,WrappedArray(scheme)),
   /scheme-name,None),Some(false))),None,WrappedArray()) was not equal to SchemeDetailsTaskListEntitySection(None,List(EntitySpoke(TaskListLink(
   Resolvable(messages__schemeTaskList__before_you_start_link_text,WrappedArray(scheme)),/scheme-name,None),Some(false))),Some(Resolvable(
   messages__schemeTaskList__before_you_start_header,WrappedArray())),WrappedArray()) (HsTaskListHelperRegistrationSpec.scala:62)

   */

  "aboutSection " must {
    "return the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAboutSection = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader)
      when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)

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
      when(mockSpokeCreationService.getWorkingKnowledgeSpoke(any(), any(), any(), any(), any())).thenReturn(expectedSpoke)

      helper.workingKnowledgeSection(userAnswers).value mustBe expectedWkSection
    }
  }

  "addEstablisherHeader " must {
    "have a link to establishers kind page when no establishers are added " in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(None,
        testEstablishersEntitySpoke, None)
      when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)

      helper.addEstablisherHeader(userAnswers, NormalMode, None).value mustBe expectedAddEstablisherHeader
    }
  }

  "addTrusteeHeader " must {
    "have change link to go to add trustees page when trustees are not mandatory(have any trustees queation not asked)" +
      "but there are one or more trustees " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName", "lastName")).asOpt.value
      val expectedAddTrusteesHeader = SchemeDetailsTaskListEntitySection(None,
        testTrusteeEntitySpoke, None)
      when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

      helper.addTrusteeHeader(userAnswers, NormalMode, None).value mustBe expectedAddTrusteesHeader
    }
  }

  "establishersSection" must {
    "return the seq of establishers without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0).
        establisherCompanyEntity(index = 1, isDeleted = true).
        establisherIndividualEntity(index = 2).
        establisherIndividualEntity(index = 3, isDeleted = true).
        establisherPartnershipEntity(index = 4, isDeleted = true).
        establisherPartnershipEntity(index = 5)

      when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockSpokeCreationService.getEstablisherIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockSpokeCreationService.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.establishersSection(userAnswers, NormalMode, None)

      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 2 last 2")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 5")))
    }
  }

  "trusteesSection" must {
    "return the seq of trustees without the deleted ones" in {
      val userAnswers = userAnswersWithSchemeName.trusteeCompanyEntity(index = 0, isDeleted = true).
        trusteeCompanyEntity(index = 1).
        trusteeIndividualEntity(index = 2, isDeleted = true).
        trusteeIndividualEntity(index = 3).
        trusteePartnershipEntity(index = 4).
        trusteePartnershipEntity(index = 5, isDeleted = true)

      when(mockSpokeCreationService.getTrusteeCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockSpokeCreationService.getTrusteeIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockSpokeCreationService.getTrusteePartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.trusteesSection(userAnswers, NormalMode, None)

      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 1")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 3 last 3")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 4")))
    }
  }

  "declaration section" must {

    "be present when declaration is enabled with trustees completed" in {
      val declarationSectionWithLink =
        SchemeDetailsTaskListEntitySection(None,
          testDeclarationEntitySpoke,
          Some("messages__schemeTaskList__sectionDeclaration_header"),
          "messages__schemeTaskList__sectionDeclaration_incomplete"
        )
      val userAnswers = answersDataAllComplete()
      when(mockSpokeCreationService.getDeclarationSpoke(any())).thenReturn(testDeclarationEntitySpoke)

      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }

    "not be present when declaration is not enabled with trustees completed" in {
      val declarationSectionWithLink =
        SchemeDetailsTaskListEntitySection(None,
          Seq.empty,
          Some("messages__schemeTaskList__sectionDeclaration_header"),
          "messages__schemeTaskList__sectionDeclaration_incomplete"
        )
      val userAnswers = answersDataAllComplete(isCompleteBeforeStart = false)

      helper.declarationSection(userAnswers).value mustBe declarationSectionWithLink
    }
  }

  "task list" must {
    "return the task list with all the sections" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0)
        .set(HaveAnyTrusteesId)(false).asOpt.value
        .set(DeclarationDutiesId)(value = true).asOpt.value

      when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
      when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)
      when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

      val result = helper.taskList(userAnswers, None, None, Some(LastUpdated(1662360059285L)))

      result mustBe SchemeDetailsTaskList(
        schemeName, None,
        beforeYouStart = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader),
        about = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader),
        workingKnowledge = None,
        addEstablisherHeader = Some(SchemeDetailsTaskListEntitySection(None, testEstablishersEntitySpoke, None)
        ),
        establishers = Seq(
          SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0"))
        ),
        addTrusteeHeader = Some(SchemeDetailsTaskListEntitySection(None, testTrusteeEntitySpoke, None)
        ),
        trustees = Nil,
        declaration = Some(
          SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
            "messages__schemeTaskList__sectionDeclaration_incomplete")
        ),
        None,
        Some(StatsSection(0, 5, Some("15 September 2022")))
      )
    }

    "return all establishers and all sections not complete where a company entity has been deleted and repurposed as a partnership" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0)
        .set(HaveAnyTrusteesId)(false).asOpt.value
        .set(DeclarationDutiesId)(value = true).asOpt.value
        .establisherCompanyEntity(index = 1, isDeleted = true)
        .establisherPartnershipEntity(index = 1)
        .establisherKind(1, EstablisherKind.Partnership)

      val testPartnershipEntitySpoke2 = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
        controllers.routes.SessionExpiredController.onPageLoad.url), None))

      when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
      when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)
      when(mockSpokeCreationService.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke2)
      when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

      val result = helper.taskList(userAnswers, None, None, None)

      result.establishers mustBe Seq(
        SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke2, Some("test partnership 1"))
      )
    }
  }

  "return the task list with correct count when all the sections are complete (with trustees)" in {
    when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
    when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
    when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
    when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)
    when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

    val userAnswers = answersDataAllComplete().setOrException(HaveAnyTrusteesId)(true)

    val result = helper.taskList(userAnswers, None, None, Some(LastUpdated(1662360059285L)))

    result.statsSection mustBe Some(StatsSection(6, 6, Some("15 September 2022")))
  }

  "return the task list with correct count when all sections are complete (without trustees)" in {
    when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
    when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
    when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
    when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)
    when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

    val userAnswers = answersDataAllComplete(isCompleteTrustees = false).setOrException(HaveAnyTrusteesId)(false)

    val result = helper.taskList(userAnswers, None, None, None)

    result.statsSection mustBe Some(StatsSection(5, 5, None))
  }

  "return all establishers and all sections not complete where a company entity has been deleted and repurposed as a partnership" in {
    val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0)
      .set(HaveAnyTrusteesId)(false).asOpt.value
      .set(DeclarationDutiesId)(value = true).asOpt.value
      .establisherCompanyEntity(index = 1, isDeleted = true)
      .establisherPartnershipEntity(index = 1)
      .establisherKind(1, EstablisherKind.Partnership)

    val testPartnershipEntitySpoke2 = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
      controllers.routes.SessionExpiredController.onPageLoad.url), None))

    when(mockSpokeCreationService.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
    when(mockSpokeCreationService.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
    when(mockSpokeCreationService.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
    when(mockSpokeCreationService.getAddEstablisherHeaderSpokes(any(), any(), any(), any())).thenReturn(testEstablishersEntitySpoke)
    when(mockSpokeCreationService.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke2)
    when(mockSpokeCreationService.getAddTrusteeHeaderSpokes(any(), any(), any(), any())).thenReturn(testTrusteeEntitySpoke)

    val result = helper.taskList(userAnswers, None, None, None)


    result.establishers mustBe Seq(
      SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0")),
      SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke2, Some("test partnership 1"))
    )
  }

}

object HsTaskListHelperRegistrationSpec extends DataCompletionHelper with Enumerable.Implicits {

  private val schemeName = "scheme"
  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  private val beforeYouStartLinkText = Message("messages__schemeTaskList__before_you_start_link_text", schemeName)
  private val beforeYouStartHeader = Some(Message("messages__schemeTaskList__before_you_start_header"))
  private val aboutHeader = Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
  private val whatYouWillNeedMemberPage = controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url
  private val addMembersLinkText = Message("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private val wkAddLinkText = Message("messages__schemeTaskList__add_details_wk")
  private val wkWynPage = controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad.url

  private val expectedBeforeYouStartSpoke = Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
    controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), Some(false)))

  private val expectedAboutSpoke = Seq(EntitySpoke(TaskListLink(addMembersLinkText, whatYouWillNeedMemberPage), None))
  private val testCompanyEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test company link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))
  private val testIndividualEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test individual link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))
  private val testPartnershipEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))
  private val testEstablishersEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test establisher link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))
  private val testTrusteeEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test trustee link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))
  private val testDeclarationEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test declaration link"),
    controllers.routes.SessionExpiredController.onPageLoad.url), None))


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
