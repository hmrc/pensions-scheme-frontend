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

import base.{JsonFileReader, SpecBase}
import controllers.register.establishers.company.{routes => establisherCompanyRoutes}
import controllers.register.establishers.partnership.{routes => establisherPartnershipRoutes}
import controllers.register.trustees.company.{routes => trusteeCompanyRoutes}
import controllers.register.trustees.individual.{routes => trusteeIndividualRoutes}
import controllers.register.trustees.partnership.{routes => trusteePartnershipRoutes}
import helpers.DataCompletionHelper
import identifiers.register.establishers.{IsEstablisherNewId, company => establisherCompanyPath, partnership => establisherPartnershipPath}
import identifiers.register.trustees.{IsTrusteeNewId, company => trusteeCompanyPath, individual => trusteeIndividualPath, partnership => trusteePartnershipPath}
import models.address.Address
import models.person.PersonName
import models.{CompanyDetails, EntitySpoke, Link, Mode, NormalMode, UpdateMode, _}
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

  "getEstablisherPartnershipSpokes" must {
    "display all spokes with appropriate links" when {
      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          establisherPartnershipBlank, NormalMode, None, name = partnershipName, index = 0
        ) mustBe expectedAddEstablisherPartnershipSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          answersIncomplete, NormalMode, None, name = partnershipName, index = 2
        ) mustBe expectedInProgressEstablisherPartnershipSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          answersComplete, NormalMode, None, name = partnershipName, index = 2
        ) mustBe expectedCompletedEstablisherPartnershipSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          establisherCompanyBlank, UpdateMode, srn, name = partnershipName, index = 0
        ) mustBe expectedAddEstablisherPartnershipSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          answersIncomplete, UpdateMode, srn, name = partnershipName, index = 2
        ) mustBe expectedInProgressEstablisherPartnershipSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getEstablisherPartnershipSpokes(
          answersComplete, UpdateMode, srn, name = partnershipName, index = 2
        ) mustBe expectedCompletedEstablisherPartnershipSpokes(UpdateMode, srn)
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
          answersIncomplete, NormalMode, None, "test company", 0
        ) mustBe expectedInProgressTrusteeCompanySpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersComplete, NormalMode, None, "test company", 0
        ) mustBe expectedCompletedTrusteeCompanySpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          trusteeCompanyBlank, UpdateMode, srn, "test company", 0
        ) mustBe expectedAddTrusteeCompanySpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersIncomplete, UpdateMode, srn, "test company", 0
        ) mustBe expectedInProgressTrusteeCompanySpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeCompanySpokes(
          answersComplete, UpdateMode, srn, "test company", 0
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
          trusteeIndividual(isComplete = false), NormalMode, None, "test individual", 0
        ) mustBe expectedInProgressTrusteeIndividualSpokes(NormalMode, None)
      }

      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = true), NormalMode, None, "test individual", 0
        ) mustBe expectedCompletedTrusteeIndividualSpokes(NormalMode, None)
      }

      "in variations journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividualBlank, UpdateMode, srn, "test individual", 0
        ) mustBe expectedAddTrusteeIndividualSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = false), UpdateMode, srn, "test individual", 0
        ) mustBe expectedInProgressTrusteeIndividualSpokes(UpdateMode, srn)
      }

      "in variations journey when all spokes are complete" in {
        subscriptionHelper.getTrusteeIndividualSpokes(
          trusteeIndividual(isComplete = true), UpdateMode, srn, "test individual", 0
        ) mustBe expectedCompletedTrusteeIndividualSpokes(UpdateMode, srn)
      }
    }
  }

  "getTrusteePartnershipSpokes" must {
    "display all spokes with appropriate links" when {

      "in subscription journey when all spokes are uninitiated" in {
        subscriptionHelper.getTrusteePartnershipSpokes(
          trusteePartnershipBlank, NormalMode, None, "test partnership", 0
        ) mustBe expectedAddTrusteePartnershipSpokes(NormalMode, None)
      }

     "in subscription journey when all spokes are in progress" in {
        subscriptionHelper.getTrusteePartnershipSpokes(
          answersIncomplete, NormalMode, None, "test partnership", 2
        ) mustBe expectedInProgressTrusteePartnershipSpokes(NormalMode, None)
      }
      
      "in subscription journey when all spokes are complete" in {
        subscriptionHelper.getTrusteePartnershipSpokes(
          answersComplete, NormalMode, None, "test partnership", 2
        ) mustBe expectedCompletedTrusteePartnershipSpokes(NormalMode, None)
      }


    "in variations journey when all spokes are uninitiated" in {
      subscriptionHelper.getTrusteePartnershipSpokes(
        trusteePartnershipBlank, UpdateMode, srn, "test partnership", 0
      ) mustBe expectedAddTrusteePartnershipSpokes(UpdateMode, srn)
    }

    "in variations journey when all spokes are in progress" in {
      subscriptionHelper.getTrusteePartnershipSpokes(
        answersIncomplete, UpdateMode, srn, "test partnership", 2
      ) mustBe expectedInProgressTrusteePartnershipSpokes(UpdateMode, srn)
    }

    "in variations journey when all spokes are complete" in {
      subscriptionHelper.getTrusteePartnershipSpokes(
        answersComplete, UpdateMode, srn, "test partnership", 2
      ) mustBe expectedCompletedTrusteePartnershipSpokes(UpdateMode, srn)
    }
    }
  }
}

