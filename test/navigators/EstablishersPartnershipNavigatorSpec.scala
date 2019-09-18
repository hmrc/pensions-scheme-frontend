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
import controllers.register.establishers.partnership.routes
import identifiers.register.establishers.ExistingCurrentAddressId
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.partnership._
import models.Mode.checkMode
import models._
import models.address.Address
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class EstablishersPartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import EstablishersPartnershipNavigatorSpec._


  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (CheckYourAnswersId(0), emptyAnswers, addPartners(mode), true, None, true),
    (PartnershipDetailsId(0), emptyAnswers, partnershipVat(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipDetailsId(0), newEstablisher, partnershipVat(mode), true, Some(exitJourney(mode, newEstablisher)), true),
    (PartnershipVatId(0), emptyAnswers, partnershipPaye(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipVatId(0), newEstablisher, partnershipPaye(mode), true, Some(exitJourney(mode, newEstablisher)), true),
    (PartnershipPayeId(0), emptyAnswers, partnershipUtr(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipPayeId(0), newEstablisher, partnershipUtr(mode), true, Some(exitJourney(mode, newEstablisher)), true),
    (PartnershipUniqueTaxReferenceID(0), emptyAnswers, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipUniqueTaxReferenceID(0), newEstablisher, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode, newEstablisher)), true),
    (PartnershipPostcodeLookupId(0), emptyAnswers, partnershipAddressList(mode), true, Some(partnershipAddressList(checkMode(mode))), true),
    (PartnershipAddressListId(0), emptyAnswers, partnershipAddress(mode), true, Some(partnershipAddress(checkMode(mode))), true),
    (PartnershipAddressYearsId(0), addressYearsOverAYear, partnershipContact(mode), true, Some(exitJourney(mode, addressYearsOverAYear)), true),
    (PartnershipAddressYearsId(0), addressYearsOverAYearNew, partnershipContact(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
    (PartnershipAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PartnershipPreviousAddressPostcodeLookupId(0), emptyAnswers, partnershipPaList(mode), true, Some(partnershipPaList(checkMode(mode))), true),
    (PartnershipPreviousAddressListId(0), emptyAnswers, partnershipPa(mode), true, Some(partnershipPa(checkMode(mode))), true),
    (PartnershipPreviousAddressId(0), emptyAnswers, partnershipContact(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipPreviousAddressId(0), newEstablisher, partnershipContact(mode), true, Some(exitJourney(mode, newEstablisher)), true),
    (OtherPartnersId(0), emptyAnswers, if (mode == UpdateMode) anyMoreChanges else partnershipReview(mode), true, Some(partnershipReview(mode)), true),
    (PartnershipEnterVATId(0), emptyAnswers, defaultPage, false, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipEnterVATId(0), newEstablisher, defaultPage, false, Some(exitJourney(mode, newEstablisher)), true)
  )

  private def normalOnlyRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (PartnershipContactDetailsId(0), emptyAnswers, exitJourney(NormalMode, emptyAnswers), true, Some(exitJourney(NormalMode, emptyAnswers)), true),
    (PartnershipReviewId(0), emptyAnswers, addEstablisher(NormalMode), false, None, true),
    (PartnershipAddressYearsId(0), addressYearsUnderAYear, partnershipPaPostCodeLookup(NormalMode), true, Some(partnershipPaPostCodeLookup(checkMode(NormalMode))), true),
    (PartnershipAddressId(0), emptyAnswers, partnershipAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true),
    (PartnershipAddressId(0), newEstablisher, partnershipAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true)
  )

  private def updateOnlyRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (PartnershipContactDetailsId(0), emptyAnswers, checkYourAnswers(UpdateMode), true, Some(exitJourney(UpdateMode, emptyAnswers)), true),
    (PartnershipReviewId(0), emptyAnswers, anyMoreChanges, false, None, true),
    (PartnershipAddressYearsId(0), addressYearsUnderAYear, partnershipPaPostCodeLookup(UpdateMode), true, addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear), true),
    (PartnershipAddressYearsId(0), addressYearsUnderAYearWithExistingCurrentAddress, partnershipPaPostCodeLookup(UpdateMode), true, addressYearsLessThanTwelveEdit(CheckUpdateMode, addressYearsUnderAYearWithExistingCurrentAddress), true),
    (PartnershipConfirmPreviousAddressId(0), emptyAnswers, defaultPage, false, Some(sessionExpired), false),
    (PartnershipConfirmPreviousAddressId(0), confirmPreviousAddressYes, defaultPage, false, Some(anyMoreChanges), false),
    (PartnershipConfirmPreviousAddressId(0), confirmPreviousAddressNo, defaultPage, false, Some(partnershipPaPostCodeLookup(checkMode(UpdateMode))), false),
    (PartnershipPayeVariationsId(0), emptyAnswers, none, true, Some(exitJourney(checkMode(UpdateMode), emptyAnswers)), true),
    (PartnershipAddressId(0), emptyAnswers, partnershipAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true),
    (PartnershipAddressId(0), newEstablisher, partnershipAddressYears(UpdateMode), true, Some(checkYourAnswers(UpdateMode)), true)
  )

  private def normalRoutes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    routes(NormalMode) ++ normalOnlyRoutes: _*
  )

  private def updateRoutes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    routes(UpdateMode) ++ updateOnlyRoutes: _*
  )

  private val navigator: EstablishersPartnershipNavigatorOld =
    new EstablishersPartnershipNavigatorOld(FakeUserAnswersCacheConnector, frontendAppConfig)

  navigator.getClass.getSimpleName must {
    appRunning()
    val navigator = new EstablishersPartnershipNavigatorOld(FakeUserAnswersCacheConnector, frontendAppConfig)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object EstablishersPartnershipNavigatorSpec extends SpecBase with OptionValues {
  private val srn = "srn"

  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers(Json.obj()).set(IsEstablisherNewId(0))(true).asOpt.value

  private def partnershipVat(mode: Mode) = routes.PartnershipVatController.onPageLoad(mode, 0, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)
  private def none = controllers.routes.IndexController.onPageLoad()

  private def exitJourney(mode: Mode, answers: UserAnswers) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if (answers.get(IsEstablisherNewId(0)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId(0))(false).asOpt.value

  private def partnershipPaye(mode: Mode) = routes.PartnershipPayeController.onPageLoad(mode, 0, None)

  private def partnershipUtr(mode: Mode) = routes.PartnershipUniqueTaxReferenceController.onPageLoad(mode, 0, None)

  private def partnershipPostcodeLookup(mode: Mode) = routes.PartnershipPostcodeLookupController.onPageLoad(mode, 0, None)

  private def partnershipAddressList(mode: Mode) = routes.PartnershipAddressListController.onPageLoad(mode, 0, None)

  private def partnershipAddress(mode: Mode) = routes.PartnershipAddressController.onPageLoad(mode, 0, None)

  private def partnershipAddressYears(mode: Mode) = routes.PartnershipAddressYearsController.onPageLoad(mode, 0, None)

  private def partnershipContact(mode: Mode) = routes.PartnershipContactDetailsController.onPageLoad(mode, 0, None)

  private def partnershipPaPostCodeLookup(mode: Mode) = routes.PartnershipPreviousAddressPostcodeLookupController.onPageLoad(mode, 0, None)

  private def partnershipPaList(mode: Mode) = routes.PartnershipPreviousAddressListController.onPageLoad(mode, 0, None)

  private def partnershipPa(mode: Mode) = routes.PartnershipPreviousAddressController.onPageLoad(mode, 0, None)

  private def partnershipReview(mode: Mode) = routes.PartnershipReviewController.onPageLoad(mode, 0, None)

  private def addPartners(mode: Mode) = routes.AddPartnersController.onPageLoad(mode, 0, None)

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private def defaultPage = controllers.routes.IndexController.onPageLoad()

  private val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).flatMap(_.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
    _.set(ExistingCurrentAddressId(0))(Address("Line 1", "Line 2", None, None, None, "UK"))).asOpt.value

  private def confirmPreviousAddress = controllers.register.establishers.partnership.routes.PartnershipConfirmPreviousAddressController.onPageLoad(0, None)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers)=
    (
      userAnswers.get(PartnershipAddressYearsId(0)),
      mode,
      userAnswers.get(ExistingCurrentAddressId(0))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        Some(confirmPreviousAddress)
      case (Some(AddressYears.UnderAYear), _, _) =>
        Some(partnershipPaPostCodeLookup(checkMode(mode)))
      case (Some(AddressYears.OverAYear), _, _) =>
        Some(exitJourney(mode, userAnswers))
      case _ =>
        Some(partnershipPaPostCodeLookup(checkMode(mode)))
    }
}
