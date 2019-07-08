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
import identifiers.register.trustees.ExistingCurrentAddressId
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models.Mode.checkMode
import models._
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{FakeFeatureSwitchManagementService, UserAnswers}

class TrusteesIndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesIndividualNavigatorSpec._

  private def routes(mode: Mode, isPrevAddEnabled: Boolean = false) = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (TrusteeDetailsId(0), emptyAnswers, nino(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (TrusteeDetailsId(0), newTrustee, nino(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (TrusteeNinoId(0), emptyAnswers, utr(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (TrusteeNinoId(0), newTrustee, utr(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (UniqueTaxReferenceId(0), emptyAnswers, postcode(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (UniqueTaxReferenceId(0), newTrustee, postcode(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (IndividualPostCodeLookupId(0), emptyAnswers, addressList(mode), true, Some(addressList(checkMode(mode))), true),
    (IndividualAddressListId(0), emptyAnswers, address(mode), true, Some(address(checkMode(mode))), true),
    (TrusteeAddressId(0), emptyAnswers, addressYears(mode), true,
      if(mode == UpdateMode) Some(addressYears(checkMode(mode))) else Some(checkYourAnswers(mode)), true),
    (TrusteeAddressId(0), newTrustee, addressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (TrusteeAddressYearsId(0), overAYearNew, contactDetails(mode), true, Some(exitJourney(mode,overAYearNew)), true),
    (TrusteeAddressYearsId(0), overAYear, contactDetails(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (TrusteeAddressYearsId(0), underAYear, previousAddressPostcode(mode), true, addressYearsLessThanTwelveEdit(mode, underAYear), true),
    (TrusteeAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (IndividualConfirmPreviousAddressId(0), emptyAnswers, none, false, Some(sessionExpired), false),
    (IndividualConfirmPreviousAddressId(0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
    (IndividualConfirmPreviousAddressId(0), confirmPreviousAddressNo, none, false, Some(previousAddressPostcode(checkMode(mode))), false),
    (IndividualPreviousAddressPostCodeLookupId(0), emptyAnswers, previousAddressList(mode), true, Some(previousAddressList(checkMode(mode))), true),
    (TrusteePreviousAddressListId(0), emptyAnswers, previousAddress(mode), true, Some(previousAddress(checkMode(mode))), true),
    (TrusteePreviousAddressId(0), emptyAnswers, contactDetails(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (TrusteePreviousAddressId(0), newTrustee, contactDetails(mode), true, Some(exitJourney(mode,newTrustee)), true),
    (TrusteeContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode,emptyAnswers)), true),
    (CheckYourAnswersId, emptyAnswers, addTrustee(mode), false, None, true)
  )

  private val navigator: TrusteesIndividualNavigator =
    new TrusteesIndividualNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(false))


  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }

  s"when previousAddress feature is toggled On" must {
    val navigator: TrusteesIndividualNavigator =
    new TrusteesIndividualNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(true))
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode, true), dataDescriber, UpdateMode)
  }

}

object TrusteesIndividualNavigatorSpec extends SpecBase with OptionValues {
  private val newTrustee = UserAnswers(Json.obj()).set(IsTrusteeNewId(0))(true).asOpt.value

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())
  val firstIndex = Index(0)
  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId(0))(false).asOpt.value

  private def none = controllers.routes.IndexController.onPageLoad
  private def confirmPreviousAddress = controllers.register.trustees.individual.routes.IndividualConfirmPreviousAddressController.onPageLoad(0, None)

  private def details(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(mode, Index(0), None)

  private def nino(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(mode, firstIndex, None)

  private def utr(mode: Mode) = controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, firstIndex, None)

  private def postcode(mode: Mode) = controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(mode, firstIndex, None)

  private def addressList(mode: Mode) = controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(mode, firstIndex, None)

  private def address(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(mode, firstIndex, None)

  private def addressYears(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(mode, firstIndex, None)

  private def previousAddressPostcode(mode: Mode) = controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, firstIndex, None)

  private def previousAddressList(mode: Mode) = controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(mode, firstIndex, None)

  private def previousAddress(mode: Mode) = controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(mode, firstIndex, None)

  private def contactDetails(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(mode, firstIndex, None)

  private def checkYourAnswers(mode: Mode) = controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(mode, firstIndex, None)

  private def addTrustee(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private val overAYearNew = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.OverAYear).set(IsTrusteeNewId(0))(true).asOpt.get

  private val overAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.OverAYear)

  private val underAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.UnderAYear)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if(mode == CheckMode || mode == NormalMode) checkYourAnswers(mode)
  else {
    if(answers.get(IsTrusteeNewId(index)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers) =
    (
      userAnswers.get(ExistingCurrentAddressId(0)),
      mode
    ) match {
      case (None, CheckUpdateMode) =>
        Some(previousAddressPostcode(checkMode(mode)))
      case (_, CheckUpdateMode) =>
        Some(confirmPreviousAddress)
      case _ =>
        Some(previousAddressPostcode(checkMode(mode)))
    }

}
