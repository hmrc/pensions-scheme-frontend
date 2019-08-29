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
import controllers.register.trustees.partnership.routes
import identifiers.Identifier
import identifiers.register.trustees.{ExistingCurrentAddressId, IsTrusteeNewId}
import identifiers.register.trustees.partnership._
import models.Mode.checkMode
import models._
import models.address.Address
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

//scalastyle:off line.size.limit
class TrusteesPartnershipNavigatorOldSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesPartnershipNavigatorOldSpec._

  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (PartnershipDetailsId(0), emptyAnswers, partnershipVat(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipDetailsId(0), newTrustee, partnershipVat(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipVatId(0), emptyAnswers, partnershipPaye(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipVatId(0), newTrustee, partnershipPaye(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipPayeId(0), emptyAnswers, partnershipUtr(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipPayeId(0), newTrustee, partnershipUtr(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipUniqueTaxReferenceId(0), emptyAnswers, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipUniqueTaxReferenceId(0), newTrustee, partnershipPostcodeLookup(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipPostcodeLookupId(0), emptyAnswers, partnershipAddressList(mode), true, Some(partnershipAddressList(checkMode(mode))), true),
    (PartnershipAddressListId(0), emptyAnswers, partnershipAddress(mode), true, Some(partnershipAddress(checkMode(mode))), true),
    (PartnershipAddressId(0), newTrustee, partnershipAddressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (PartnershipAddressYearsId(0), addressYearsOverAYear, partnershipContact(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PartnershipPreviousAddressPostcodeLookupId(0), emptyAnswers, partnershipPaList(mode), true, Some(partnershipPaList(checkMode(mode))), true),
    (PartnershipPreviousAddressListId(0), emptyAnswers, partnershipPa(mode), true, Some(partnershipPa(checkMode(mode))), true),
    (PartnershipPreviousAddressId(0), emptyAnswers, partnershipContact(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipPreviousAddressId(0), newTrustee, partnershipContact(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (PartnershipContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipContactDetailsId(0), newTrustee, checkYourAnswers(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CheckYourAnswersId(0), emptyAnswers, addTrustee(mode), false, None, true),
    (PartnershipEnterVATId(0), emptyAnswers, defaultPage, false, Some(exitJourney(mode, emptyAnswers)), true),
    (PartnershipEnterVATId(0), newTrustee, defaultPage, false, Some(exitJourney(mode, newTrustee)), true)
  )

  private def normalRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = {
    routes(NormalMode) ++ Table(
      ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
      (PartnershipAddressYearsId(0), addressYearsUnderAYear, partnershipPaPostCodeLookup(NormalMode), true,
        Some(partnershipPaPostCodeLookup(CheckMode)), true),
      (PartnershipAddressId(0), emptyAnswers, partnershipAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true)
    )
  }

  private def updateOnlyRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = {
    routes(UpdateMode) ++ Table(
      ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
      (PartnershipAddressYearsId(0), addressYearsUnderAYear, partnershipPaPostCodeLookup(UpdateMode), true, Some(addressYearsLessThanTwelveEdit(UpdateMode, addressYearsUnderAYear)), true),
      (PartnershipAddressYearsId(0), addressYearsUnderAYearWithExistingCurrentAddress, partnershipPaPostCodeLookup(UpdateMode), true, Some(addressYearsLessThanTwelveEdit(CheckUpdateMode, addressYearsUnderAYearWithExistingCurrentAddress)), true),
      (PartnershipConfirmPreviousAddressId(0), confirmPreviousAddressYes, defaultPage, false, Some(anyMoreChanges), false),
      (PartnershipConfirmPreviousAddressId(0), confirmPreviousAddressNo, defaultPage, false, Some(partnershipPaPostCodeLookup(CheckUpdateMode)), false),
      (PartnershipConfirmPreviousAddressId(0), emptyAnswers, defaultPage, false, Some(sessionExpired), false),
      (PartnershipPayeVariationsId(0), emptyAnswers, none, true, Some(exitJourney(UpdateMode, emptyAnswers)), true),
      (PartnershipAddressId(0), emptyAnswers, partnershipAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true)
    )
  }

  val navigator = new TrusteesPartnershipNavigatorOld(FakeUserAnswersCacheConnector, frontendAppConfig)

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateOnlyRoutes(), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }
}

object TrusteesPartnershipNavigatorOldSpec extends OptionValues {

  private val newTrustee = UserAnswers(Json.obj()).set(IsTrusteeNewId(0))(true).asOpt.value

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())

  private def defaultPage = controllers.routes.IndexController.onPageLoad()

  private def partnershipVat(mode: Mode) = routes.PartnershipVatController.onPageLoad(mode, 0, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

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

  private def addTrustee(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()
  private def confirmPreviousAddress = controllers.register.trustees.partnership.routes.PartnershipConfirmPreviousAddressController.onPageLoad(0, None)

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(PartnershipAddressYearsId(0))(AddressYears.UnderAYear).flatMap(
    _.set(ExistingCurrentAddressId(0))(Address("Line 1", "Line 2", None, None, None, "UK"))).asOpt.value

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId(0))(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int = 0): Call =
    if (mode == CheckMode || mode == NormalMode)
      checkYourAnswers(mode)
    else if (answers.get(IsTrusteeNewId(index)).getOrElse(false))
      checkYourAnswers(mode)
    else
      anyMoreChanges

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers): Call =
    (
      userAnswers.get(ExistingCurrentAddressId(0)),
      mode
    ) match {
      case (None, CheckUpdateMode) =>
        partnershipPaPostCodeLookup(checkMode(mode))
      case (_, CheckUpdateMode) =>
        confirmPreviousAddress
      case _ =>
        partnershipPaPostCodeLookup(checkMode(mode))
    }
}


