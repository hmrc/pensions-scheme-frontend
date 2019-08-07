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
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.company.{CompanyEmailId, CompanyVatId}
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath}
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteeCompanyPath, individual => trusteeIndividualPath}
import models.address.Address
import models.person.{PersonDetails, PersonName}
import models.{CompanyDetails, EntitySpoke, Link, Mode, NormalMode, UpdateMode, _}
import org.joda.time.LocalDate
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
          establisherCompanyWithPartialData, NormalMode, None, "test company", 0
        ) mustBe expectedInProgressSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyWithCompletedDirectors, NormalMode, None, "test company", 0
        ) mustBe expectedCompletedSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyWithPartialData, UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherCompanySpokes(
          establisherCompanyWithCompletedDirectors, UpdateMode, srn, "test company", 0
        ) mustBe expectedCompletedSpokes(UpdateMode, srn)
      }
    }
  }

  "getTrusteeCompanySpokes" must {
    "display all spokes with appropriate links" when {
      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompanyBlank, NormalMode, None, "test company", 0
        ) mustBe expectedAddTrusteeCompanySpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompany(isComplete = false), NormalMode, None, "test company", 0
        ) mustBe expectedInProgressTrusteeCompanySpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompany(isComplete = true), NormalMode, None, "test company", 0
        ) mustBe expectedCompletedTrusteeCompanySpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddTrusteeCompanySpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompany(isComplete = false), UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressTrusteeCompanySpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompany(isComplete = true), UpdateMode, srn, "test company", 0
        ) mustBe expectedCompletedTrusteeCompanySpokes(UpdateMode, srn)
      }
    }
  }

  "getTrusteeIndividualSpokes" must {
    "display all spokes with appropriate links" when {

      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividualBlank, NormalMode, None, "test individual", 0
        ) mustBe expectedAddTrusteeIndividualSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = false, toggled = true), NormalMode, None, "test individual", 0
        ) mustBe expectedInProgressTrusteeIndividualSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = true, toggled = true), NormalMode, None, "test individual", 0
        ) mustBe expectedCompletedTrusteeIndividualSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividualBlank, UpdateMode, srn, "test individual", 0
        ) mustBe expectedAddTrusteeIndividualSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = false, toggled = true), UpdateMode, srn, "test individual", 0
        ) mustBe expectedInProgressTrusteeIndividualSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = true, toggled = true), UpdateMode, srn, "test individual", 0
        ) mustBe expectedCompletedTrusteeIndividualSpokes(UpdateMode, srn)
      }
    }
  }
}

object HsTaskListHelperUtilsSpec extends SpecBase with OptionValues with CompletionStatusHelper {

  val srn = Some("S123")
  private val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(true)
  private val address = Address("line 1", "line 2", Some("line 3"), Some("line 4"), Some("zz11zz"), "GB")

  protected def establisherCompanyBlank: UserAnswers = {
    UserAnswers().set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsEstablisherNewId(0))(true)
    )
      .asOpt.value
  }

  protected def establisherCompanyWithPartialData: UserAnswers = {
    establisherCompanyBlank
      .set(CompanyVatId(0))(Vat.Yes("test-vat")).flatMap(
      _.set(establisherCompanyPath.HasCompanyVATId(0))(true).flatMap(
      _.set(establisherCompanyPath.CompanyAddressId(0))(address).flatMap(
        _.set(CompanyEmailId(0))("test@test.com").flatMap(
            _.set(DirectorNameId(0, 0))(PersonName("Joe", "Bloggs"))
        )))).asOpt.value
  }

  protected def trusteeCompanyBlank: UserAnswers = {
    UserAnswers().set(trusteeCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def trusteeIndividualBlank: UserAnswers = {
    UserAnswers().set(trusteeIndividualPath.TrusteeDetailsId(0))(PersonDetails("test", None, "person", LocalDate.now)).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def trusteeCompany(isComplete: Boolean): UserAnswers = {
    trusteeCompanyBlank
      .set(IsTrusteeNewId(0))(true).flatMap(
      _.set(trusteeCompanyPath.IsAddressCompleteId(0))(isComplete).flatMap(
        _.set(trusteeCompanyPath.IsDetailsCompleteId(0))(isComplete).flatMap(
          _.set(trusteeCompanyPath.IsContactDetailsCompleteId(0))(isComplete)))).asOpt.value
  }

  // TODO PODS-2940 Need to do something here?
  protected def trusteeIndividual(isComplete: Boolean, toggled:Boolean): UserAnswers = {
    val ua = trusteeIndividualBlank
      .set(IsTrusteeNewId(0))(true)
//      .flatMap(
//      _.set(trusteeIndividualPath.IsAddressCompleteId(0))(isComplete).flatMap(
//        _.set(trusteeIndividualPath.IsDetailsCompleteId(0))(isComplete).flatMap(
//          _.set(trusteeIndividualPath.IsContactDetailsCompleteId(0))(isComplete))))
      .asOpt.value
    setTrusteeCompletionStatus(isComplete = isComplete, toggled = toggled, 0, ua)
  }

  protected def establisherCompanyWithCompletedDirectors = UserAnswers(readJsonFromFile("/payloadHnS.json"))

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

  def expectedAddTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_add_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedAddTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_add_details", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_add_address", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_add_contact", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedInProgressTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedInProgressTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_details", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_address", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_contact", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedCompletedTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_details", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_address", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersCompany_change_contact", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true)))
    )

  def expectedCompletedTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_details", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_address", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__sectionEstablishersIndividual_change_contact", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true)))
  )

}