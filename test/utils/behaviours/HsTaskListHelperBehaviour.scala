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

import base.{JsonFileReader, SpecBase}
import config.FeatureSwitchManagementService
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import helpers.DataCompletionHelper
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.establishers.{IsEstablisherAddressCompleteId, IsEstablisherCompleteId, IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.trustees.individual.{TrusteeDetailsId, _}
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeNewId, MoreThanTenTrusteesId, company => trusteesCompany}
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import models._
import models.person.{PersonDetails, PersonName}
import models.register.SchemeType
import models.register.SchemeType.SingleTrust
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.{JsArray, JsObject, JsPath, JsResult, Json}
import utils.hstasklisthelper.HsTaskListHelper
import utils.{FakeFeatureSwitchManagementService, UserAnswers}
import viewmodels._

trait HsTaskListHelperBehaviour extends SpecBase with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader {

  protected lazy val beforeYouStartLinkText: String = messages("messages__schemeTaskList__before_you_start_link_text")
  protected lazy val schemeInfoLinkText: String = messages("messages__schemeTaskList__scheme_info_link_text")
  protected lazy val aboutMembersLinkText: String = messages("messages__schemeTaskList__about_members_link_text")
  protected lazy val aboutBenefitsAndInsuranceLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  protected lazy val aboutBankDetailsLinkText: String = messages("messages__schemeTaskList__about_bank_details_link_text")
  protected lazy val workingKnowledgeLinkText: String = messages("messages__schemeTaskList__working_knowledge_link_text")
  protected lazy val addEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  protected lazy val changeEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  protected lazy val companyLinkText: String = messages("messages__schemeTaskList__company_link")
  protected lazy val individualLinkText: String = messages("messages__schemeTaskList__individual_link")
  protected lazy val partnershipLinkText: String = messages("messages__schemeTaskList__partnership_link")
  protected lazy val addTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_add_link")
  protected lazy val addTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_add_additional_text")
  protected lazy val changeTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val addDeleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val deleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_delete_link")
  protected lazy val deleteTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_delete_additional_text")
  protected lazy val declarationLinkText: String = messages("messages__schemeTaskList__declaration_link")
  val deletedEstablishers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
    _.set(IsEstablisherCompleteId(0))(false).flatMap(
      _.set(IsEstablisherNewId(0))(true).flatMap(
        _.set(establisherCompanyPath.CompanyDetailsId(1))(CompanyDetails("test company", true)).flatMap(
          _.set(IsEstablisherCompleteId(1))(true).flatMap(
            _.set(IsEstablisherNewId(1))(true).flatMap(
              _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                _.set(IsEstablisherNewId(2))(true).flatMap(
                  _.set(IsEstablisherCompleteId(2))(false)
                )))))))).asOpt.value
  protected val createTaskListHelper: (UserAnswers, FeatureSwitchManagementService) => HsTaskListHelper
  private val isHnS1Enabled = false
  private val isHnS2Enabled = false
  private val fakeFeatureManagementService = new FakeFeatureSwitchManagementService(isHnS1Enabled)
  private val fakeFeatureManagementServiceToggleON = new FakeFeatureSwitchManagementService(true)



  def beforeYouStartSection(createTaskListHelper: UserAnswers => HsTaskListHelper, linkContent: String, mode: Mode, srn: Option[String]): Unit = {

    def link(target: String) = Link(linkContent, target)

    "return the link to scheme name page when not completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartLink(userAnswers, mode, srn) mustBe
        link(controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }

    "return the link to cya page when completed " in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartLink(userAnswers, mode, srn) mustBe link(
        controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url
      )
    }
  }

  def addEstablisherHeader(mode: Mode, srn: Option[String]): Unit = {

    "return the link to establisher kind page when no establishers are added " in {
      val userAnswers = UserAnswers()
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addEstablisherHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
          controllers.register.establishers.routes.EstablisherKindController.onPageLoad(mode, userAnswers.allEstablishers(isHnS1Enabled, isHnS2Enabled, mode).size, srn).url)), None)
    }

    "return the link to add establisher page when establishers are added " in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addEstablisherHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)), None)
    }

    "return the link to add establisher page when establishers are added with toggle ON " in {
      val userAnswers = UserAnswers().set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(IsEstablisherCompleteId(0))(true).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementServiceToggleON)
      helper.addEstablisherHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeEstablisherLinkText,
          controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn).url)), None)
    }
  }

  def establishersSectionHnS(mode: Mode, srn: Option[String]): Unit = {
    def modeBasedCompletion(completion: Option[Boolean]): Option[Boolean] = if (mode == NormalMode) completion else None

    "return the seq of establishers sub sections when h&s toggle is on" in {
      val userAnswers = establisherCompany()
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.establishers(userAnswers, mode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
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

  def trusteesSectionHnS(mode: Mode, srn: Option[String]): Unit = {
    def modeBasedCompletion(completion: Option[Boolean]): Option[Boolean] = if (mode == NormalMode) completion else None

    "return the seq of trustees sub sections when h&s toggle is on when all spokes are uninitiated" in {
      val userAnswers = trusteeCompany(false)
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.trustees(userAnswers, mode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), None),
              EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
                trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), None)
            ), Some("test company"))
        )
    }

    "return the seq of trustees sub sections when h&s toggle is on when all spokes are completed" in {
      val userAnswers = allAnswersHnS
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.trustees(userAnswers, mode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
                trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(Some(true)))
            ), Some("test company")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "firstName lastName"),
              trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, 1, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, 1, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "firstName lastName"),
                trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, 1, srn).url), modeBasedCompletion(Some(true)))
            ), Some("firstName lastName")),
          SchemeDetailsTaskListEntitySection(None,
            Seq(EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test partnership"),
              trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(Some(true))),
              EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test partnership"),
                trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(Some(true)))
            ), Some("test partnership"))
        )
    }
  }

  protected def establisherCompany(isCompleteEstablisher: Boolean = true): UserAnswers = {
    UserAnswers().set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company", false)).flatMap(
      _.set(IsEstablisherNewId(0))(true).flatMap(
        _.set(IsEstablisherAddressCompleteId(0))(isCompleteEstablisher).flatMap(
          _.set(establisherCompanyPath.CompanyPayeId(0))(Paye.No).flatMap(
            _.set(IsEstablisherCompleteId(0))(isCompleteEstablisher)
          )))).asOpt.value
  }

  def trusteeTests(mode: Mode, srn: Option[String], toggle:Boolean):Unit = {
    val fsm:FakeFeatureSwitchManagementService = if(toggle) fakeFeatureManagementServiceToggleON else fakeFeatureManagementService

    s"display and link should go to trustee kind page when do you have any trustees is true and no trustees are added  with toggle set to $toggle" in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees(isHnSEnabled = toggle).size, srn).url)), None)
    }

    "display and link should go to add trustees page when do you have any trustees is not present" +
      s"and trustees are added and completed  with toggle set to $toggle" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      s"and trustees are added and not completed  with toggle set to $toggle" in {
      val userAnswers = UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now()))
       .asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }
  }

  //scalastyle:off method.length
  def addTrusteeHeader(mode: Mode, srn: Option[String]): Unit = {

    "display correct link data when 2 trustees exist " in {
      val userAnswers = UserAnswers()
        .set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(TrusteeDetailsId(1))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addDeleteTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }

    "display correct link data when trustee is optional and no trustee exists " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees(isHnS1Enabled).size, srn).url)), None, None)
    }

    "display correct link data when trustee is mandatory and no trustees exists " in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.MasterTrust).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees(isHnS1Enabled).size, srn).url)), None,
          None)
    }

    s"display correct link data when trustee is mandatory and 1 trustee exists with toggle off" in {
      val userAnswers = UserAnswers().set(HaveAnyTrusteesId)(true).asOpt.value
        .set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).asOpt.value
        .set(SchemeTypeId)(SchemeType.MasterTrust).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None, Some(addTrusteesAdditionalInfo))
    }

    s"display correct link data when 10 trustees exist  with toggle off" in {
      val userAnswers = answersDataWithTenTrustees(toggled = false)
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(deleteTrusteesLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None,
          Some(deleteTrusteesAdditionalInfo))
    }

    trusteeTests(mode, srn, toggle = false)
    trusteeTests(mode, srn, toggle = true)

  }

  def declarationTests(toggled: Boolean) = {

    val fsm:FakeFeatureSwitchManagementService = if(toggled) fakeFeatureManagementServiceToggleON else fakeFeatureManagementService

    s"not have link when about you start section not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteBeforeStart = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when about members section not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteAboutMembers = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when about benefits and insurance section not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteAboutBenefits = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when establishers section not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteEstablishers = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when trustees section not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteTrustees = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have link when all the sections are completed when toggle is set to $toggled" in {
      val userAnswers = (if(toggled) allAnswersHnS else allAnswers).set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have no link when all the sections are not completed when toggle is set to $toggled" in {
      val userAnswers = answersData(isCompleteBeforeStart = false, toggled = toggled).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have link when all the sections are completed with trustees when toggle is set to $toggled" in {
      val userAnswers = (if(toggled) allAnswersHnS else allAnswers).set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers)
    }
  }
  //scalastyle:off method.length
  def declarationSection(): Unit = {
    "have link when all the sections are completed without trustees and do you have trustees is false " in {
      val userAnswers = allAnswers.set(IsBeforeYouStartCompleteId)(true).flatMap(
        _.set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
                  _.set(HaveAnyTrusteesId)(false)).flatMap(
                  _.set(InsuranceDetailsChangedId)(true))
              )
          )
        )
      ).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }

    "not have link when all the sections are completed with 10 trustees but the more than ten question has not been answered" in {
      val userAnswers = answersDataWithTenTrustees(toggled = false)
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }

    "not have link when all the sections are completed with 10 trustees but the more than ten question has not been answered with toggle ON" in {
      val userAnswers = answersDataWithTenTrustees(toggled = true)
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementServiceToggleON), userAnswers)
    }

    "have link when all the sections are completed with 10 trustees and the more than ten question has been answered" in {
      val userAnswers = answersDataWithTenTrustees(toggled = false).set(MoreThanTenTrusteesId)(true).asOpt.value

      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }

    "have link when all the sections are completed with 10 trustees and the more than ten question has been answered with toggle ON" in {
      val userAnswers = answersDataWithTenTrustees(toggled = true).set(MoreThanTenTrusteesId)(true).asOpt.value

      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fakeFeatureManagementServiceToggleON), userAnswers)
    }

    declarationTests(toggled = false)
    declarationTests(toggled =true)

    "not have link when trustees section not completed" in {
      val userAnswers = answersData(isCompleteTrustees = false, toggled = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }

    "not have link when do you have any trustees is true but no trustees are added" in {
      val userAnswers = UserAnswers().set(IsBeforeYouStartCompleteId)(true).flatMap(
        _.set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
                _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                  _.set(SchemeTypeId)(SingleTrust)).flatMap(
                  _.set(IsEstablisherCompleteId(0))(true)).flatMap(
                  _.set(HaveAnyTrusteesId)(true))
              )
            )
          )
        )
      ).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
    }
  }

  private def answersDataWithTenTrustees(toggled: Boolean) = {
    val answers = if(toggled) allAnswersHnS else allAnswers
    val trustee = (answers.json \ "trustees" \ 0).get
    UserAnswers(answers.json.as[JsObject] - "trustees" + ("trustees" -> Json.toJson(List.fill(10)(trustee)).as[JsArray]))
  }

  protected def answersData(isCompleteBeforeStart: Boolean = true,
                            isCompleteAboutMembers: Boolean = true,
                            isCompleteAboutBank: Boolean = true,
                            isCompleteAboutBenefits: Boolean = true,
                            isCompleteWk: Boolean = true,
                            isCompleteEstablishers: Boolean = true,
                            isCompleteTrustees: Boolean = true,
                            isChangedInsuranceDetails: Boolean = true,
                            isChangedEstablishersTrustees: Boolean = true,
                            toggled: Boolean
                           ): JsResult[UserAnswers] = {

    setTrusteeCompletionStatusJsResult(isComplete = isCompleteTrustees, toggled = toggled, 0, UserAnswers()
      .set(IsBeforeYouStartCompleteId)(isCompleteBeforeStart).flatMap(
      _.set(IsAboutMembersCompleteId)(isCompleteAboutMembers).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(isCompleteAboutBank).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(isCompleteAboutBenefits).flatMap(
            _.set(IsWorkingKnowledgeCompleteId)(isCompleteWk).flatMap(
              _.set(EstablisherDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())).flatMap(
                _.set(IsEstablisherCompleteId(0))(isCompleteEstablishers)).flatMap(
                _.set(IsEstablisherAddressCompleteId(0))(isCompleteEstablishers)).flatMap(
                _.set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now())))
            )
          )
        )
      )
    ).asOpt.value)
  }

  protected def mustHaveDeclarationLinkEnabled(helper: HsTaskListHelper, userAnswers: UserAnswers, url: Option[String] = None): Unit =
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe
      Some(Link(declarationLinkText, url.getOrElse(controllers.register.routes.DeclarationController.onPageLoad().url))))

  protected def mustNotHaveDeclarationLink(helper: HsTaskListHelper, userAnswers: UserAnswers): Unit =
    helper.declarationSection(userAnswers).foreach(_.declarationLink mustBe None)

  protected def allAnswers: UserAnswers = UserAnswers(readJsonFromFile("/payload.json"))
  protected def allAnswersHnS: UserAnswers = UserAnswers(readJsonFromFile("/payloadHnS.json"))
  protected def allAnswersIncomplete: UserAnswers = UserAnswers(readJsonFromFile("/payloadIncomplete.json"))

  protected def trusteeCompany(isCompleteTrustee: Boolean = true): UserAnswers =
    UserAnswers().set(trusteesCompany.CompanyDetailsId(0))(CompanyDetails("test company", false)).flatMap(
      _.set(IsTrusteeNewId(0))(true)).asOpt.value

  protected def allTrustees(isCompleteTrustees: Boolean = true, toggled: Boolean): UserAnswers = {
    setTrusteeCompletionStatus(isCompleteTrustees, toggled, 0, UserAnswers().set(TrusteeDetailsId(0))(PersonDetails("firstName", None, "lastName", LocalDate.now()))
      .flatMap(
        _.set(IsTrusteeNewId(0))(true).flatMap(
            _.set(trusteesCompany.CompanyVatId(0))(Vat.No).flatMap(
              _.set(trusteesCompany.CompanyPayeId(0))(Paye.No).flatMap(
                _.set(trusteesCompany.CompanyDetailsId(1))(CompanyDetails("test company", false)).flatMap(
                  _.set(IsTrusteeNewId(1))(true).flatMap(
                      _.set(trusteesCompany.CompanyVatId(1))(Vat.No).flatMap(
                        _.set(trusteesCompany.CompanyPayeId(1))(Paye.No).flatMap(
                            _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                              _.set(IsTrusteeNewId(2))(true).flatMap(
                                  _.set(trusteesCompany.CompanyVatId(2))(Vat.No).flatMap(
                                    _.set(trusteesCompany.CompanyPayeId(2))(Paye.No)
                                    ))))))))))).asOpt.value)
  }

  protected def allTrusteesIndividual(isCompleteTrustees: Boolean = true, toggled: Boolean): UserAnswers = {
    setTrusteeCompletionStatus(
      isCompleteTrustees, toggled, 0, UserAnswers()
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).flatMap(
        _.set(IsTrusteeNewId(0))(true)
      ).asOpt.value
    )
  }
}
