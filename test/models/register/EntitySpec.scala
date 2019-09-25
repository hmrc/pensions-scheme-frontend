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

package models.register

import identifiers.register.establishers.EstablisherKindId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.partnership.{PartnershipDetailsId => TrusteePartnershipDetailsId}
import models.register.SchemeType.SingleTrust
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import models.{NormalMode, UpdateMode}
import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class EntitySpec extends WordSpecLike with MustMatchers with OptionValues {

  "DirectorEntity" must {
    val directorEntity = DirectorEntity(
      DirectorNameId(establisherIndex = 0, directorIndex = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1
    )

    "have correct director index" in {
      directorEntity.index mustEqual 1
    }

    "have correct edit link when the director is incomplete" in {
      val expectedEditLink = controllers.register.establishers.company.director.routes.DirectorNameController.onPageLoad(NormalMode, 0, 1, None).url
      directorEntity.editLink(NormalMode, None)  mustBe Some(expectedEditLink)
    }

    "have correct edit link when the director is completed" in {
      val completedDirectorEntity = DirectorEntity(
        DirectorNameId(establisherIndex = 0, directorIndex = 0),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = true,
        1
      )

      val expectedEditLink =
        controllers.register.establishers.company.director.routes.CheckYourAnswersController.onPageLoad(
          establisherIndex = 0, directorIndex = 0, NormalMode, None).url
      completedDirectorEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(0, 1, NormalMode, None).url
      directorEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "EstablisherPartnershipEntity" must {
    val partnershipEntity = EstablisherPartnershipEntity(
      PartnershipDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1
    )

    "have correct company index" in {
      partnershipEntity.index mustEqual 1
    }

    "have correct edit link when company is incomplete" in {
      val expectedEditLink = controllers.register.establishers.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 1, None).url
      partnershipEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have cya link when company is completed and new" in {
      val completedCompanyEntity = EstablisherPartnershipEntity(
        PartnershipDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = true,
        1
      )
      val expectedEditLink = controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, 1, None).url
      completedCompanyEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have review link when company is completed and not new" in {
      val completedPartnershipEntity = EstablisherPartnershipEntity(
        PartnershipDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = false,
        1
      )
      val expectedEditLink = controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, 1, None).url
      completedPartnershipEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have review link when company is not completed and not new" in {
      val completedPartnershipEntity = EstablisherPartnershipEntity(
        PartnershipDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = false,
        isNewEntity = false,
        1
      )
      val expectedEditLink = controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(NormalMode, 1, None).url
      completedPartnershipEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(NormalMode, 1, EstablisherKind.Partnership, None).url
      partnershipEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "EstablisherCompanyEntity" must {
    val companyEntity = EstablisherCompanyEntity(
      EstablisherCompanyDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1
    )

    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have correct edit link when company is incomplete" in {
      val expectedEditLink = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, 1).url
      companyEntity.editLink(NormalMode, None) mustBe None
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(NormalMode, 1, EstablisherKind.Company, None).url
      companyEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "EstablisherIndividualEntity" must {
    val individualEntity = EstablisherIndividualEntityNonHnS(
      EstablisherDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1
    )

    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "have correct edit link when individual is incomplete" in {
      val expectedEditLink = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 1, None).url
      individualEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have cya link when individual is complete and new" in {
      val completeIndividualEntity = EstablisherIndividualEntityNonHnS(
        EstablisherDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = true,
        1
      )

      val expectedEditLink = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have reviw when individual is complete and not new" in {
      val completeIndividualEntity = EstablisherIndividualEntityNonHnS(
        EstablisherDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = false,
        1
      )

      val expectedEditLink = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have cya when individual is complete and not new" in {
      val completeIndividualEntity = EstablisherIndividualEntityNonHnS(
        EstablisherDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true,
        isNewEntity = false,
        1
      )

      val expectedEditLink = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have cya when individual is not complete and not new" in {
      val completeIndividualEntity = EstablisherIndividualEntityNonHnS(
        EstablisherDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = false,
        isNewEntity = false,
        1
      )

      val expectedEditLink = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustBe Some(expectedEditLink)
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(NormalMode, 1, EstablisherKind.Indivdual, None).url
      individualEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "EstablisherSkeletonEntity" must {
    val skeletonEntity = EstablisherSkeletonEntity(
      EstablisherKindId(1)
    )

    "have correct index" in {
      skeletonEntity.index mustBe 1
    }

    "have isDeleted flag set to true" in {
      skeletonEntity.isDeleted mustBe true
    }

    "have isCompleted flag set to false" in {
      skeletonEntity.isCompleted mustBe false
    }
  }

  "TrusteePartnershipEntity" must {
    val partnershipEntity = TrusteePartnershipEntity(
      TrusteePartnershipDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1,
      Some(SingleTrust.toString)
    )

    "have correct company index" in {
      partnershipEntity.index mustEqual 1
    }

    "have no edit link" in {
      val expectedEditLink = controllers.register.trustees.partnership.routes.PartnershipDetailsController.onPageLoad(NormalMode, 1, None).url
      partnershipEntity.editLink(NormalMode, None) mustBe None
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Partnership, None).url
      partnershipEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }

    "dont have delete link with update mode" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Partnership, None).url
      partnershipEntity.deleteLink(UpdateMode, None) mustBe None
    }

    "have delete link with update mode" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(UpdateMode, 1, TrusteeKind.Partnership, None).url
      partnershipEntity.copy(noOfRecords = 2).deleteLink(UpdateMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "TrusteeCompanyEntity" must {
    val companyEntity = TrusteeCompanyEntity(
      CompanyDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1,
      Some(SingleTrust.toString)
    )

    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have no edit link" in {
      val expectedEditLink = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1, None).url
      companyEntity.editLink(NormalMode, None) mustBe None
    }



    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Company, None).url
      companyEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }

    "dont have delete link with update mode" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Company, None).url
      companyEntity.deleteLink(UpdateMode, None) mustBe None
    }

    "have delete link with update mode" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(UpdateMode, 1, TrusteeKind.Company, None).url
      companyEntity.copy(noOfRecords = 2).deleteLink(UpdateMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "TrusteeIndividualEntity" must {
    val individualEntity = TrusteeIndividualEntity(
      TrusteeNameId(trusteeIndex = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false,
      isNewEntity = true,
      1,
      Some(SingleTrust.toString)
    )

    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "not have edit link" in {
      individualEntity.editLink(NormalMode, None) mustBe None
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Individual, None).url
      individualEntity.deleteLink(NormalMode, None) mustBe Some(expectedDeleteLink)
    }
  }

  "TrusteeSkeletonEntity" must {
    val skeletonTrustee = TrusteeSkeletonEntity(
      TrusteeKindId(1)
    )

    "have correct index" in {
      skeletonTrustee.index mustBe 1
    }

    "have isDeleted flag set to true" in {
      skeletonTrustee.isDeleted mustBe true
    }

    "have isCompleted flag set to false" in {
      skeletonTrustee.isCompleted mustBe false
    }

  }

}
