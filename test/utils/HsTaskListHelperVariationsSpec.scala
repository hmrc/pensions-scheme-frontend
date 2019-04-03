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

import base.SpecBase
import identifiers.{IsWorkingKnowledgeCompleteId, _}
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{PartnershipDetailsId => EstablisherPartnershipDetailsId}
import identifiers.register.trustees.IsTrusteeCompleteId
import identifiers.register.trustees.company.{CompanyDetailsId => TrusteeCompanyDetailsId}
import identifiers.register.trustees.individual.TrusteeDetailsId
import identifiers.register.trustees.partnership.{IsPartnershipCompleteId, PartnershipDetailsId => TrusteePartnershipDetailsId}
import models.person.PersonDetails
import models.{CompanyDetails, PartnershipDetails}
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.JsResult
import viewmodels._

class HsTaskListHelperVariationsSpec extends WordSpec with MustMatchers with OptionValues  {

  import HsTaskListHelperVariationsSpec._

  "declarationEnabled" must {

    "return true when all the sections are completed with trustees and atleast one change flag is true" in {
      val userAnswers = answersData().asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe true
    }

    "return false when all the sections are completed with trustees but none of the change flags is true" in {
      val userAnswers = answersData(isChangedInsuranceDetails = false, isChangedEstablishersTrustees = false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers)
      helper.declarationEnabled(userAnswers) mustBe false
    }
  }


}

object HsTaskListHelperVariationsSpec extends SpecBase {
  private lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  private lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  private lazy val workingKnowledgeLinkText = messages("messages__schemeTaskList__working_knowledge_link_text")
  private lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  private lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
  private lazy val companyLinkText = messages("messages__schemeTaskList__company_link")
  private lazy val individualLinkText = messages("messages__schemeTaskList__individual_link")
  private lazy val partnershipLinkText = messages("messages__schemeTaskList__partnership_link")
  private lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")
  private lazy val changeTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_change_link")
  private lazy val declarationLinkText = messages("messages__schemeTaskList__declaration_link")

  private def beforeYouStartLink(link: String) = {
    Link(
      messages(beforeYouStartLinkText),
      link
    )
  }

  private def answersData(isCompleteBeforeStart: Boolean = true,
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
                  _.set(IsTrusteeCompleteId(0))(isCompleteTrustees)).flatMap(
                  _.set(InsuranceDetailsChangedId)(isChangedInsuranceDetails)).flatMap(
                  _.set(InsuranceDetailsChangedId)(isChangedEstablishersTrustees))
              )
            )
          )
        )
      )
    )
  }

}