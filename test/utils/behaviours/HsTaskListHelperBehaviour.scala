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

package utils.behaviours

import base.SpecBase
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeCompleteId, MoreThanTenTrusteesId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import models.person.PersonDetails
import models.{CompanyDetails, NormalMode, PartnershipDetails}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.JsResult
import utils.{HsTaskListHelperRegistration, UserAnswers}
import viewmodels._

trait HsTaskListHelperBehaviour extends SpecBase with MustMatchers with OptionValues {

  protected lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  protected lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  protected lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  protected lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  protected lazy val workingKnowledgeLinkText = messages("messages__schemeTaskList__working_knowledge_link_text")
  protected lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  protected lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  protected lazy val companyLinkText = messages("messages__schemeTaskList__company_link")
  protected lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  protected lazy val partnershipLinkText = messages("messages__schemeTaskList__partnership_link")
  protected lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")
  protected lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val declarationLinkText = messages("messages__schemeTaskList__declaration_link")

  protected def beforeYouStartLink(link: String) = {
    Link(
      messages(beforeYouStartLinkText),
      link
    )
  }

  protected def answersData(isCompleteBeforeStart: Boolean = true,
                            isCompleteAboutMembers: Boolean = true,
                            isCompleteAboutBank: Boolean = true,
                            isCompleteAboutBenefits: Boolean = true,
                            isCompleteWk: Boolean = true,
                            isCompleteEstablishers: Boolean = true,
                            isCompleteTrustees: Boolean = true
                           ): JsResult[UserAnswers] = {
    UserAnswers().set(IsBeforeYouStartCompleteId)(isCompleteBeforeStart).flatMap(
      _.set(IsAboutMembersCompleteId)(isCompleteAboutMembers).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(isCompleteAboutBank).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(isCompleteAboutBenefits).flatMap(
            _.set(IsWorkingKnowledgeCompleteId)(isCompleteWk).flatMap(
              _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                _.set(IsEstablisherCompleteId(0))(isCompleteEstablishers)).flatMap(
                _.set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(IsTrusteeCompleteId(0))(isCompleteTrustees))
              )
            )
          )
        )
      )
    )
  }


  private def answersDataWithTenTrustees(isCompleteBeforeStart: Boolean = true,
                                         isCompleteAboutMembers: Boolean = true,
                                         isCompleteAboutBank: Boolean = true,
                                         isCompleteAboutBenefits: Boolean = true,
                                         isCompleteWk: Boolean = true,
                                         isCompleteEstablishers: Boolean = true,
                                         isCompleteTrustees: Boolean = true
                                        ): JsResult[UserAnswers] = {

    val addTrustee: (UserAnswers, Int) => JsResult[UserAnswers] = (ua, index) =>
      ua.set(TrusteeDetailsId(index))(PersonDetails(s"firstName$index", None, s"lastName$index", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(index))(isCompleteTrustees))

    answersData(isCompleteBeforeStart,
      isCompleteAboutMembers,
      isCompleteAboutBank,
      isCompleteAboutBenefits,
      isCompleteWk,
      isCompleteEstablishers,
      isCompleteTrustees)
      .flatMap(addTrustee(_, 1))
      .flatMap(addTrustee(_, 2))
      .flatMap(addTrustee(_, 3))
      .flatMap(addTrustee(_, 4))
      .flatMap(addTrustee(_, 5))
      .flatMap(addTrustee(_, 6))
      .flatMap(addTrustee(_, 7))
      .flatMap(addTrustee(_, 8))
      .flatMap(addTrustee(_, 9))
  }

  protected def allEstablishers(isCompleteEstablisher: Boolean = true): UserAnswers = {
    UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
      _.set(IsEstablisherCompleteId(0))(isCompleteEstablisher).flatMap(
        _.set(EstablisherCompanyDetailsId(1))(CompanyDetails("test company", None, None, false)).flatMap(
          _.set(IsEstablisherCompleteId(1))(isCompleteEstablisher).flatMap(
            _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
              _.set(IsEstablisherCompleteId(2))(isCompleteEstablisher)
            ))))).asOpt.value
  }

  protected def allTrustees(isCompleteTrustees: Boolean = true): UserAnswers = {
    UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
      _.set(IsTrusteeCompleteId(0))(isCompleteTrustees).flatMap(
        _.set(TrusteeCompanyDetailsId(1))(CompanyDetails("test company", None, None, false)).flatMap(
          _.set(IsTrusteeCompleteId(1))(isCompleteTrustees).flatMap(
            _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
              _.set(IsPartnershipCompleteId(2))(isCompleteTrustees)
            ))))).asOpt.value
  }

  def beforeYouStartSection(): Unit = {
    "return the link to scheme name page when not completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.beforeYouStartSection(userAnswers).link mustBe
        beforeYouStartLink(controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }

    "return the link to cya page when completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.beforeYouStartSection(userAnswers).link mustBe beforeYouStartLink(
        controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad().url
      )
    }
  }


  def addEstablisherHeader(): Unit = {

    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addEstablisherHeader(userAnswers) mustBe
        SchemeDetailsTaskListSection(None, Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, userAnswers.allEstablishers.size).url), None)
    }

    "return the link to add establisher page when establishers are added " in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addEstablisherHeader(userAnswers) mustBe
        SchemeDetailsTaskListSection(None, Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode).url), None)
    }
  }

  def addTrusteeHeader(): Unit = {

    "not display when do you have any trustees is false " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers) mustBe None
    }

    "display and link should go to trustee kind page when do you have any trustees is true and no trustees are added " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers).value mustBe
        SchemeDetailsTaskListSection(None, Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, userAnswers.allTrustees.size, None).url), None)
    }

    "display and link should go to add trustees page and status is completed when do you have any trustees is not present" +
      "and trustees are added and completed" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(true)
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(true), Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      "and trustees are added and not completed" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(false)
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.addTrusteeHeader(userAnswers).value mustBe
        SchemeDetailsTaskListSection(Some(false), Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None).url), None)
    }
  }

  def establishersSection(): Unit = {

    "return the seq of establishers sub sections for non deleted establishers which are all completed" in {
      val userAnswers = allEstablishers()
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(individualLinkText,
          controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(true), Link(companyLinkText,
            controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(NormalMode, None, 1).url), Some("test company")),
          SchemeDetailsTaskListSection(Some(true), Link(partnershipLinkText,
            controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
        )
    }

    "return the seq of establishers sub sections for non deleted establishers which are not completed" in {
      val userAnswers = allEstablishers(isCompleteEstablisher = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(individualLinkText,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(false), Link(companyLinkText,
            controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, 1).url), Some("test company")),
          SchemeDetailsTaskListSection(Some(false), Link(partnershipLinkText,
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
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
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.establishers(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(individualLinkText,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(false), Link(partnershipLinkText,
            controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
        )
    }
  }

  def trusteesSection(): Unit = {

    "return the seq of trustees sub sections for non deleted trustees which are all completed" in {
      val userAnswers = allTrustees()
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(true), Link(individualLinkText,
          controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(true), Link(companyLinkText,
            controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url), Some("test company")),
          SchemeDetailsTaskListSection(Some(true), Link(partnershipLinkText,
            controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
        )
    }

    "return the seq of trustees sub sections for non deleted trustees which are not completed" in {
      val userAnswers = allTrustees(isCompleteTrustees = false)
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(individualLinkText,
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(false), Link(companyLinkText,
            controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1, None).url), Some("test company")),
          SchemeDetailsTaskListSection(Some(false), Link(partnershipLinkText,
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
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
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.trustees(userAnswers) mustBe
        Seq(SchemeDetailsTaskListSection(Some(false), Link(individualLinkText,
          controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 0, None).url), Some("firstName lastName")),
          SchemeDetailsTaskListSection(Some(false), Link(partnershipLinkText,
            controllers.register.trustees.partnership.routes.TrusteeDetailsController.onPageLoad(NormalMode, 2, None).url), Some("test partnership"))
        )
    }
  }

  def declarationEnabled(): Unit = {

    "return true when all the sections are completed with trustees" in {
      val userAnswers = answersData().asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe true
    }

    "return true when all the sections are completed without trustees and do you have trustees is false " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).flatMap(
        _.set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
                _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(IsEstablisherCompleteId(0))(true)).flatMap(
                  _.set(HaveAnyTrusteesId)(false))
              )
            )
          )
        )
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe true
    }

    "return false when all the sections are completed with 10 trustees but the more than ten question has not been answered" in {
      val userAnswers = answersDataWithTenTrustees().asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return true when all the sections are completed with 10 trustees and the more than ten question has been answered" in {
      val userAnswers = answersDataWithTenTrustees().flatMap(
        _.set(MoreThanTenTrusteesId)(true)
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe true
    }

    "return false when about you start section not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when about members section not completed" in {
      val userAnswers = answersData(isCompleteAboutMembers = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when about bank details section not completed" in {
      val userAnswers = answersData(isCompleteAboutBank = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when about benefits and insurance section not completed" in {
      val userAnswers = answersData(isCompleteAboutBenefits = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when working knowledge section not completed" in {
      val userAnswers = answersData(isCompleteWk = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when establishers section not completed" in {
      val userAnswers = answersData(isCompleteEstablishers = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when trustees section not completed" in {
      val userAnswers = answersData(isCompleteTrustees = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }

    "return false when do you have any trustees is true but no trustees are added" in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).flatMap(
        _.set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
                _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(IsEstablisherCompleteId(0))(true)).flatMap(
                  _.set(HaveAnyTrusteesId)(true))
              )
            )
          )
        )
      ).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }
  }

  def declarationLink(): Unit = {
    "return the link when all the sections are completed" in {
      val userAnswers = answersData().asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationLink(userAnswers).value mustBe Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url)
    }

    "return None when all the sections are not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      val helper = new HsTaskListHelperRegistration(userAnswers)
      helper.declarationLink(userAnswers) mustBe None
    }
  }
}