object HsTaskListHelperUtilsSpec extends SpecBase with OptionValues with DataCompletionHelper with JsonFileReader {

  val srn = Some("S123")
  private val partnershipName = "test partnership"
  private val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(true)
  private val address = Address("line 1", "line 2", Some("line 3"), Some("line 4"), Some("zz11zz"), "GB")

  protected def establisherCompanyBlank: UserAnswers = {
    UserAnswers().set(establisherCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsEstablisherNewId(0))(true)
    )
      .asOpt.value
  }

  protected def establisherPartnershipBlank: UserAnswers = {
    UserAnswers().set(establisherPartnershipPath.PartnershipDetailsId(0))(PartnershipDetails(partnershipName)).flatMap(
      _.set(IsEstablisherNewId(0))(true)
    ).asOpt.value
  }

  protected def trusteeCompanyBlank: UserAnswers = {
    UserAnswers().set(trusteeCompanyPath.CompanyDetailsId(0))(CompanyDetails("test company")).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def trusteeIndividualBlank: UserAnswers = {
    UserAnswers().set(trusteeIndividualPath.TrusteeNameId(0))(PersonName("test", "person")).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def trusteePartnershipBlank: UserAnswers = {
    UserAnswers().set(trusteePartnershipPath.PartnershipDetailsId(0))(PartnershipDetails("test partnership")).flatMap(
      _.set(IsTrusteeNewId(0))(true)
    )
      .asOpt.value
  }

  protected def establisherCompanyWithCompletedDirectors = UserAnswers(readJsonFromFile("/payload.json"))
  protected def trusteeIndividual(isComplete: Boolean): UserAnswers = {
    val ua = trusteeIndividualBlank
      .set(IsTrusteeNewId(0))(true)
      .asOpt.value
    setTrusteeCompletionStatus(isComplete = isComplete, 0, ua)
  }

  protected def trusteePartnership(isComplete: Boolean): UserAnswers = {
    val ua = trusteePartnershipBlank
      .set(IsTrusteeNewId(0))(true)
      .asOpt.value
    setTrusteeCompletionStatus(isComplete = isComplete, 0, ua)
  }

  protected def answersComplete = UserAnswers(readJsonFromFile("/payload.json"))
  protected def answersIncomplete = UserAnswers(readJsonFromFile("/payloadInProgress.json"))

  def modeBasedCompletion(mode: Mode, completion: Option[Boolean]): Option[Boolean] = if(mode == NormalMode) completion else None

  def subscriptionHelper: HsTaskListHelper = new HsTaskListHelperRegistration(UserAnswers(), fakeFeatureSwitch)
  def variationsHelper(viewOnly: Boolean = false): HsTaskListHelper = new HsTaskListHelperVariations(UserAnswers(), viewOnly, srn, fakeFeatureSwitch)

  def expectedAddSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_directors", "test company"),
      controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController.onPageLoad(mode, srn, 0).url), None)
  )

  def expectedInProgressSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
      establisherCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedCompletedSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
      establisherCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_directors", "test company"),
      controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(mode, srn, 0).url), modeBasedCompletion(mode, Some(true)))
  )

  def expectedAddEstablisherPartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__add_details", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_address", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, srn, 0).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_partners", partnershipName),
      controllers.register.establishers.partnership.partner.routes.WhatYouWillNeedPartnerController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedInProgressEstablisherPartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, srn, 2).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, srn, 2).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", partnershipName),
      establisherPartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_partners", partnershipName),
      controllers.register.establishers.partnership.partner.routes.WhatYouWillNeedPartnerController.onPageLoad(mode, 2, srn).url), None)
  )

  def expectedCompletedEstablisherPartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", partnershipName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", partnershipName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", partnershipName),
      establisherPartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_partners", partnershipName),
      controllers.register.establishers.partnership.partner.routes.WhatYouWillNeedPartnerController.onPageLoad(mode, 2, srn).url), None)
  )

  def expectedAddTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedAddTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedAddTrusteePartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__add_details", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_address", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, 0, srn).url), None),
    EntitySpoke(Link(messages("messages__schemeTaskList__add_contact", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, 0, srn).url), None)
  )

  def expectedInProgressTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
      trusteeCompanyRoutes.WhatYouWillNeedCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedInProgressTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test individual"),
      trusteeIndividualRoutes.WhatYouWillNeedIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedInProgressTrusteePartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipAddressController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(false))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test partnership"),
      trusteePartnershipRoutes.WhatYouWillNeedPartnershipContactDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(false)))
  )

  def expectedCompletedTrusteeCompanySpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test company"),
      trusteeCompanyRoutes.CheckYourAnswersCompanyContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true)))
    )

  def expectedCompletedTrusteeIndividualSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualAddressController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test individual"),
      trusteeIndividualRoutes.CheckYourAnswersIndividualContactDetailsController.onPageLoad(mode, 0, srn).url), modeBasedCompletion(mode, Some(true)))
  )

  def expectedCompletedTrusteePartnershipSpokes(mode: Mode, srn: Option[String]): Seq[EntitySpoke] = Seq(
    EntitySpoke(Link(messages("messages__schemeTaskList__change_details", "test partnership"),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_address", "test partnership"),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipAddressController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true))),
    EntitySpoke(Link(messages("messages__schemeTaskList__change_contact", "test partnership"),
      trusteePartnershipRoutes.CheckYourAnswersPartnershipContactDetailsController.onPageLoad(mode, 2, srn).url), modeBasedCompletion(mode, Some(true)))
  )
}