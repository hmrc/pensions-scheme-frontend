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
import identifiers.register.establishers.individual.{EstablisherDetailsId, EstablisherNameId}
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteesCompany}
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import models.person.{PersonDetails, PersonName}
import models.register.SchemeType
import models.{person, _}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.{JsArray, JsObject, JsResult, Json}
import utils.hstasklisthelper.HsTaskListHelper
import utils.{FakeFeatureSwitchManagementService, UserAnswers}
import viewmodels._

trait HsTaskListHelperBehaviour extends SpecBase with MustMatchers with OptionValues with DataCompletionHelper with JsonFileReader {
  protected val schemeName = "scheme"
  protected val userAnswersWithSchemeName:UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value

  protected lazy val beforeYouStartLinkText: String = messages("messages__schemeTaskList__before_you_start_link_text", schemeName)
  protected lazy val schemeInfoLinkText: String = messages("messages__schemeTaskList__scheme_info_link_text")
  protected lazy val aboutMembersLinkText: String = messages("messages__schemeTaskList__about_members_link_text", schemeName)
  protected lazy val aboutMembersViewLinkText: String = messages("messages__schemeTaskList__about_members_link_text_view", schemeName)
  protected lazy val aboutBenefitsAndInsuranceLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text", schemeName)
  protected lazy val aboutBenefitsAndInsuranceViewLinkText: String = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName)
  protected lazy val aboutBankDetailsLinkText: String = messages("messages__schemeTaskList__about_bank_details_link_text", schemeName)
  protected lazy val workingKnowledgeLinkText: String = messages("messages__schemeTaskList__working_knowledge_link_text", schemeName)
  protected lazy val addEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  protected lazy val changeEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  protected lazy val viewEstablisherLinkText: String = messages("messages__schemeTaskList__sectionEstablishers_view_link")
  protected lazy val companyLinkText: String = messages("messages__schemeTaskList__company_link")
  protected lazy val individualLinkText: String = messages("messages__schemeTaskList__individual_link")
  protected lazy val partnershipLinkText: String = messages("messages__schemeTaskList__partnership_link")
  protected lazy val addTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_add_link")
  protected lazy val addTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_add_additional_text")
  protected lazy val viewTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_view_link")
  protected lazy val changeTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val addDeleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_change_link")
  protected lazy val deleteTrusteesLinkText: String = messages("messages__schemeTaskList__sectionTrustees_delete_link")
  protected lazy val deleteTrusteesAdditionalInfo: String = messages("messages__schemeTaskList__sectionTrustees_delete_additional_text")
  protected lazy val declarationLinkText: String = messages("messages__schemeTaskList__declaration_link")
  val deletedEstablishers = userAnswersWithSchemeName.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).flatMap(
      _.set(IsEstablisherNewId(0))(true).flatMap(
        _.set(establisherCompanyPath.CompanyDetailsId(1))(CompanyDetails("test company", true)).flatMap(
            _.set(IsEstablisherNewId(1))(true).flatMap(
              _.set(EstablisherPartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                _.set(IsEstablisherNewId(2))(true)
                ))))).asOpt.value
  protected val createTaskListHelper: (UserAnswers, FeatureSwitchManagementService) => HsTaskListHelper
  protected val isHnS2Enabled = false
  private val fakeFeatureManagementService = new FakeFeatureSwitchManagementService(true)

  def beforeYouStartSection(createTaskListHelper: UserAnswers => HsTaskListHelper, linkContent: String, mode: Mode, srn: Option[String]): Unit = {

    def link(target: String) = Link(linkContent, target)

    "return the link to scheme name page when not completed " in {
      val userAnswers = userAnswersWithSchemeName.set(IsBeforeYouStartCompleteId)(false).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartLink(userAnswers, mode, srn) mustBe
        link(controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)
    }

    "return the link to cya page when completed " in {
      val userAnswers = userAnswersWithSchemeName.set(IsBeforeYouStartCompleteId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers)
      helper.beforeYouStartLink(userAnswers, mode, srn) mustBe link(
        controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(mode, srn).url
      )
    }
  }

  def establishersSectionHnS(mode: Mode, srn: Option[String]): Unit = {
    def modeBasedCompletion(completion: Option[Boolean]): Option[Boolean] = if (mode == NormalMode) completion else None

    "return the seq of establishers sub sections" in {
      val userAnswers = establisherCompany()
      val helper = createTaskListHelper(userAnswers, new FakeFeatureSwitchManagementService(true))
      helper.establishers(userAnswers, mode, srn) mustBe
        Seq(
          SchemeDetailsTaskListEntitySection(None,
            Seq(
              EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
                establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), Some(false)),
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

    "return the seq of trustees sub sections when all spokes are uninitiated" in {
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
  }

  protected def establisherCompany(isCompleteEstablisher: Boolean = true): UserAnswers = {
    userAnswersWithSchemeName.set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company", false)).flatMap(
      _.set(IsEstablisherNewId(0))(true).flatMap(
          _.set(establisherCompanyPath.HasCompanyPAYEId(0))(false)
          )).asOpt.value
  }

  def trusteeTests(mode: Mode, srn: Option[String],
                   addLinkText:String = addTrusteesLinkText,
                   changeLinkText:String = changeTrusteesLinkText):Unit = {
    val fsm:FakeFeatureSwitchManagementService = fakeFeatureManagementService

    s"display and link should go to trustee kind page when do you have any trustees is true and no trustees are added" in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url)), None)
    }

    "display and link should go to add trustees page when do you have any trustees is not present" +
      s"and trustees are added and completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName")).asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }

    "display and link should go to add trustees page and status is not completed when do you have any trustees is not present" +
      s"and trustees are added and not completed" in {
      val userAnswers = userAnswersWithSchemeName.set(TrusteeNameId(0))(person.PersonName("firstName", "lastName"))
       .asOpt.value
      val helper = createTaskListHelper(userAnswers, fsm)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(changeLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }
  }

  //scalastyle:off method.length
  def addTrusteeHeader(mode: Mode, srn: Option[String],
                       addDeleteLinkText:String = addDeleteTrusteesLinkText,
                       addLinkText:String = addTrusteesLinkText,
                       changeLinkText:String = changeTrusteesLinkText): Unit = {

    "display correct link data when 2 trustees exist " in {
      val userAnswers = userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).asOpt.value
        .set(TrusteeNameId(1))(PersonName("firstName",  "lastName")).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addDeleteLinkText,
          controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, srn).url)), None)
    }

    "display correct link data when trustee is optional and no trustee exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.BodyCorporate).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url)), None, None)
    }

    "display correct link data when trustee is mandatory and no trustees exists " in {
      val userAnswers = userAnswersWithSchemeName.set(HaveAnyTrusteesId)(true).asOpt.value
        .set(SchemeTypeId)(SchemeType.MasterTrust).asOpt.value
      val helper = createTaskListHelper(userAnswers, fakeFeatureManagementService)
      helper.addTrusteeHeader(userAnswers, mode, srn).value mustBe
        SchemeDetailsTaskListHeader(None, Some(Link(addTrusteesLinkText,
          controllers.register.trustees.routes.TrusteeKindController.onPageLoad(mode, userAnswers.allTrustees.size, srn).url)), None,
          None)
    }

    trusteeTests(mode, srn, addLinkText, changeLinkText)
  }

  def declarationTests():Unit = {

    val fsm:FakeFeatureSwitchManagementService = fakeFeatureManagementService

    s"not have link when about you start section not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when about members section not completed" in {
      val userAnswers = answersData(isCompleteAboutMembers = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when about benefits and insurance section not completed" in {
      val userAnswers = answersData(isCompleteAboutBenefits = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when establishers section not completed" in {
      val userAnswers = answersData(isCompleteEstablishers = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"not have link when trustees section not completed" in {
      val userAnswers = answersData(isCompleteTrustees = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have link when all the sections are completed" in {
      val userAnswers = allAnswers.set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have no link when all the sections are not completed" in {
      val userAnswers = answersData(isCompleteBeforeStart = false).asOpt.value
      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fsm), userAnswers)
    }

    s"have link when all the sections are completed with trustees" in {
      val userAnswers = allAnswers.set(EstablishersOrTrusteesChangedId)(true).asOpt.value
      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fsm), userAnswers)
    }
  }
  //scalastyle:off method.length
  def declarationSection(): Unit = {
    "have link when all the sections are completed without trustees and do you have trustees is false " in {
      val userAnswers = allAnswers
        .set(IsBeforeYouStartCompleteId)(true).flatMap(
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


//    "not have link when all the sections are completed with 10 trustees but the more than ten question has not been answered" in {
//      val userAnswers = answersDataWithTenTrustees
//      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
//    }
//
//    "have link when all the sections are completed with 10 trustees and the more than ten question has been answered" in {
//      val userAnswers = answersDataWithTenTrustees.set(MoreThanTenTrusteesId)(true).asOpt.value
//
//      mustHaveDeclarationLinkEnabled(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
//    }
//
//    declarationTests
//
//
//    "not have link when do you have any trustees is true but no trustees are added" in {
//      val userAnswers = userAnswersWithSchemeName.set(IsBeforeYouStartCompleteId)(true).flatMap(
//        _.set(IsAboutMembersCompleteId)(true).flatMap(
//          _.set(IsAboutBankDetailsCompleteId)(true).flatMap(
//            _.set(IsAboutBenefitsAndInsuranceCompleteId)(true).flatMap(
//              _.set(IsWorkingKnowledgeCompleteId)(true).flatMap(
//                _.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).flatMap(
//                  _.set(SchemeTypeId)(SingleTrust)).flatMap(
//                  _.set(IsEstablisherCompleteId(0))(true)).flatMap(
//                  _.set(HaveAnyTrusteesId)(true))
//              )
//            )
//          )
//        )
//      ).asOpt.value
//      mustNotHaveDeclarationLink(createTaskListHelper(userAnswers, fakeFeatureManagementService), userAnswers)
//    }
  }

  private def answersDataWithTenTrustees = {
    val trustee = (allAnswers.json \ "trustees" \ 0).get
    UserAnswers(allAnswers.json.as[JsObject] - "trustees" + ("trustees" -> Json.toJson(List.fill(10)(trustee)).as[JsArray]))
  }

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

    setTrusteeCompletionStatusJsResult(isComplete = isCompleteTrustees, 0, userAnswersWithSchemeName
      .set(IsBeforeYouStartCompleteId)(isCompleteBeforeStart).flatMap(
      _.set(IsAboutMembersCompleteId)(isCompleteAboutMembers).flatMap(
        _.set(IsAboutBankDetailsCompleteId)(isCompleteAboutBank).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(isCompleteAboutBenefits).flatMap(
            _.set(IsWorkingKnowledgeCompleteId)(isCompleteWk).flatMap(
              _.set(EstablisherNameId(0))(PersonName("firstName", "lastName")).flatMap(
                _.set(TrusteeNameId(0))(PersonName("firstName", "lastName")))
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
  protected def allAnswersIncomplete: UserAnswers = UserAnswers(readJsonFromFile("/payloadInProgress.json")) //"/payloadIncomplete.json"

  protected def trusteeCompany(isCompleteTrustee: Boolean = true): UserAnswers =
    userAnswersWithSchemeName.set(trusteesCompany.CompanyDetailsId(0))(CompanyDetails("test company", false)).flatMap(
      _.set(IsTrusteeNewId(0))(true)).asOpt.value

  protected def allTrustees(isCompleteTrustees: Boolean = true): UserAnswers = {
    setTrusteeCompletionStatus(isCompleteTrustees, 0, userAnswersWithSchemeName.set(TrusteeNameId(0))(PersonName("firstName", "lastName"))
      .flatMap(
        _.set(IsTrusteeNewId(0))(true).flatMap(
            _.set(trusteesCompany.HasCompanyVATId(0))(false).flatMap(
              _.set(trusteesCompany.HasCompanyPAYEId(0))(false).flatMap(
                _.set(trusteesCompany.CompanyDetailsId(1))(CompanyDetails("test company", false)).flatMap(
                  _.set(IsTrusteeNewId(1))(true).flatMap(
                      _.set(trusteesCompany.HasCompanyVATId(1))(false).flatMap(
                        _.set(trusteesCompany.HasCompanyPAYEId(1))(false).flatMap(
                            _.set(TrusteePartnershipDetailsId(2))(PartnershipDetails("test partnership", false)).flatMap(
                              _.set(IsTrusteeNewId(2))(true).flatMap(
                                  _.set(trusteesCompany.HasCompanyVATId(2))(false).flatMap(
                                    _.set(trusteesCompany.HasCompanyPAYEId(2))(false)
                                    ))))))))))).asOpt.value)
  }

  protected def allTrusteesIndividual(isCompleteTrustees: Boolean = true): UserAnswers = {
    setTrusteeCompletionStatus(
      isCompleteTrustees, 0, userAnswersWithSchemeName
        .set(TrusteeNameId(0))(PersonName("firstName", "lastName")).flatMap(
        _.set(IsTrusteeNewId(0))(true)
      ).asOpt.value
    )
  }
}
