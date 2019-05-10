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
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.individual._
import models._
import models.Mode.checkMode
import org.scalatest.OptionValues
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesIndividualNavigatorSpec._

  private def routes(mode: Mode) = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (TrusteeDetailsId(0), emptyAnswers, nino(mode), true, Some(checkYourAnswers(mode)), true),
    (TrusteeNinoId(0), emptyAnswers, utr(mode), true, Some(checkYourAnswers(mode)), true),
    (UniqueTaxReferenceId(0), emptyAnswers, postcode(mode), true, Some(checkYourAnswers(mode)), true),
    (IndividualPostCodeLookupId(0), emptyAnswers, addressList(mode), true, Some(addressList(checkMode(mode))), true),
    (IndividualAddressListId(0), emptyAnswers, address(mode), true, Some(address(checkMode(mode))), true),
    (TrusteeAddressId(0), emptyAnswers, addressYears(mode), true,
      if(mode == UpdateMode) Some(addressYears(checkMode(mode))) else Some(checkYourAnswers(mode)), true),
    (TrusteeAddressId(0), newTrustee, addressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (TrusteeAddressYearsId(0), overAYear, contactDetails(mode), true, Some(checkYourAnswers(mode)), true),
    (TrusteeAddressYearsId(0), underAYear, previousAddressPostcode(mode), true, Some(previousAddressPostcode(checkMode(mode))), true),
    (TrusteeAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (IndividualPreviousAddressPostCodeLookupId(0), emptyAnswers, previousAddressList(mode), true, Some(previousAddressList(checkMode(mode))), true),
    (TrusteePreviousAddressListId(0), emptyAnswers, previousAddress(mode), true, Some(previousAddress(checkMode(mode))), true),
    (TrusteePreviousAddressId(0), emptyAnswers, contactDetails(mode), true, Some(checkYourAnswers(mode)), true),
    (TrusteeContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, None, true),
    (CheckYourAnswersId, emptyAnswers, if(mode==UpdateMode) controllers.routes.AnyMoreChangesController.onPageLoad(None) else addTrustee(mode), false, None, true)
  )

  private val navigator: TrusteesIndividualNavigator =
    new TrusteesIndividualNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)


  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }

}

object TrusteesIndividualNavigatorSpec extends OptionValues {
  private val newTrustee = UserAnswers(Json.obj()).set(IsTrusteeNewId(0))(true).asOpt.value

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())
  val firstIndex = Index(0)

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

  private def overAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.OverAYear)

  private def underAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.UnderAYear)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode) = if (mode == NormalMode) checkYourAnswers(mode) else anyMoreChanges

}
