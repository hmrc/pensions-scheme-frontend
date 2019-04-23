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
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId}
import identifiers.register.establishers.company.{CompanyPayeId, CompanyVatId, CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId, CompanyPayeId => TrusteeCompanyPayeId, CompanyVatId => TrusteeCompanyVatId}
import identifiers.register.trustees.{IsTrusteeCompleteId, IsTrusteeNewId, MoreThanTenTrusteesId}
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.company.{CompanyVatId => TrusteeCompanyVatId}
import identifiers.register.trustees.company.{CompanyPayeId => TrusteeCompanyPayeId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeCompleteId, MoreThanTenTrusteesId}
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import models._
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.JsResult
import utils.{HsTaskListHelper, UserAnswers}
import viewmodels._

trait HsTaskListHelperBehaviour extends SpecBase with MustMatchers with OptionValues {

  protected val createTaskListHelper:UserAnswers => HsTaskListHelper

  protected lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  protected lazy val schemeInfoLinkText = messages("messages__schemeTaskList__scheme_info_link_text")
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


  protected def answersData(isCompleteBeforeStart: Boolean = true,
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
        _.set(IsEstablisherNewId(0))(true).flatMap(
          _.set(CompanyVatId(0))(Vat.No).flatMap(
            _.set(CompanyPayeId(0))(Paye.No).flatMap(
              _.set(EstablisherCompanyDetailsId(1))(CompanyDetails("test company", false)).flatMap(
                _.set(IsEstablisherNewId(1))(true).flatMap(
                  _.set(CompanyVatId(1))(Vat.No).flatMap(
                    _.set(CompanyPayeId(1))(Paye.No).flatMap(
                      _.set(IsEstablisherCompleteId(1))(isCompleteEstablisher).flatMap(
                        _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                          _.set(IsEstablisherNewId(2))(true).flatMap(
                            _.set(CompanyVatId(2))(Vat.No).flatMap(
                              _.set(CompanyPayeId(2))(Paye.No).flatMap(
                                _.set(IsEstablisherCompleteId(2))(isCompleteEstablisher)
                              )))))))))))))).asOpt.value
  }

  protected def allTrustees(isCompleteTrustees: Boolean = true): UserAnswers = {
    UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
      _.set(IsTrusteeCompleteId(0))(isCompleteTrustees).flatMap(
        _.set(IsTrusteeNewId(0))(true).flatMap(
          _.set(TrusteeCompanyVatId(0))(Vat.No).flatMap(
            _.set(TrusteeCompanyPayeId(0))(Paye.No).flatMap(
              _.set(TrusteeCompanyDetailsId(1))(CompanyDetails("test company", false)).flatMap(
                _.set(IsTrusteeNewId(1))(true).flatMap(
                  _.set(TrusteeCompanyVatId(1))(Vat.No).flatMap(
                    _.set(TrusteeCompanyPayeId(1))(Paye.No).flatMap(
                      _.set(IsTrusteeCompleteId(1))(isCompleteTrustees).flatMap(
                        _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                          _.set(IsTrusteeNewId(2))(true).flatMap(
                            _.set(TrusteeCompanyVatId(2))(Vat.No).flatMap(
                              _.set(TrusteeCompanyPayeId(2))(Paye.No).flatMap(
                                _.set(IsPartnershipCompleteId(2))(isCompleteTrustees)
                              )))))))))))))).asOpt.value
  }


  def beforeYouStartSection(createTaskListHelper: UserAnswers => HsTaskListHelper, linkContent: String, mode: Mode, srn: Option[String]): Unit = {

    def link(target: String) = Link(linkContent, target)

    "return the link to scheme name page when not completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartSection(userAnswers, mode, srn).link mustBe
        link(controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }

    "return the link to cya page when completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartSection(userAnswers, mode, srn).link mustBe link(
        controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url
      )
    }
  }


  def addEstablisherHeader(mode: Mode , srn: Option[String]): Unit = {

    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = UserAnswers()
      val helper = createTaskListHelper(userAnswers)
      helper.addEstablisherHeader(userAnswers, mode, srn) mustBe
        SchemeDetailsTaskListSection(None, Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, userAnswers.allEstablishers.size, srn).url), None)
    }

    "return the link to add establisher page when establishers are added " in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addEstablisherHeader(userAnswers, mode, srn) mustBe
        SchemeDetailsTaskListSection(None, Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url), None)
    }
  }

  def addTrusteeHeader(mode: Mode , srn: Option[String]): Unit = {

    "not display when do you have any trustees is false " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, mode, srn) mustBe None
    }

    "display and link should go to trustee kind page when do you have any trustees is true and no trustees are added " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListSection(None, Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url), None)
    }

    "display and link should go to add trustees page and status is completed when do you have any trustees is not present" +
      "and trustees are added and completed" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(true)
      ).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListSection(Some(true), Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      "and trustees are added and not completed" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
        _.set(IsTrusteeCompleteId(0))(false)
      ).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListSection(Some(false), Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url), None)
    }
  }

  protected def mustHaveLink(helper:HsTaskListHelper, userAnswers:UserAnswers):Unit =
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe
      Some(Link(declarationLinkText, controllers.register.routes.DeclarationController.onPageLoad().url)) )

  protected def mustHaveNoLink(helper:HsTaskListHelper, userAnswers:UserAnswers):Unit =
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe None )

  //scalastyle:off method.length
  def declarationSection(): Unit = {
    "have link when all the sections are completed with trustees" in {
      val userAnswers = answersData().asOpt.value
      mustHaveLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "have link when all the sections are completed without trustees and do you have trustees is false " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).flatMap(
        _.set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
                _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(IsEstablisherCompleteId(0))(true)).flatMap(
                  _.set(HaveAnyTrusteesId)(false)).flatMap(
                  _.set(InsuranceDetailsChangedId)(true))
              )
            )
          )
        )
      ).asOpt.value
      mustHaveLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when all the sections are completed with 10 trustees but the more than ten question has not been answered" in {
      val userAnswers = answersDataWithTenTrustees().asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "have link when all the sections are completed with 10 trustees and the more than ten question has been answered" in {
      val userAnswers = answersDataWithTenTrustees().flatMap(
        _.set(MoreThanTenTrusteesId)(true)
      ).asOpt.value
      mustHaveLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when about you start section not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when about members section not completed" in {
      val userAnswers = answersData(isCompleteAboutMembers = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when about benefits and insurance section not completed" in {
      val userAnswers = answersData(isCompleteAboutBenefits = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when establishers section not completed" in {
      val userAnswers = answersData(isCompleteEstablishers = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when trustees section not completed" in {
      val userAnswers = answersData(isCompleteTrustees = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "not have link when do you have any trustees is true but no trustees are added" in {
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
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "have link when all the sections are completed" in {
      val userAnswers = answersData().asOpt.value
      mustHaveLink(createTaskListHelper(userAnswers), userAnswers)
    }

    "have no link when all the sections are not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      mustHaveNoLink(createTaskListHelper(userAnswers), userAnswers)
    }
  }
}


