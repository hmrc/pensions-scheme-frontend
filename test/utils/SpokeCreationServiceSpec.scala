/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.register.establishers.company.director.{routes => establisherCompanyDirectorRoutes}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.establishers.individual.{routes => establisherIndividualRoutes}
import controllers.register.establishers.partnership.partner.{routes => establisherPartnershipPartnerRoutes}
import controllers.register.establishers.partnership.{routes => establisherPartnershipRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import helpers.DataCompletionHelper
import identifiers.SchemeNameId
import models.address.Address
import models.person.PersonName
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import models.register.{DeclarationDormant, SchemeType}
import models.{EntitySpoke, Mode, NormalMode, _}
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpec
import utils.hstasklisthelper.SpokeCreationService
import viewmodels.Message

class SpokeCreationServiceSpec
  extends AnyWordSpec
    with Matchers
    with OptionValues
    with DataCompletionHelper {

  import SpokeCreationServiceSpec._

  val spokeCreationService = new SpokeCreationService()

  "getBeforeYouStartSpoke" when {
    "in subscription" must {
      "display the spoke with link to scheme name page with in progress status if the spoke is in progress" in {
        val userAnswers = userAnswersWithSchemeName
        val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__before_you_start_link_text", schemeName),
          controllers.routes.SchemeNameController.onPageLoad(srn).url), Some(false)))

        val result = spokeCreationService.getBeforeYouStartSpoke(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display the spoke with link to cya page with complete status if the spoke is completed" in {
        val userAnswers = userAnswersWithSchemeName.schemeType(SchemeType.SingleTrust).declarationDuties(haveWorkingKnowledge = true).
          establishedCountry(country = "UK")
        val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__before_you_start_link_text", schemeName),
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(NormalMode, srn).url), Some(true)))

        val result = spokeCreationService.getBeforeYouStartSpoke(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display the spoke with link to scheme name page with in progress status if the spoke is in progress" in {
        val userAnswers = userAnswersWithSchemeName
        val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__scheme_info_link_text", schemeName),
          controllers.routes.SchemeNameController.onPageLoad(srn).url), Some(false)))

        val result = spokeCreationService.getBeforeYouStartSpoke(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display the spoke with link to cya page with no status if the spoke is completed" in {
        val userAnswers = userAnswersWithSchemeName.schemeType(SchemeType.SingleTrust).declarationDuties(haveWorkingKnowledge = true).
          establishedCountry(country = "UK")
        val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__scheme_info_link_text", schemeName),
          controllers.routes.CheckYourAnswersBeforeYouStartController.onPageLoad(UpdateMode, srn).url), None))

        val result = spokeCreationService.getBeforeYouStartSpoke(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getAboutSpoke" when {
    "in subscription" must {
      "display all the spokes with link to first page, incomplete status if the spoke is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName
        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text_add", schemeName),
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad(srn).url), Some(false)),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_add", schemeName),
            controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad(srn).url), Some(false)),
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with link to first page, incomplete status if the spoke is in progress" in {
        val userAnswers = userAnswersWithSchemeName.currentMembers(Members.One).occupationalPensionScheme(isOccupational = true).
          ukBankAccount(ukBankAccount = true)
        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text_continue", schemeName),
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad(srn).url), Some(false)),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_continue", schemeName),
            controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad(srn).url), Some(false)),
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with link to cya page, complete status if the spoke is completed" in {
        val userAnswers = setCompleteMembers(isComplete = true,
          setCompleteBank(isComplete = true,
            setCompleteBenefits(isComplete = true,
              userAnswersWithSchemeName)))

        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text", schemeName),
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(NormalMode, srn).url), Some(true)),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text", schemeName),
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(NormalMode, srn).url), Some(true)),
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display members and benefits and insurance spokes with link to first page, in progress status if the spoke is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName
        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text_view", schemeName),
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad(srn).url), Some(false)),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName),
            controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad(srn).url), Some(false))
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display members and benefits and insurance spokes with link to first page, in progress status if the spoke is in progress" in {
        val userAnswers = userAnswersWithSchemeName.currentMembers(Members.One).occupationalPensionScheme(isOccupational = true).
          ukBankAccount(ukBankAccount = true)
        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text_view", schemeName),
            controllers.routes.WhatYouWillNeedMembersController.onPageLoad(srn).url), Some(false)),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName),
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), Some(false))
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display members and benefits and insurance spokes with link to cya page, blank status if the spoke is completed" in {
        val userAnswers = setCompleteMembers(isComplete = true,
          setCompleteBank(isComplete = true,
            setCompleteBenefits(isComplete = true,
              userAnswersWithSchemeName)))

        val expectedSpoke = Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_members_link_text_view", schemeName),
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, srn).url), None),
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__about_benefits_and_insurance_link_text_view", schemeName),
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, srn).url), None)
        )

        val result = spokeCreationService.getAboutSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getWorkingKnowledgeSpoke in subscription" must {
    "display the spoke with link to wyn page with status as incomplete if the spoke is uninitiated" in {
      val userAnswers = userAnswersWithSchemeName
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details_wk"),
        controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad(srn).url), Some(false)))

      val result = spokeCreationService.getWorkingKnowledgeSpoke(userAnswers, NormalMode, srn, schemeName, None)
      result mustBe expectedSpoke
    }

    "display the spoke with link to wyn page with incomplete status if the spoke is in progress" in {
      val userAnswers = userAnswersWithSchemeName.adviserName(name = "test adviser")
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__continue_details", schemeName),
        controllers.routes.WhatYouWillNeedWorkingKnowledgeController.onPageLoad(srn).url), Some(false)))

      val result = spokeCreationService.getWorkingKnowledgeSpoke(userAnswers, NormalMode, srn, schemeName, None)
      result mustBe expectedSpoke
    }

    "display the spoke with link to cya page with completed status if the spoke is completed" in {
      val userAnswers = setCompleteWorkingKnowledge(isComplete = true, userAnswersWithSchemeName)
      val expectedSpoke = Seq(EntitySpoke(TaskListLink(Message("messages__schemeTaskList__change_details", schemeName),
        controllers.routes.AdviserCheckYourAnswersController.onPageLoad(srn).url), Some(true)))

      val result = spokeCreationService.getWorkingKnowledgeSpoke(userAnswers, NormalMode, srn, schemeName, None)
      result mustBe expectedSpoke
    }
  }

  "getEstablisherCompanySpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links, incomplete status when establisher company is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estCompanyAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, incomplete status when establisher company is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establisherCompanyDetails(index = 0, CompanyDetails("test company")).
          establisherCompanyDormant(0, DeclarationDormant.Yes).establishersCompanyAddress(index = 0, address).
          establishersCompanyEmail(index = 0, email = "s@s.com").establishersCompanyDirectorName(0, 0, PersonName("s", "l"))
          .establishersCompanyDirectorNino(0, 0, ReferenceValue("AB100100A"))
        val expectedSpoke = estCompanyInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, complete status when establisher company is completed" in {
        val userAnswers = setCompleteEstCompany(0, userAnswersWithSchemeName).isEstablisherNew(0, flag = true).
          establisherCompanyDormant(0, DeclarationDormant.Yes)
        val expectedSpoke = estCompanyCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display all the spokes with appropriate links when establisher company is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estCompanyAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher company is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establisherCompanyDetails(index = 0, CompanyDetails("test company")).
          establisherCompanyNoUtrReason(0, "no utr").establishersCompanyAddress(index = 0, address).
          establishersCompanyEmail(index = 0, email = "s@s.com").establishersCompanyDirectorName(0, 0, PersonName("s", "l"))
          .establishersCompanyDirectorNino(0, 0, ReferenceValue("AB100100A"))

        val expectedSpoke = estCompanyInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher company is completed" in {
        val userAnswers = setCompleteEstCompany(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estCompanyCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher company is not new" in {
        val userAnswers = setCompleteEstCompany(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = false)
        val expectedSpoke = estCompanyCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getEstablisherPartnershipSpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links, incomplete status when establisher partnership is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estPartnershipAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, incomplete status when establisher partnership is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establisherPartnershipDetails(index = 0, PartnershipDetails("test partnership")).
          establisherPartnershipHasVat(0, hasVat = false).establisherPartnershipAddress(index = 0, address).
          establishersPartnershipEmail(index = 0, email = "s@s.com").establishersPartnershipPartnerName(0, 0, PersonName("s", "l"))
          .establishersPartnershipPartnerNino(0, 0, ReferenceValue("AB100100A"))
        val expectedSpoke = estPartnershipInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, complete status when establisher partnership is completed" in {
        val userAnswers = setCompleteEstPartnership(0, userAnswersWithSchemeName).isEstablisherNew(0, flag = true)
        val expectedSpoke = estPartnershipCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

    }

    "in variation" must {
      "display all the spokes with appropriate links when establisher partnership is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estPartnershipAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher partnership is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establisherPartnershipDetails(index = 0, PartnershipDetails("test partnership")).
          establisherPartnershipHasVat(0, hasVat = false).establisherPartnershipAddress(index = 0, address).
          establishersPartnershipEmail(index = 0, email = "s@s.com").establishersPartnershipPartnerName(0, 0, PersonName("s", "l"))
          .establishersPartnershipPartnerNino(0, 0, ReferenceValue("AB100100A"))

        val expectedSpoke = estPartnershipInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher partnership is completed" in {
        val userAnswers = setCompleteEstPartnership(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estPartnershipCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher partnership is completed and only one partner" in {
        val userAnswers = setCompleteEstPartnershipOnePartner(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = true)
        val expectedSpoke =
          estPartnershipCompleteSpokeWithoutPartners(UpdateMode, srn, linkText = "view", status = None) ++ Seq(
            EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__view_partners", schemeName),
              establisherPartnershipRoutes.AddPartnersController.onPageLoad(UpdateMode, index = 0, srn).url), Some(false))
          )

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher partnership is not new" in {
        val userAnswers = setCompleteEstPartnership(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = false)
        val expectedSpoke = estPartnershipCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherPartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getEstablisherIndividualSpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links, blank status when establisher individual is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estIndividualAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, incomplete status when establisher individual is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establishersIndividualName(index = 0, PersonName("s", "l")).
          establishersIndividualNino(0, ReferenceValue("AB100100A")).
          establishersIndividualAddress(index = 0, address).
          establishersIndividualEmail(index = 0, email = "s@s.com")
        val expectedSpoke = estIndividualInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, complete status when establisher individual is completed" in {
        val userAnswers = setCompleteEstIndividual(0, userAnswersWithSchemeName).isEstablisherNew(0, flag = true)
        val expectedSpoke = estIndividualCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display all the spokes with appropriate links when establisher individual is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estIndividualAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher individual is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isEstablisherNew(index = 0, flag = true).
          establishersIndividualName(index = 0, PersonName("s", "l")).
          establishersIndividualNino(0, ReferenceValue("AB100100A")).
          establishersIndividualAddress(index = 0, address).
          establishersIndividualEmail(index = 0, email = "s@s.com")

        val expectedSpoke = estIndividualInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher individual is completed" in {
        val userAnswers = setCompleteEstIndividual(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = true)
        val expectedSpoke = estIndividualCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when establisher individual is not new" in {
        val userAnswers = setCompleteEstIndividual(0, userAnswersWithSchemeName).isEstablisherNew(index = 0, flag = false)
        val expectedSpoke = estIndividualCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getEstablisherIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getTrusteeCompanySpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links, blank status when trustee company is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeCompanyAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, in progress status when trustee company is incomplete" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteesCompanyDetails(index = 0, CompanyDetails("test company")).
          trusteesCompanyHasUTR(0, hasUtr = true).
          trusteesCompanyEnterUTR(0, ReferenceValue("test-utr")).
          trusteesCompanyAddress(index = 0, address).
          trusteeCompanyEmail(index = 0, email = "s@s.com")
        val expectedSpoke = trusteeCompanyInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, complete status when trustee company is completed" in {
        val userAnswers = setCompleteTrusteeCompany(0, userAnswersWithSchemeName).isTrusteeNew(0, flag = true)
        val expectedSpoke = trusteeCompanyCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display all the spokes with appropriate links when trustee company is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeCompanyAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee company is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteesCompanyDetails(index = 0, CompanyDetails("test company")).
          trusteesCompanyEnterUTR(0, ReferenceValue("1231231231")).
          trusteesCompanyAddress(index = 0, address).
          trusteeCompanyEmail(index = 0, email = "s@s.com")

        val expectedSpoke = trusteeCompanyInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee company is completed" in {
        val userAnswers = setCompleteTrusteeCompany(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeCompanyCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee company is not new" in {
        val userAnswers = setCompleteTrusteeCompany(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = false)
        val expectedSpoke = trusteeCompanyCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteeCompanySpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getTrusteePartnershipSpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links, blank status when trustee partnership is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteePartnershipAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, incomplete status when trustee partnership is incomplete" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteePartnershipDetails(index = 0, PartnershipDetails("test partnership")).
          trusteesPartnershipHasVAT(0, hasVat = false).trusteePartnershipAddress(index = 0, address).
          trusteePartnershipEmail(index = 0, email = "s@s.com")
        val expectedSpoke = trusteePartnershipInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links, complete status when trustee partnership is completed" in {
        val userAnswers = setCompleteTrusteePartnership(0, userAnswersWithSchemeName).isTrusteeNew(0, flag = true)
        val expectedSpoke = trusteePartnershipCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display all the spokes with appropriate links when trustee partnership is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteePartnershipAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee partnership is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteePartnershipDetails(index = 0, PartnershipDetails("test partnership")).
          trusteesPartnershipHasVAT(0, hasVat = false).trusteePartnershipAddress(index = 0, address).
          trusteePartnershipEmail(index = 0, email = "s@s.com")

        val expectedSpoke = trusteePartnershipInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee partnership is completed" in {
        val userAnswers = setCompleteTrusteePartnership(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteePartnershipCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee partnership is not new" in {
        val userAnswers = setCompleteTrusteePartnership(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = false)
        val expectedSpoke = trusteePartnershipCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteePartnershipSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getTrusteeIndividualSpokes" when {
    "in subscription" must {
      "display all the spokes with appropriate links when trustee individual is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeIndividualAddSpokes(NormalMode, srn, Some(false))

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee individual is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteeName(index = 0, PersonName("s", "l")).
          trusteeIndividualNino(0, ReferenceValue("AB100100A")).
          trusteesAddress(index = 0, address).
          trusteeEmail(index = 0, email = "s@s.com")
        val expectedSpoke = trusteeIndividualInProgressSpoke(NormalMode, srn = srn, linkText = "continue", status = Some(false))

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee individual is completed" in {
        val userAnswers = setCompleteTrusteeIndividual(0, userAnswersWithSchemeName).isTrusteeNew(0, flag = true)
        val expectedSpoke = trusteeIndividualCompleteSpoke(NormalMode, srn = srn, linkText = "change", status = Some(true))

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, NormalMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }

    "in variation" must {
      "display all the spokes with appropriate links when trustee individual is uninitiated" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeIndividualAddSpokes(UpdateMode, srn, Some(false))

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee individual is in progress" in {
        val userAnswers = userAnswersWithSchemeName.isTrusteeNew(index = 0, flag = true).
          trusteeName(index = 0, PersonName("s", "l")).
          trusteeIndividualNino(index = 0, ReferenceValue("AB100100A")).
          trusteesAddress(index = 0, address).
          trusteeEmail(index = 0, email = "s@s.com")

        val expectedSpoke = trusteeIndividualInProgressSpoke(UpdateMode, srn, linkText = "view", status = Some(false))

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee individual is completed" in {
        val userAnswers = setCompleteTrusteeIndividual(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = true)
        val expectedSpoke = trusteeIndividualCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }

      "display all the spokes with appropriate links when trustee individual is not new" in {
        val userAnswers = setCompleteTrusteeIndividual(0, userAnswersWithSchemeName).isTrusteeNew(index = 0, flag = false)
        val expectedSpoke = trusteeIndividualCompleteSpoke(UpdateMode, srn, linkText = "view", status = None)

        val result = spokeCreationService.getTrusteeIndividualSpokes(userAnswers, UpdateMode, srn, schemeName, None)
        result mustBe expectedSpoke
      }
    }
  }

  "getAddEstablisherHeaderSpokesWithToggleOff" must {
    "return no spokes when no establishers and view only" in {
      val result = spokeCreationService.getAddEstablisherHeaderSpokesToggleOff(userAnswersWithSchemeName, NormalMode, srn, viewOnly = true)
      result mustBe Nil
    }

    "return all the spokes with appropriate links when no establishers and NOT view only" in {
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link_toggleOff"),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(UpdateMode, 0, srn).url), None)
        )

      val result = spokeCreationService.getAddEstablisherHeaderSpokesToggleOff(userAnswersWithSchemeName, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when establishers and NOT view only and srn is defined" in {
      val userAnswers = userAnswersWithSchemeName
        .establisherKind(0, EstablisherKind.Indivdual)
        .establishersIndividualName(0, personName)

      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_view_link"),
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(UpdateMode, srn).url), None)
        )

      val result = spokeCreationService.getAddEstablisherHeaderSpokesToggleOff(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when establishers and NOT view only and no srn is defined" in {
      val userAnswers = userAnswersWithSchemeName
        .establisherKind(0, EstablisherKind.Indivdual)
        .establishersIndividualName(0, personName)

      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionEstablishers_change_link_toggleOff"),
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, srn).url),
          None
        ))

      val result = spokeCreationService.getAddEstablisherHeaderSpokesToggleOff(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }
  }

  "getAddEstablisherHeaderSpokes (with toggle on)" must {
    "return no spokes when no establishers and view only" in {
      val result = spokeCreationService.getAddEstablisherHeaderSpokes(userAnswersWithSchemeName, NormalMode, srn, viewOnly = true)
      result mustBe Nil
    }

    "return all the spokes with appropriate links when no establishers and NOT view only" in {
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionEstablishers_add_link", schemeName),
            controllers.register.establishers.routes.EstablisherKindController.onPageLoad(UpdateMode, 0, srn).url), Some(false))
        )

      val result = spokeCreationService.getAddEstablisherHeaderSpokes(userAnswersWithSchemeName, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when establishers and NOT view only and no srn is defined" in {
      val userAnswers = userAnswersWithSchemeName
        .establisherKind(0, EstablisherKind.Indivdual)
        .establishersIndividualName(0, personName)

      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionEstablishers_continue_link", schemeName),
            controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, srn).url),
          Some(false)
        ))

      val result = spokeCreationService.getAddEstablisherHeaderSpokes(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }
  }

  "getAddTrusteeHeaderSpokes" must {
    "return all the spokes with appropriate links when have any trustees flag has no value AND there are trustees AND NOT view only and there is an srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
      val expectedSpoke =
        Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url), None)
        )

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND" +
      " there are trustees AND NOT view only and there is an srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url), None)
        )

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has no value AND" +
      " there are trustees AND NOT view only and there is no srn AND spoke is incomplete" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionTrustees_continue_link", schemeName),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, srn).url), Some(false)
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND" +
      " there are trustees AND NOT view only and there is no srn AND spoke is incomplete" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionTrustees_continue_link", schemeName),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, srn).url), Some(false)
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has no value AND there are NO trustees AND NOT view only" in {
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionTrustees_add_link", schemeName),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, 0, srn).url), Some(false)
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswersWithSchemeName, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND there are NO trustees AND NOT view only" in {
      val userAnswers = userAnswersWithSchemeName
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionTrustees_add_link", schemeName),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, 0, srn).url), Some(false)
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return no spokes when no trustees and view only" in {
      val result = spokeCreationService.getAddTrusteeHeaderSpokes(userAnswersWithSchemeName, NormalMode, srn, viewOnly = true)
      result mustBe Nil
    }

  }

  "getAddTrusteeHeaderSpokesToggleOff" must {
    "return all the spokes with appropriate links when have any trustees flag has no value AND there are trustees AND NOT view only and there is an srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
      val expectedSpoke =
        Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url), None)
        )

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND" +
      " there are trustees AND NOT view only and there is an srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(
          EntitySpoke(TaskListLink(Message("messages__schemeTaskList__sectionTrustees_view_link"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(UpdateMode, srn).url), None)
        )

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has no value AND there are trustees AND NOT view only and there is no srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link_toggleOff"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, srn).url),
          None
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND" +
      " there are trustees AND NOT view only and there is no srn" in {
      val userAnswers = userAnswersWithSchemeName
        .trusteeKind(0, TrusteeKind.Individual)
        .trusteeName(0, personName)
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(Message("messages__schemeTaskList__sectionTrustees_change_link_toggleOff"),
            controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, srn).url),
          None
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswers, NormalMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has no value AND there are NO trustees AND NOT view only" in {
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionTrustees_add_link_toggleOff"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, 0, srn).url),
          None
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswersWithSchemeName, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return all the spokes with appropriate links when have any trustees flag has value of true AND there are NO trustees AND NOT view only" in {
      val userAnswers = userAnswersWithSchemeName
        .haveAnyTrustees(true)
      val expectedSpoke =
        Seq(EntitySpoke(
          TaskListLink(
            Message("messages__schemeTaskList__sectionTrustees_add_link_toggleOff"),
            controllers.register.trustees.routes.TrusteeKindController.onPageLoad(UpdateMode, 0, srn).url),
          None
        ))

      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswers, UpdateMode, srn, viewOnly = false)
      result mustBe expectedSpoke
    }

    "return no spokes when no trustees and view only" in {
      val result = spokeCreationService.getAddTrusteeHeaderSpokesToggleOff(userAnswersWithSchemeName, NormalMode, srn, viewOnly = true)
      result mustBe Nil
    }

  }
}


