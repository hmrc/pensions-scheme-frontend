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
import identifiers.Identifier
import identifiers.register.establishers.individual._
import identifiers.register.trustees.HaveAnyTrusteesId
import models._
import models.Mode.checkMode
import models.register.SchemeType
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersIndividualNavigatorSpec._

  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (EstablisherDetailsId(0), emptyAnswers, establisherNino(mode), true, Some(exitJourney(mode)), true),
    (EstablisherNinoId(0), emptyAnswers, establisherUtr(mode), true, Some(exitJourney(mode)), true),
    (UniqueTaxReferenceId(0), emptyAnswers, postCodeLookup(mode), true, Some(exitJourney(mode)), true),
    (PostCodeLookupId(0), emptyAnswers, addressList(mode), true, Some(addressList(checkMode(mode))), true),
    (AddressListId(0), emptyAnswers, address(mode), true, Some(address(checkMode(mode))), true),
    (AddressId(0), emptyAnswers, addressYears(mode), true, Some(exitJourney(mode)), true),
    (AddressYearsId(0), addressYearsOverAYear, contactDetails(mode), true, Some(exitJourney(mode)), true),
    (AddressYearsId(0), addressYearsUnderAYear, previousAddressPostCodeLookup(mode), true, Some(previousAddressPostCodeLookup(checkMode(mode))), true),
    (AddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (PreviousPostCodeLookupId(0), emptyAnswers, previousAddressAddressList(mode), true, Some(previousAddressAddressList(checkMode(mode))), true),
    (PreviousAddressListId(0), emptyAnswers, previousAddress(mode), true, Some(previousAddress(checkMode(mode))), true),
    (PreviousAddressId(0), emptyAnswers, contactDetails(mode), true, Some(exitJourney(mode)), true),
    (CheckYourAnswersId, emptyAnswers, addEstablisher(mode), false, None, true)
  )

  private val navigator: EstablishersIndividualNavigator =
    new EstablishersIndividualNavigator(frontendAppConfig, FakeUserAnswersCacheConnector)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
  }
}

object EstablishersIndividualNavigatorSpec extends OptionValues {
  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val hasTrusteeCompanies = UserAnswers().trusteesCompanyDetails(0, CompanyDetails("test-company-name"))
  private val bodyCorporateWithNoTrustees =
    UserAnswers().schemeName("test-scheme-name").schemeType(SchemeType.BodyCorporate).set(HaveAnyTrusteesId)(false).asOpt.value

  private def establisherNino(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(mode, 0, None)

  private def establisherUtr(mode: Mode) = controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, 0, None)

  private def postCodeLookup(mode: Mode) = controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(mode, 0, None)

  private def addressList(mode: Mode) = controllers.register.establishers.individual.routes.AddressListController.onPageLoad(mode, 0, None)

  private def address(mode: Mode) = controllers.register.establishers.individual.routes.AddressController.onPageLoad(mode, 0, None)

  private def addressYears(mode: Mode) = controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(mode, 0, None)

  private def contactDetails(mode: Mode) = controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(mode, 0, None)

  private def previousAddressPostCodeLookup(mode: Mode) =
    controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, 0, None)

  private def previousAddressAddressList(mode: Mode) =
    controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(mode, 0, None)

  private def previousAddress(mode: Mode) = controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(mode, 0, None)

  private def haveAnyTrustees = controllers.register.trustees.routes.HaveAnyTrusteesController.onPageLoad(NormalMode, None)

  private def addTrustees(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def checkYourAnswers(mode: Mode) = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode) = if (mode == NormalMode) checkYourAnswers(mode) else anyMoreChanges

}


