/*
 * Copyright 2018 HM Revenue & Customs
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

import base.{JsonFileReader, SpecBase}
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.{HaveAnyTrusteesId, IsTrusteeCompleteId}
import identifiers.register.{IsAboutSchemeCompleteId, IsWorkingKnowledgeCompleteId, SchemeDetailsId}
import models.person.PersonDetails
import models.register.{EstablisherCompanyEntity, EstablisherIndividualEntity, SchemeDetails, SchemeType}
import models.{CompanyDetails, NormalMode}
import org.joda.time.LocalDate
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.{CheckMode, Index, NormalMode}
import models.register.{EstablisherCompanyEntity, EstablisherIndividualEntity, EstablisherPartnershipEntity}
import org.scalatest.{MustMatchers, WordSpec}
import viewmodels._

class TaskListHelperSpec extends WordSpec with MustMatchers {

  import TaskListHelperSpec._

  "TaskListHelper" must {
    "return valid about section based on user answers" in {

      new TaskListHelper(Some(userAnswers)).taskList mustBe JourneyTaskList(expectedAboutSection, expectedEstablishersSection,
        expectedTrusteesSection, expectedWorkingKnowledgeSection, expectedDeclarationLink)
    }

    "return blank task list if there are no user answers" in {
      val blankJourneyTaskList = JourneyTaskList(
        JourneyTaskListSection(None, aboutSectionDefaultLink, None),
        Seq.empty,
        Seq.empty,
        JourneyTaskListSection(None, workingKnowledgeDefaultLink, None),
        None)

      new TaskListHelper(None).taskList mustBe blankJourneyTaskList
    }
  }


  "declarationEnabled" must {

    "return false with no establishers and trustees" in {

      val helper = new TaskListHelper(Some(declarationWithoutEstabliserAndTrustees()))
      helper.declarationEnabled(declarationWithoutEstabliserAndTrustees()) mustBe false
    }

    "return false with establishers in progress and no trustees" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndNoTrustees(isEstablisherCompleteId = false)))
      helper.declarationEnabled(declarationWithEstabliserAndNoTrustees(isEstablisherCompleteId = false)) mustBe false
    }

    "return false with establishers completed and no trustees" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndNoTrustees()))
      helper.declarationEnabled(declarationWithEstabliserAndNoTrustees()) mustBe false
    }

    "return false with establishers in progress and trustees completed" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndTrustees(isEstablisherCompleteId = false)))
      helper.declarationEnabled(declarationWithEstabliserAndTrustees(isEstablisherCompleteId = false)) mustBe false
    }

    "return false with establishers completed and trustees in progress" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndTrustees(isTrusteeCompleteId = false)))
      helper.declarationEnabled(declarationWithEstabliserAndTrustees(isTrusteeCompleteId = false)) mustBe false
    }

    "return true with establishers and trustees completed" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndTrustees()))
      helper.declarationEnabled(declarationWithEstabliserAndTrustees()) mustBe true
    }

    "return true with establishers completed and HaveAnyTrusteesId set to false and scheme type is other" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndHaveAnyTrusteesAndOtherSchemeType(haveAnyTrusteesId = false)))
      helper.declarationEnabled(declarationWithEstabliserAndHaveAnyTrusteesAndOtherSchemeType(haveAnyTrusteesId = false)) mustBe true
    }

    "return false with establishers completed and HaveAnyTrusteesId set to true and scheme type is Single" in {

      val helper = new TaskListHelper(Some(declarationWithoutEstabliserAndHaveAnyTrustees(haveAnyTrusteesId = true)))
      helper.declarationEnabled(declarationWithoutEstabliserAndHaveAnyTrustees(haveAnyTrusteesId = true)) mustBe false
    }

    "return true with establishers completed and HaveAnyTrusteesId set to false" in {

      val helper = new TaskListHelper(Some(declarationWithEstabliserAndTrustees()))
      helper.declarationEnabled(declarationWithEstabliserAndTrustees()) mustBe true
    }
  }

  "linkTarget" must {

    "return correct link for establishers company if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherCompany, 0) mustBe
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0).url

    }

    "return correct link for establishers if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherCompany.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 0).url

    }

    "return correct link for establishers partnership if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherPartnership, 0) mustBe
        controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(0).url

    }

    "return correct link for establishers partnership if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherPartnership.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 0).url

    }

    "return correct link for establishers individual if its completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherIndividual, 0) mustBe
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(0).url

    }

    "return correct link for establishers individual if its not completed" in {
      val helper = new TaskListHelper(None)
      helper.linkTarget(establisherIndividual.copy(isCompleted = false), 0) mustBe
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0).url

    }
  }

}

object TaskListHelperSpec extends SpecBase with JsonFileReader {

  private val aboutSectionDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.SchemeDetailsController.onPageLoad(NormalMode).url)
  }

  private val aboutSectionCompletedLink: Link = {
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.CheckYourAnswersController.onPageLoad.url)
  }

  private val workingKnowledgeDefaultLink: Link = {
    Link(messages("messages__schemeTaskList__working_knowledge_add_link"),
      controllers.routes.WorkingKnowledgeController.onPageLoad().url)
  }

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswers = UserAnswers(userAnswersJson)
  val inProgressEst = Seq(EstablisherCompanyEntity(CompanyDetailsId(0), "test 1", false, true),
    EstablisherIndividualEntity(EstablisherDetailsId(1), "test 2", false, false))

  val expectedAboutSection = JourneyTaskListSection(
    Some(true),
    aboutSectionCompletedLink,
    None)

  private val expectedWorkingKnowledgeSection = JourneyTaskListSection(
    Some(true),
    workingKnowledgeDefaultLink,
    None)

  private val expectedEstablishersSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__company_link"),
        controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0).url),
      Some("Test company name")),
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__individual_link"),
        controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(1).url),
      Some("Test individual name")))

  private val expectedTrusteesSection = Seq(
    JourneyTaskListSection(Some(true),
      Link(messages("messages__schemeTaskList__partnership_link"),
        controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(0).url),
      Some("Test partnership name")))

  private val expectedDeclarationLink = Some(Link(messages("messages__schemeTaskList__declaration_link"),
    controllers.register.routes.DeclarationController.onPageLoad().url))

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)

  private val schemeDetails = SchemeDetails("Test Scheme Name", SchemeType.SingleTrust)
  private val companyDetails = CompanyDetails("test company", Some("vat"), Some("paye"))

  private def declarationWithoutEstabliserAndTrustees(schemeType: SchemeType = SchemeType.SingleTrust) : UserAnswers = {
    UserAnswers()
      .set(SchemeDetailsId)(schemeDetails.copy(schemeType = schemeType)).asOpt.value
      .set(IsAboutSchemeCompleteId)(true).asOpt.value
      .set(IsWorkingKnowledgeCompleteId)(true).asOpt.value
  }

  private def declarationWithEstabliserAndTrustees(isEstablisherCompleteId : Boolean = true,
                                           isTrusteeCompleteId : Boolean = true) : UserAnswers = {
    declarationWithoutEstabliserAndTrustees()
      .set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(0))(true).asOpt.value
      .set(EstablisherDetailsId(1))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(1))(isEstablisherCompleteId).asOpt.value
      .set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsTrusteeCompleteId(0))(true).asOpt.value
      .set(TrusteeDetailsId(1))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsTrusteeCompleteId(1))(isTrusteeCompleteId).asOpt.value
  }

  private def declarationWithEstabliserAndNoTrustees(isEstablisherCompleteId : Boolean = true) : UserAnswers = {
    declarationWithoutEstabliserAndHaveAnyTrustees()
      .set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(0))(true).asOpt.value
      .set(EstablisherDetailsId(1))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(1))(isEstablisherCompleteId).asOpt.value
  }

  private def declarationWithoutEstabliserAndHaveAnyTrustees(haveAnyTrusteesId : Boolean = false) : UserAnswers = {
    declarationWithoutEstabliserAndTrustees()
      .set(HaveAnyTrusteesId)(haveAnyTrusteesId).asOpt.value
  }

  private def declarationWithEstabliserAndHaveAnyTrusteesAndOtherSchemeType(haveAnyTrusteesId : Boolean = false) : UserAnswers = {
    declarationWithoutEstabliserAndTrustees(SchemeType.BodyCorporate)
      .set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(0))(true).asOpt.value
      .set(EstablisherDetailsId(1))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      .set(IsEstablisherCompleteId(1))(true).asOpt.value
      .set(HaveAnyTrusteesId)(haveAnyTrusteesId).asOpt.value
  }

  private val establisherCompany = EstablisherCompanyEntity(CompanyDetailsId(Index(0)), "Test Comapny", isDeleted = false, isCompleted = true)
  private val establisherPartnership = EstablisherPartnershipEntity(PartnershipDetailsId(Index(0)), "Test Partnership", isDeleted = false, isCompleted = true)
  private val establisherIndividual = EstablisherIndividualEntity(EstablisherDetailsId(Index(0)), "Test Partnership", isDeleted = false, isCompleted = true)
}
