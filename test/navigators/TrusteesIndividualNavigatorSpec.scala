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
import identifiers.register.trustees.individual._
import models._
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesIndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import TrusteesIndividualNavigatorSpec._

  private def routes() = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (TrusteeDetailsId(0), emptyAnswers, nino(NormalMode), true, Some(checkYourAnswers), true),
    (TrusteeNinoId(0), emptyAnswers, utr(NormalMode), true, Some(checkYourAnswers), true),
    (UniqueTaxReferenceId(0), emptyAnswers, postcode(NormalMode), true, Some(checkYourAnswers), true),
    (IndividualPostCodeLookupId(0), emptyAnswers, addressList(NormalMode), true, Some(addressList(CheckMode)), true),
    (IndividualAddressListId(0), emptyAnswers, address(NormalMode), true, Some(address(CheckMode)), true),
    (TrusteeAddressId(0), emptyAnswers, addressYears(NormalMode), true, Some(checkYourAnswers), true),
    (TrusteeAddressYearsId(0), overAYear, contactDetails(NormalMode), true, Some(checkYourAnswers), true),
    (TrusteeAddressYearsId(0), underAYear, previousAddressPostcode(NormalMode), true, Some(previousAddressPostcode(CheckMode)), true),
    (TrusteeAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (IndividualPreviousAddressPostCodeLookupId(0), emptyAnswers, previousAddressList(NormalMode), true, Some(previousAddressList(CheckMode)), true),
    (TrusteePreviousAddressListId(0), emptyAnswers, previousAddress(NormalMode), true, Some(previousAddress(CheckMode)), true),
    (TrusteePreviousAddressId(0), emptyAnswers, contactDetails(NormalMode), true, Some(checkYourAnswers), true),
    (TrusteeContactDetailsId(0), emptyAnswers, checkYourAnswers, true, None, true),
    (CheckYourAnswersId, emptyAnswers, addTrustee(NormalMode), false, None, true)
  )

  private val navigator: TrusteesIndividualNavigator =
    new TrusteesIndividualNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)


  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

object TrusteesIndividualNavigatorSpec {

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad()

  private val emptyAnswers = UserAnswers(Json.obj())
  val firstIndex = Index(0)

  private def details(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeDetailsController.onPageLoad(mode, Index(0))

  private def nino(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeNinoController.onPageLoad(mode, firstIndex)

  private def utr(mode: Mode) = controllers.register.trustees.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, firstIndex)

  private def postcode(mode: Mode) = controllers.register.trustees.individual.routes.IndividualPostCodeLookupController.onPageLoad(mode, firstIndex)

  private def addressList(mode: Mode) = controllers.register.trustees.individual.routes.IndividualAddressListController.onPageLoad(mode, firstIndex)

  private def address(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeAddressController.onPageLoad(mode, firstIndex)

  private def addressYears(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeAddressYearsController.onPageLoad(mode, firstIndex)

  private def previousAddressPostcode(mode: Mode) = controllers.register.trustees.individual.routes.IndividualPreviousAddressPostcodeLookupController.onPageLoad(mode, firstIndex)

  private def previousAddressList(mode: Mode) = controllers.register.trustees.individual.routes.TrusteePreviousAddressListController.onPageLoad(mode, firstIndex)

  private def previousAddress(mode: Mode) = controllers.register.trustees.individual.routes.TrusteePreviousAddressController.onPageLoad(mode, firstIndex)

  private def contactDetails(mode: Mode) = controllers.register.trustees.individual.routes.TrusteeContactDetailsController.onPageLoad(mode, firstIndex)

  private def checkYourAnswers = controllers.register.trustees.individual.routes.CheckYourAnswersController.onPageLoad(firstIndex)

  private def addTrustee(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def overAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.OverAYear)

  private def underAYear = UserAnswers().trusteesIndividualAddressYears(0, AddressYears.UnderAYear)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
