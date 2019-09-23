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
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.ExistingCurrentAddressId
import identifiers.register.establishers.individual._
import models.Mode._
import models._
import models.address.Address
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersIndividualNavigatorOldSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import EstablishersIndividualNavigatorOldSpec._

  private def routes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = {
    Table(
      ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
      (EstablisherDetailsId(0), emptyAnswers, establisherNino(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (EstablisherDetailsId(0), newEstablisher, establisherNino(mode), true, Some(exitJourney(mode, newEstablisher)), true),
      (EstablisherNinoId(0), emptyAnswers, establisherUtr(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (EstablisherNinoId(0), newEstablisher, establisherUtr(mode), true, Some(exitJourney(mode, newEstablisher)), true),
      (UniqueTaxReferenceId(0), emptyAnswers, postCodeLookup(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (UniqueTaxReferenceId(0), newEstablisher, postCodeLookup(mode), true, Some(exitJourney(mode, newEstablisher)), true),
      (PostCodeLookupId(0), emptyAnswers, addressList(mode), true, Some(addressList(checkMode(mode))), true),
      (AddressListId(0), emptyAnswers, address(mode), true, Some(address(checkMode(mode))), true),
      (AddressId(0), emptyAnswers, addressYears(mode), true, if(mode == UpdateMode) Some(confirmPreviousAddress) else Some(checkYourAnswers(mode)), true),
      (AddressId(0), newEstablisher, addressYears(mode), true, Some(checkYourAnswers(mode)), true),
      (AddressYearsId(0), addressYearsOverAYearNew, contactDetails(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
      (AddressYearsId(0), addressYearsOverAYear, contactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (AddressYearsId(0), addressYearsUnderAYear, previousAddressPostCodeLookup(mode), true, addressYearsLessThanTwelveEdit(checkMode(mode), addressYearsUnderAYear), true),
      (AddressYearsId(0), addressYearsUnderAYearWithExistingCurrentAddress, previousAddressPostCodeLookup(mode), true, addressYearsLessThanTwelveEdit(checkMode(mode), addressYearsUnderAYearWithExistingCurrentAddress), true),
      (AddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
      (IndividualConfirmPreviousAddressId(0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
      (IndividualConfirmPreviousAddressId(0), confirmPreviousAddressNo, none, false, Some(previousAddressPostCodeLookup(checkMode(mode))), false),
      (IndividualConfirmPreviousAddressId(0), emptyAnswers, none, false, Some(sessionExpired), false),
      (PreviousPostCodeLookupId(0), emptyAnswers, previousAddressAddressList(mode), true, Some(previousAddressAddressList(checkMode(mode))), true),
      (PreviousAddressListId(0), emptyAnswers, previousAddress(mode), true, Some(previousAddress(checkMode(mode))), true),
      (PreviousAddressId(0), emptyAnswers, contactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (PreviousAddressId(0), newEstablisher, contactDetails(mode), true, Some(exitJourney(mode, newEstablisher)), true),
      (ContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
      (ContactDetailsId(0), newEstablisher, checkYourAnswers(mode), true, Some(exitJourney(mode, newEstablisher)), true),
      (CheckYourAnswersId, emptyAnswers, if(mode==UpdateMode) anyMoreChanges else addEstablisher(mode), false, None, true)
    )
  }

  private val navigator: EstablishersIndividualNavigatorOld =
    new EstablishersIndividualNavigatorOld(frontendAppConfig, FakeUserAnswersCacheConnector)

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode), dataDescriber, UpdateMode)
  }
}

object EstablishersIndividualNavigatorOldSpec extends SpecBase with OptionValues {

  private val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.OverAYear).flatMap(
    _.set(IsEstablisherNewId(0))(true)).asOpt.value
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearWithExistingCurrentAddress = UserAnswers(Json.obj())
    .set(AddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
    .set(ExistingCurrentAddressId(0))(Address("Line 1", "Line 2", None, None, None, "UK")).asOpt.value
  private val newEstablisher = UserAnswers(Json.obj())
    .set(IsEstablisherNewId(0))(true).asOpt.value
  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId(0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId(0))(false).asOpt.value


  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def establisherNino(mode: Mode) = controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(mode, 0, None)

  private def establisherUtr(mode: Mode) = controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(mode, 0, None)

  private def postCodeLookup(mode: Mode) = controllers.register.establishers.individual.routes.PostCodeLookupController.onPageLoad(mode, 0, None)

  private def addressList(mode: Mode) = controllers.register.establishers.individual.routes.AddressListController.onPageLoad(mode, 0, None)

  private def address(mode: Mode) = controllers.register.establishers.individual.routes.AddressController.onPageLoad(mode, 0, None)

  private def addressYears(mode: Mode) = controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(mode, 0, None)

  private def confirmPreviousAddress = controllers.register.establishers.individual.routes.IndividualConfirmPreviousAddressController.onPageLoad(0, None)

  private def contactDetails(mode: Mode) = controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(mode, 0, None)

  private def previousAddressPostCodeLookup(mode: Mode) =
    controllers.register.establishers.individual.routes.PreviousAddressPostCodeLookupController.onPageLoad(mode, 0, None)

  private def previousAddressAddressList(mode: Mode) =
    controllers.register.establishers.individual.routes.PreviousAddressListController.onPageLoad(mode, 0, None)

  private def previousAddress(mode: Mode) = controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(mode, 0, None)

  private def addEstablisher(mode: Mode) = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, None)

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def checkYourAnswers(mode: Mode) = controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if(mode == CheckMode || mode == NormalMode) checkYourAnswers(mode)
  else {
      if(answers.get(IsEstablisherNewId(index)).getOrElse(false)) checkYourAnswers(mode)
      else anyMoreChanges
    }

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers)=
    (
      userAnswers.get(AddressYearsId(0)),
      mode,
      userAnswers.get(ExistingCurrentAddressId(0))
    ) match {
      case (Some(AddressYears.UnderAYear), CheckUpdateMode, Some(_)) =>
        Some(confirmPreviousAddress)
      case (Some(AddressYears.UnderAYear), _, _) =>
        Some(previousAddressPostCodeLookup(mode))
      case (Some(AddressYears.OverAYear), _, _) =>
        Some(exitJourney(mode, userAnswers, 0))
      case _ =>
        Some(previousAddressPostCodeLookup(mode))
    }
}


