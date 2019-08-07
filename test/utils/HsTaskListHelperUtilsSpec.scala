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
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteeCompanyPath}
import models.address.Address
import models._
import org.scalatest.{MustMatchers, OptionValues}
import utils.DataCompletionSpec.readJsonFromFile
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
          answersIncomplete, NormalMode, None, "test company", 0
        ) mustBe expectedInProgressSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          answersComplete, NormalMode, None, "test company", 0
        ) mustBe expectedCompletedSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          answersIncomplete, UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          answersComplete, UpdateMode, srn, "test company", 0
        ) mustBe expectedCompletedSpokes(UpdateMode, srn)
      }
    }
  }

  "getTrusteeCompanySpokes" must {
    "display all spokes with appropriate links" when {
      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompanyBlank, NormalMode, None, "test company", 0
        ) mustBe expectedAddTrusteeSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersIncomplete, NormalMode, None, "test company", 0
        ) mustBe expectedInProgressTrusteeSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersComplete, NormalMode, None, "test company", 0
        ) mustBe expectedCompletedTrusteeSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddTrusteeSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersIncomplete, UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressTrusteeSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersComplete, UpdateMode, srn, "test company", 0
        ) mustBe expectedCompletedTrusteeSpokes(UpdateMode, srn)
      }
    }
  }

}

object HsTaskListHelperUtilsSpec extends SpecBase with OptionValues {

  val srn = Some("S123")
  private val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(true)
  private val address = Address("line 1", "line 2", Some("line 3"), Some("line 4"), Some("zz11zz"), "GB")

  protected def establisherCompanyBlank: UserAnswers = {
    UserAnswers().set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsEstablisherNewId(0))(true)
    )
      .asOpt.value
  }

  protected def trusteeCompanyBlank: UserAnswers = {
    UserAnswers().set(trusteeCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def answersComplete = UserAnswers(readJsonFromFile("/payloadHnS.json"))
  protected def answersIncomplete = UserAnswers(readJsonFromFile("/payloadHnSInProgress.json"))

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
      controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, 0).url), None)
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

  def expectedAddTrusteeSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedInProgressTrusteeSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedCompletedTrusteeSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true)))
    )

}