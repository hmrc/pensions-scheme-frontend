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
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.TrusteeKindId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.NormalMode
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class EntitySpec extends WordSpecLike with MustMatchers with OptionValues {

  "DirectorEntity" must {
    val directorEntity = DirectorEntity(
      DirectorDetailsId(establisherIndex = 0, directorIndex = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false
    )

    "have correct director index" in {
      directorEntity.index mustEqual 1
    }

    "have correct edit link when the director is incomplete" in {
      val expectedEditLink = controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, 0, 1, None).url
      directorEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct edit link when the director is completed" in {
      val completedDirectorEntity = DirectorEntity(
        DirectorDetailsId(establisherIndex = 0, directorIndex = 0),
        name = "test name",
        isDeleted = false,
        isCompleted = true
      )

      val expectedEditLink =
        controllers.register.establishers.company.director.routes.CheckYourAnswersController.onPageLoad(
          establisherIndex = 0, directorIndex = 0, NormalMode, None).url
      completedDirectorEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(0, 1, NormalMode, None).url
      directorEntity.deleteLink(NormalMode, None) mustEqual expectedDeleteLink
    }
  }

  "EstablisherCompanyEntity" must {
    val companyEntity = EstablisherCompanyEntity(
      EstablisherCompanyDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false
    )

    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have correct edit link when company is incomplete" in {
      val expectedEditLink = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, None, 1).url
      companyEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct edit link when company is completed" in {
      val completedCompanyEntity = EstablisherCompanyEntity(
        EstablisherCompanyDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true
      )
      val expectedEditLink = controllers.register.establishers.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, None, 1).url
      completedCompanyEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(NormalMode, 1, EstablisherKind.Company, None).url
      companyEntity.deleteLink(NormalMode, None) mustEqual expectedDeleteLink
    }
  }

  "EstablisherIndividualEntity" must {
    val individualEntity = EstablisherIndividualEntity(
      EstablisherDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false
    )

    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "have correct edit link when individual is incomplete" in {
      val expectedEditLink = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 1, None).url
      individualEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct edit link when individual is complete" in {
      val completeIndividualEntity = EstablisherIndividualEntity(
        EstablisherDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true
      )

      val expectedEditLink = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(NormalMode, 1, EstablisherKind.Indivdual, None).url
      individualEntity.deleteLink(NormalMode, None) mustEqual expectedDeleteLink
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

  "TrusteeCompanyEntity" must {
    val companyEntity = TrusteeCompanyEntity(
      CompanyDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false
    )

    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have correct edit link when company is incomplete" in {
      val expectedEditLink = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1, None).url
      companyEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct edit link when company is complete" in {
      val completeCompanyEntity = TrusteeCompanyEntity(
        CompanyDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true
      )

      val expectedEditLink = controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeCompanyEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Company, None).url
      companyEntity.deleteLink(NormalMode, None) mustEqual expectedDeleteLink
    }
  }

  "TrusteeIndividualEntity" must {
    val individualEntity = TrusteeIndividualEntity(
      TrusteeDetailsId(index = 1),
      name = "test name",
      isDeleted = false,
      isCompleted = false)

    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "have correct edit link when individual is incomplete" in {
      val expectedEditLink = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 1, None).url
      individualEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct edit link when individual is complete" in {
      val completeIndividualEntity = TrusteeIndividualEntity(
        TrusteeDetailsId(index = 1),
        name = "test name",
        isDeleted = false,
        isCompleted = true
      )

      val expectedEditLink = controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(NormalMode, 1, None).url
      completeIndividualEntity.editLink(NormalMode, None) mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(NormalMode, 1, TrusteeKind.Individual, None).url
      individualEntity.deleteLink(NormalMode, None) mustEqual expectedDeleteLink
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
