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

package navigators

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import controllers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.partner._
import identifiers.register.establishers.partnership.{AddPartnersId, PartnershipDetailsId}
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import identifiers.{EstablishersOrTrusteesChangedId, Identifier}
import models.Mode.checkMode
import models._
import models.address.Address
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{FakeFeatureSwitchManagementService, UserAnswers}

class EstablishersPartnerNavigatorOldSpec extends SpecBase with NavigatorBehaviour {
  //scalastyle:off line.size.limit
  //scalastyle:off magic.number
  import EstablishersPartnerNavigatorOldSpec._

  private val navigator = new EstablishersPartnerNavigatorOld(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(false))

  private def commonRoutes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (AddPartnersId(0), emptyAnswers, partnerDetails(0, mode), true, None, true),
    (AddPartnersId(0), addPartnersTrue, partnerDetails(1, mode), true, None, true),
    (AddPartnersId(0), addOnePartner, sessionExpired, false, None, false),
    (AddPartnersId(0), addPartnersMoreThan10, otherPartners(mode), true, None, true),
    (PartnerDetailsId(0, 0), emptyAnswers, partnerNino(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerDetailsId(0, 0), newPartner, partnerNino(mode), true, Some(exitJourney(mode, newPartner)), true),
    (PartnerNinoId(0, 0), emptyAnswers, partnerUtr(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerNinoId(0, 0), newPartner, partnerUtr(mode), true, Some(exitJourney(mode, newPartner)), true),
    (PartnerUniqueTaxReferenceId(0, 0), emptyAnswers, partnerAddressPostcode(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerUniqueTaxReferenceId(0, 0), newPartner, partnerAddressPostcode(mode), true, Some(exitJourney(mode, newPartner)), true),
    (PartnerAddressPostcodeLookupId(0, 0), emptyAnswers, partnerAddressList(mode), true, Some(partnerAddressList(checkMode(mode))), true),
    (PartnerAddressListId(0, 0), emptyAnswers, partnerAddress(mode), true, Some(partnerAddress(checkMode(mode))), true),
    (PartnerAddressYearsId(0, 0), addressYearsOverAYearNew, partnerContactDetails(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
    (PartnerAddressYearsId(0, 0), addressYearsOverAYear, partnerContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerAddressYearsId(0, 0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PartnerConfirmPreviousAddressId(0, 0), emptyAnswers, none, false, Some(sessionExpired), false),
    (PartnerConfirmPreviousAddressId(0, 0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
    (PartnerConfirmPreviousAddressId(0, 0), confirmPreviousAddressNo, none, false, Some(partnerPreviousAddPostcode(checkMode(mode))), false),
    (PartnerPreviousAddressPostcodeLookupId(0, 0), emptyAnswers, partnerPreviousAddList(mode), true, Some(partnerPreviousAddList(checkMode(mode))), true),
    (PartnerPreviousAddressListId(0, 0), emptyAnswers, partnerPreviousAddress(mode), true, Some(partnerPreviousAddress(checkMode(mode))), true),
    (PartnerPreviousAddressId(0, 0), emptyAnswers, partnerContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerPreviousAddressId(0, 0), newPartner, partnerContactDetails(mode), true, Some(exitJourney(mode, newPartner)), true),
    (PartnerContactDetailsId(0, 0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnerContactDetailsId(0, 0), newPartner, checkYourAnswers(mode), true, Some(exitJourney(mode, newPartner)), true)
  )


  private def normalRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(NormalMode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeletePartnerId(0), emptyAnswers, addPartners(NormalMode), false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addPartners(NormalMode), true, None, true),
    (AddPartnersId(0), addPartnersFalse, partnershipReview(NormalMode), true, None, true),
    (PartnerAddressId(0, 0), emptyAnswers, partnerAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true),
    (PartnerAddressId(0, 0), newPartner, partnerAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true)
  )

  private def editRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(UpdateMode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeletePartnerId(0), emptyAnswers, anyMoreChanges, false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addPartners(UpdateMode), true, None, true),
    (AddPartnersId(0), addPartnersFalseWithChanges, anyMoreChanges, true, None, true),
    (AddPartnersId(0), addPartnersFalseNewEst, partnershipReview(UpdateMode), true, None, true),
    (AddPartnersId(0), addPartnersFalse, taskList, true, None, true),
    (PartnerAddressYearsId(0, 0), addressYearsUnderAYear, partnerPreviousAddPostcode(UpdateMode), true, addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear), true),
    (PartnerAddressYearsId(0, 0), addressYearsUnderAYearWithExistingCurrentAddress, partnerPreviousAddPostcode(UpdateMode), true, addressYearsLessThanTwelveEdit(CheckUpdateMode, addressYearsUnderAYearWithExistingCurrentAddress), true),
    (PartnerNewNinoId(0, 0), emptyAnswers, none, true, Some(exitJourney(UpdateMode, emptyAnswers)), true),
    (PartnerAddressId(0, 0), emptyAnswers, partnerAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true),
    (PartnerAddressId(0, 0), newPartner, partnerAddressYears(UpdateMode), true, Some(checkYourAnswers(UpdateMode)), true)
  )


  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, editRoutes(), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object EstablishersPartnerNavigatorOldSpec extends SpecBase with OptionValues {
  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private val emptyAnswers = UserAnswers(Json.obj())

  val establisherIndex = Index(0)
  val partnerIndex = Index(0)
  private val johnDoe = PersonDetails("John", None, "Doe", LocalDate.now())
  private val newPartner = UserAnswers(Json.obj()).set(IsNewPartnerId(establisherIndex, partnerIndex))(true).asOpt.value
  val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.OverAYear).flatMap(
    _.set(IsNewPartnerId(establisherIndex, partnerIndex))(true)).asOpt.value
  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.UnderAYear).asOpt.value
  val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(establisherIndex, partnerIndex))(AddressYears.UnderAYear).flatMap(
    _.set(ExistingCurrentAddressId(0, 0))(Address("Line 1", "Line 2", None, None, None, "UK"))).asOpt.value

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(PartnerConfirmPreviousAddressId(0, 0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(PartnerConfirmPreviousAddressId(0, 0))(false).asOpt.value

  private val config = injector.instanceOf[Configuration]

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers)=
    (
      userAnswers.get(PartnerAddressYearsId(establisherIndex, partnerIndex)),
      mode,
      userAnswers.get(ExistingCurrentAddressId(establisherIndex, partnerIndex))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        Some(confirmPreviousAddress)
      case (Some(AddressYears.UnderAYear), _, _) =>
        Some(partnerPreviousAddPostcode(checkMode(mode)))
      case (Some(AddressYears.OverAYear), _, _) =>
        Some(exitJourney(mode, userAnswers))
      case _ =>
        Some(partnerPreviousAddPostcode(checkMode(mode)))
    }

  private def confirmPreviousAddress = routes.PartnerConfirmPreviousAddressController.onPageLoad(0, 0, None)

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def validData(partners: PersonDetails*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          PartnershipDetailsId.toString -> PartnershipDetails("test partnership name", false),
          "partner" -> partners.map(d => Json.obj(PartnerDetailsId.toString -> Json.toJson(d)))
        )
      )
    )
  }
  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, None)
  private val addPartnersTrue = UserAnswers(validData(johnDoe)).set(AddPartnersId(0))(true).asOpt.value
  private val addPartnersFalse = UserAnswers(validData(johnDoe)).set(AddPartnersId(0))(false).asOpt.value
  private val addPartnersFalseWithChanges = UserAnswers(validData(johnDoe)).
    set(AddPartnersId(0))(false).flatMap(_.set(EstablishersOrTrusteesChangedId)(true))
    .asOpt.value
  private val addPartnersFalseNewEst = UserAnswers(validData(johnDoe)).set(AddPartnersId(0))(false)
    .flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addPartnersMoreThan10 = UserAnswers(validData(Seq.fill(10)(johnDoe): _*))
  private val addOnePartner = UserAnswers(validData(johnDoe))

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if(answers.get(IsNewPartnerId(establisherIndex, partnerIndex)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private def partnerNino(mode: Mode) = routes.PartnerNinoController.onPageLoad(mode, establisherIndex, partnerIndex, None)

  private def partnerDetails(partnerIndex: Index = Index(0), mode: Mode) = routes.PartnerDetailsController.onPageLoad(mode, 0, partnerIndex, None)

  private def partnerUtr(mode: Mode) = routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerContactDetails(mode: Mode) = routes.PartnerContactDetailsController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressPostcode(mode: Mode) = routes.PartnerAddressPostcodeLookupController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressList(mode: Mode) = routes.PartnerAddressListController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddress(mode: Mode) = routes.PartnerAddressController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerAddressYears(mode: Mode) = routes.PartnerAddressYearsController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddPostcode(mode: Mode) = routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddList(mode: Mode) = routes.PartnerPreviousAddressListController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def partnerPreviousAddress(mode: Mode) = routes.PartnerPreviousAddressController.onPageLoad(mode, partnerIndex, establisherIndex, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(mode, establisherIndex, partnerIndex, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def addPartners(mode: Mode) = controllers.register.establishers.partnership.routes.AddPartnersController.onPageLoad(mode, establisherIndex, None)

  private def partnershipReview(mode: Mode) = controllers.register.establishers.partnership.routes.PartnershipReviewController.onPageLoad(mode, establisherIndex, None)

  private def otherPartners(mode: Mode) = controllers.register.establishers.partnership.routes.OtherPartnersController.onPageLoad(mode, 0, None)
}
