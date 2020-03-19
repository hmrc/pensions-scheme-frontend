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
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.{SchemeNameId, _}
import models._
import models.person.PersonName
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import utils.UserAnswers
import viewmodels.{Message, SchemeDetailsTaskList, SchemeDetailsTaskListEntitySection}

class HsTaskListHelperVariationsSpec extends WordSpec with MustMatchers with MockitoSugar {

  import HsTaskListHelperVariationsSpec._

  private val mockAllSpokes = mock[SpokeCreationService]
  private val helper = new HsTaskListHelperVariations(mockAllSpokes)

  "h1" must {
    "have the name of the scheme" in {
      val userAnswers = userAnswersWithSchemeName

      helper.taskList(userAnswers, None, srn).h1 mustBe schemeName
    }
  }

  "beforeYouStartSection " must {
    "return the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName
      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)

      val expectedBeforeYouStartSection = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader)
      helper.beforeYouStartSection(userAnswers, srn) mustBe expectedBeforeYouStartSection
    }
  }

  "aboutSection " must {
    "return the correct entity section " in {
      val userAnswers = userAnswersWithSchemeName

      val expectedAboutSection = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader)
      when(mockAllSpokes.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)

      helper.aboutSection(userAnswers, UpdateMode, srn) mustBe expectedAboutSection
    }
  }

  "addEstablisherHeader " must {
    "have no link when no establishers are added and viewOnly is true" in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(None, Nil, None,
        Message("messages__schemeTaskList__sectionEstablishers_no_establishers"))

      helper.addEstablisherHeader(userAnswers, UpdateMode, srn, viewOnly = true).value mustBe expectedAddEstablisherHeader
    }

    "have a link to establishers kind page when no establishers are added and viewOnly is false" in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(None, Seq(
        EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link"),
          controllers.register.establishers.routes.EstablisherKindController
            .onPageLoad(UpdateMode, userAnswers.allEstablishers(UpdateMode).size, srn).url), None)), None)

      helper.addEstablisherHeader(userAnswers, UpdateMode, srn, viewOnly = false).value mustBe expectedAddEstablisherHeader
    }

    "have a link to add establishers page when establishers are added and viewOnly is false" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value

      val expectedAddEstablisherHeader = SchemeDetailsTaskListEntitySection(None, Seq(
        EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_view_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(UpdateMode, srn).url, None), None)), None)

      helper.addEstablisherHeader(userAnswers, UpdateMode, srn, viewOnly = false).value mustBe expectedAddEstablisherHeader
    }

    "have no header section when establishers are added and viewOnly is true" in {
      val userAnswers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).asOpt.value

      helper.addEstablisherHeader(userAnswers, UpdateMode, srn, viewOnly = true) mustBe None
    }
  }

  "addTrusteeHeader " must {

    "have no link when no trustees are added and viewOnly is true" in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddTrusteeHeader = SchemeDetailsTaskListEntitySection(None, Nil, None,
        Message("messages__schemeTaskList__sectionTrustees_no_trustees"))

      helper.addTrusteeHeader(userAnswers, UpdateMode, srn, viewOnly = true).value mustBe expectedAddTrusteeHeader
    }

    "have a link to trustee kind page when no trustees are added and viewOnly is false" in {
      val userAnswers = userAnswersWithSchemeName
      val expectedAddTrusteeHeader = SchemeDetailsTaskListEntitySection(None, Seq(
        EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_add_link"),
          controllers.register.trustees.routes.TrusteeKindController
            .onPageLoad(UpdateMode, userAnswers.allTrustees.size, srn).url), None)), None)

      helper.addTrusteeHeader(userAnswers, UpdateMode, srn, viewOnly = false).value mustBe expectedAddTrusteeHeader
    }

    "have a link to add trustees page when trustees are added and viewOnly is false" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value

      val expectedAddTrusteeHeader = SchemeDetailsTaskListEntitySection(None, Seq(
        EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url, None), None)), None)

      helper.addTrusteeHeader(userAnswers, UpdateMode, srn, viewOnly = false).value mustBe expectedAddTrusteeHeader
    }

    "have empty header section when establishers are added and viewOnly is true" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value

      helper.addTrusteeHeader(userAnswers, UpdateMode, srn, viewOnly = true).value mustBe SchemeDetailsTaskListEntitySection(None, Nil, None)
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

      when(mockAllSpokes.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getEstablisherIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getEstablisherPartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.establishersSection(userAnswers, UpdateMode, srn)

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

      when(mockAllSpokes.getTrusteeCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)
      when(mockAllSpokes.getTrusteeIndividualSpokes(any(), any(), any(), any(), any())).thenReturn(testIndividualEntitySpoke)
      when(mockAllSpokes.getTrusteePartnershipSpokes(any(), any(), any(), any(), any())).thenReturn(testPartnershipEntitySpoke)

      val result = helper.trusteesSection(userAnswers, UpdateMode, srn)

      result mustBe Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 1")),
        SchemeDetailsTaskListEntitySection(None, testIndividualEntitySpoke, Some("first 3 last 3")),
        SchemeDetailsTaskListEntitySection(None, testPartnershipEntitySpoke, Some("test partnership 4")))
    }
  }

  "declaration section" must {

    "not be there when viewOnly is true" in {
      val userAnswers = userAnswersWithSchemeName

      helper.declarationSection(userAnswers, srn, viewOnly = true) mustBe None
    }

    "have link to still need details page when user answers is updated but not completed and viewOnly is false" in {
      val userAnswers = userAnswersWithSchemeName.set(InsuranceDetailsChangedId)(true).asOpt.value

      helper.declarationSection(userAnswers, srn, viewOnly = false).value mustBe SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__declaration_link"),
          controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
        ), None)),
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2")
    }

    "have link to variation declaration page when user answers is updated, completed and viewOnly is false" in {
      val userAnswers = answersDataAllComplete().set(InsuranceDetailsChangedId)(true).asOpt.value

      helper.declarationSection(userAnswers, srn, viewOnly = false).value mustBe SchemeDetailsTaskListEntitySection(None,
        Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__declaration_link"),
          controllers.routes.VariationDeclarationController.onPageLoad(srn).url
        ), None)),
        Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2")
    }

    "not have link when user answers is not updated and viewOnly is false" in {
      val userAnswers = answersDataAllComplete()

      helper.declarationSection(userAnswers, srn, viewOnly = false).value mustBe SchemeDetailsTaskListEntitySection(None,
        Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
        "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
        "messages__schemeTaskList__sectionDeclaration_incomplete_v2")
    }
  }

  "task list" must {
    "return the task list with all the sections" in {
      val userAnswers = userAnswersWithSchemeName.establisherCompanyEntity(index = 0)
        .set(HaveAnyTrusteesId)(false).asOpt.value
        .set(InsuranceDetailsChangedId)(true).asOpt.value

      when(mockAllSpokes.getBeforeYouStartSpoke(any(), any(), any(), any(), any())).thenReturn(expectedBeforeYouStartSpoke)
      when(mockAllSpokes.getAboutSpokes(any(), any(), any(), any(), any())).thenReturn(expectedAboutSpoke)
      when(mockAllSpokes.getEstablisherCompanySpokes(any(), any(), any(), any(), any())).thenReturn(testCompanyEntitySpoke)

      val result = helper.taskList(userAnswers, Some(false), srn)

      result mustBe SchemeDetailsTaskList(
        schemeName, srn,
        beforeYouStart = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader),
        about = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader),
        workingKnowledge = None,
        addEstablisherHeader = Some(SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__sectionEstablishers_view_link"),
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(UpdateMode, srn).url))), None)),
        establishers = Seq(SchemeDetailsTaskListEntitySection(None, testCompanyEntitySpoke, Some("test company 0"))),
        addTrusteeHeader = Some(SchemeDetailsTaskListEntitySection(None, Seq(EntitySpoke(TaskListLink(
          Message("messages__schemeTaskList__sectionTrustees_add_link"), controllers.register.trustees.routes.TrusteeKindController
              .onPageLoad(UpdateMode, userAnswers.allTrustees.size, srn).url), None)), None)),
        trustees = Nil,
        declaration = Some(SchemeDetailsTaskListEntitySection(None,
          Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__declaration_link"),
            controllers.register.routes.StillNeedDetailsController.onPageLoad(srn).url
          ), None)),
          Some("messages__schemeTaskList__sectionDeclaration_header"),
          "messages__schemeTaskList__sectionDeclaration_incomplete_v1",
          "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))
      )
    }
  }
}

object HsTaskListHelperVariationsSpec extends SpecBase with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader {
  private val schemeName = "scheme"
  private val srn = Some("test-srn")

  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
  private val beforeYouStartLinkText = Message("messages__schemeTaskList__scheme_info_link_text", schemeName)
  private val expectedBeforeYouStartSpoke = Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
    controllers.routes.SchemeNameController.onPageLoad(UpdateMode).url), Some(false)))
  private val beforeYouStartHeader = Some(Message("messages__schemeTaskList__scheme_information_link_text"))

  private val addMembersLinkText = Message("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private val whatYouWillNeedMemberPage = controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url
  private val aboutHeader = Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))
  private val expectedAboutSpoke = Seq(EntitySpoke(TaskListLink(addMembersLinkText, whatYouWillNeedMemberPage), None))

  private val testCompanyEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test company link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))
  private val testIndividualEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test individual link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))
  private val testPartnershipEntitySpoke = Seq(EntitySpoke(TaskListLink(Message("test partnership link"),
    controllers.routes.SessionExpiredController.onPageLoad().url), None))

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
