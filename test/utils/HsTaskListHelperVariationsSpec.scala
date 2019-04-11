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
import identifiers._
import identifiers.register.establishers.IsEstablisherCompleteId
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.trustees.IsTrusteeCompleteId
import identifiers.register.trustees.individual.TrusteeDetailsId
import models.person.PersonDetails
import models.{Link, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.libs.json.JsResult
import utils.behaviours.HsTaskListHelperBehaviour
import viewmodels.SchemeDetailsTaskListSection

class HsTaskListHelperVariationsSpec extends HsTaskListHelperBehaviour {
  "h1" must {
    "have the name of the scheme" in {
      val name = "scheme name 1"
      val userAnswers = UserAnswers().set(SchemeNameId)(name).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.taskList.h1 mustBe name
    }
  }

  "h2" must {
    "display Scheme details" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.taskList.h2 mustBe messages("messages__scheme_details__title")
    }
  }

  "page title" must {
    "display Scheme details" in {
      val userAnswers = UserAnswers()
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.taskList.pageTitle mustBe messages("messages__scheme_details__title")
    }
  }

  "beforeYouStartSection " must {
    behave like beforeYouStartSection()
  }

  "aboutSection " must {

    "return the the Seq of members and benefits section with " +
      "links of the cya pages of individual sub sections " in {
      val userAnswers = UserAnswers().set(IsAboutMembersCompleteId)(true).flatMap(
          _.set(IsAboutBenefitsAndInsuranceCompleteId)(true)
      ).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.aboutSection(userAnswers) mustBe
        Seq(
          SchemeDetailsTaskListSection(Some(true), Link(aboutMembersLinkText,
            controllers.routes.CheckYourAnswersMembersController.onPageLoad(UpdateMode, Some("test srn")).url), None),
          SchemeDetailsTaskListSection(Some(true), Link(aboutBenefitsAndInsuranceLinkText,
            controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(UpdateMode, Some("test srn")).url), None)
        )
    }
  }

  "workingKnowledgeSection " must {
    "not display when do you have working knowledge is false " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(false).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.taskList.workingKnowledge mustBe None
    }

    "not display when do you have working knowledge is true " in {
      val userAnswers = UserAnswers().set(DeclarationDutiesId)(true).asOpt.value
      val helper = new HsTaskListHelperVariations(userAnswers, "test srn")
      helper.taskList.workingKnowledge mustBe None
    }
  }

  "addEstablisherHeader " must {

    behave like addEstablisherHeader(UpdateMode, Some("test-srn"))
  }

  "addTrusteeHeader " must {

    behave like addTrusteeHeader(UpdateMode, Some("test-srn"))
  }

  "establishers" must {

    behave like establishersSection(UpdateMode, Some("test-srn"))
  }

  "trustees" must {

    behave like trusteesSection(UpdateMode, Some("test-srn"))
  }

  "declarationEnabled" must {

    behave like declarationEnabled()
  }

  "declarationLink" must {

    behave like declarationLink()
  }
}

object HsTaskListHelperVariationsSpec extends SpecBase {

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