object SpokeCreationServiceSpec extends OptionValues with DataCompletionHelper {

  private val personName = PersonName("First", "Last")
  private val srn = SchemeReferenceNumber("S123")
  private val schemeName = "scheme"
  private val userAnswersWithSchemeName: UserAnswers = UserAnswers().set(SchemeNameId)(schemeName).asOpt.value
  private val address = Address("line1", "line2", None, None, None, "GB")

  private def estCompanyAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_directors", schemeName),
      establisherCompanyDirectorRoutes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, establisherIndex = 0).url), status)
  )

  private def estCompanyInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_directors", schemeName),
      establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index = 0).url), status)
  )

  private def estCompanyCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_directors", schemeName),
      establisherCompanyRoutes.AddCompanyDirectorsController.onPageLoad(mode, srn, index = 0).url), status)
  )

  private def estPartnershipAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_partners", schemeName),
      establisherPartnershipPartnerRoutes.WhatYouWillNeedPartnerController.onPageLoad(mode, 0, srn).url), status)
  )

  private def estPartnershipInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, index = 0).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_partners", schemeName),
      establisherPartnershipRoutes.AddPartnersController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def estPartnershipCompleteSpokeWithoutPartners(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def estPartnershipCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = {
    estPartnershipCompleteSpokeWithoutPartners(mode, srn, linkText, status) ++ Seq(
      EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_partners", schemeName),
        establisherPartnershipRoutes.AddPartnersController.onPageLoad(mode, index = 0, srn).url), status)
    )
  }

  private def estIndividualAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def estIndividualInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def estIndividualCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      establisherIndividualRoutes.CheckYourAnswersDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      establisherIndividualRoutes.CheckYourAnswersAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      establisherIndividualRoutes.CheckYourAnswersContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeCompanyAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeCompanyInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeCompanyCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteePartnershipAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteePartnershipInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteePartnershipCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeIndividualAddSpokes(mode: Mode, srn: SchemeReferenceNumber, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_details", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_address", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message("messages__schemeTaskList__add_contact", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeIndividualInProgressSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )

  private def trusteeIndividualCompleteSpoke(mode: Mode, srn: SchemeReferenceNumber, linkText: String, status: Option[Boolean]) = Seq(
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_details", schemeName),
      trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_address", schemeName),
      trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, index = 0, srn).url), status),
    EntitySpoke(TaskListLink(Message(s"messages__schemeTaskList__${linkText}_contact", schemeName),
      trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, index = 0, srn).url), status)
  )
}
