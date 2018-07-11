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

package models.register

import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.company.{CompanyDetailsId => EstablisherCompanyDetailsId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.company.CompanyDetailsId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.NormalMode
import models.register.establishers.EstablisherKind
import models.register.trustees.TrusteeKind
import org.scalatest.{MustMatchers, OptionValues, WordSpecLike}

class EntitySpec extends WordSpecLike with MustMatchers with OptionValues {

  "DirectorEntity" must {
    "have correct director index" in {
      val directorEntity = DirectorEntity(DirectorDetailsId(establisherIndex = 0, directorIndex = 1), "test name", isDeleted = false, isCompleted = false)
      directorEntity.index mustEqual 1
    }

    "have correct edit link" in {
      val directorEntity = DirectorEntity(DirectorDetailsId(establisherIndex = 0, directorIndex = 0), "test name", isDeleted = false, isCompleted = false)
      val expectedEditLink = controllers.register.establishers.company.director.routes.DirectorDetailsController.onPageLoad(NormalMode, 0, 0).url
      directorEntity.editLink mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val directorEntity = DirectorEntity(DirectorDetailsId(establisherIndex = 0, directorIndex = 1), "test name", isDeleted = false, isCompleted = false)
      val expectedDeleteLink = controllers.register.establishers.company.director.routes.ConfirmDeleteDirectorController.onPageLoad(0, 1).url
      directorEntity.deleteLink mustEqual expectedDeleteLink
    }
  }

  "EstablisherCompanyEntity" must {
    val companyEntity = EstablisherCompanyEntity(EstablisherCompanyDetailsId(index = 1), "test name", isDeleted = false, isCompleted = false)
    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have correct edit link" in {
      val expectedEditLink = controllers.register.establishers.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1).url
      companyEntity.editLink mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(1, EstablisherKind.Company).url
      companyEntity.deleteLink mustEqual expectedDeleteLink
    }
  }

  "EstablisherIndividualEntity" must {
    val individualEntity = EstablisherIndividualEntity(EstablisherDetailsId(index = 1), "test name", isDeleted = false, isCompleted = false)
    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "have correct edit link" in {
      val expectedEditLink = controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(NormalMode, 1).url
      individualEntity.editLink mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.establishers.routes.ConfirmDeleteEstablisherController.onPageLoad(1, EstablisherKind.Indivdual).url
      individualEntity.deleteLink mustEqual expectedDeleteLink
    }
  }

  "TrusteeCompanyEntity" must {
    val companyEntity = TrusteeCompanyEntity(CompanyDetailsId(index = 1), "test name", isDeleted = false, isCompleted = false)
    "have correct company index" in {
      companyEntity.index mustEqual 1
    }

    "have correct edit link" in {
      val expectedEditLink = controllers.register.trustees.company.routes.CompanyDetailsController.onPageLoad(NormalMode, 1).url
      companyEntity.editLink mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(1, TrusteeKind.Company).url
      companyEntity.deleteLink mustEqual expectedDeleteLink
    }
  }

  "TrusteeIndividualEntity" must {
    val individualEntity = TrusteeIndividualEntity(TrusteeDetailsId(index = 1), "test name", isDeleted = false, isCompleted = false)
    "have correct individual index" in {
      individualEntity.index mustEqual 1
    }

    "have correct edit link" in {
      val expectedEditLink = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(NormalMode, 1).url
      individualEntity.editLink mustEqual expectedEditLink
    }

    "have correct delete link" in {
      val expectedDeleteLink = controllers.register.trustees.routes.ConfirmDeleteTrusteeController.onPageLoad(1, TrusteeKind.Individual).url
      individualEntity.deleteLink mustEqual expectedDeleteLink
    }
  }
}