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
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import identifiers.register.establishers.company.{CompanyEmailId, CompanyPhoneId}
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.establishers.company.CompanyVatId
import models.person.PersonDetails
import models._
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues}
import utils.hstasklisthelper.{HsTaskListHelper, HsTaskListHelperRegistration, HsTaskListHelperVariations}

class HsTaskListHelperUtilsSpec extends SpecBase with MustMatchers with OptionValues {
  import HsTaskListHelperUtilsSpec._

  "getEstablisherCompanySpokes" must {
    "display all spokes with appropriate links" when {
      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyBlank, NormalMode, None, "test company", 0
        ) mustBe expectedAddSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompany(isComplete = false), NormalMode, None, "test company", 0
        ) mustBe expectedInProgressSpokes(NormalMode, None)
      }

      "in subscription journey with partial data" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyWithPartialData, NormalMode, None, "test company", 0
        ) mustBe expectedSpokesWithPartialData(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyWithCompletedDirectors(isComplete = true), NormalMode, None, "test company", 0
        ) mustBe expectedCompletedSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompany(isComplete = false), UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompany(isComplete = true), UpdateMode, srn, "test company", 0
        ) mustBe expectedCompletedSpokes(UpdateMode, srn)
      }
    }
  }

}

object HsTaskListHelperUtilsSpec extends SpecBase with OptionValues {

  val srn = Some("S123")
  private val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(true)

  protected def establisherCompanyBlank: UserAnswers = {
    UserAnswers().set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company", false)).flatMap(
      _.set(IsEstablisherNewId(0))(true)
    )
      .asOpt.value
  }

  protected def establisherCompany(isComplete: Boolean): UserAnswers = {
    establisherCompanyBlank
      .set(IsEstablisherNewId(0))(true).flatMap(
        _.set(establisherCompanyPath.IsAddressCompleteId(0))(isComplete).flatMap(
          _.set(establisherCompanyPath.IsDetailsCompleteId(0))(isComplete).flatMap(
            _.set(establisherCompanyPath.IsContactDetailsCompleteId(0))(isComplete).flatMap(
            _.set(DirectorDetailsId(0, 0))(PersonDetails("Joe", None, "Bloggs", LocalDate.now()))
          )))).asOpt.value
  }

  protected def establisherCompanyWithPartialData: UserAnswers = {
    establisherCompanyBlank
      .set(CompanyVatId(0))(Vat.Yes("test-vat")).flatMap(
      _.set(establisherCompanyPath.IsAddressCompleteId(0))(false).flatMap(
        _.set(CompanyEmailId(0))("test@test.com").flatMap(
          _.set(CompanyPhoneId(0))("1234")
        )
      )
    ).asOpt.value
  }

  protected def establisherCompanyWithCompletedDirectors(isComplete: Boolean) = establisherCompany(isComplete)
    .set(IsDirectorCompleteId(0, 0))(true).asOpt.value

  def modeBasedCompletion(mode: Mode, completion: Option[Boolean]): Option[Boolean] = if(mode == NormalMode) completion else None

  def subscriptionHelper: HsTaskListHelper = new HsTaskListHelperRegistration(UserAnswers(), fakeFeatureSwitch)
  def variationsHelper(viewOnly: Boolean = false): HsTaskListHelper = new HsTaskListHelperVariations(UserAnswers(), viewOnly, srn, fakeFeatureSwitch)

  def expectedAddSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), None)
  )

  def expectedInProgressSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedSpokesWithPartialData(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), None)
  )

  def expectedCompletedSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true)))
  )

}