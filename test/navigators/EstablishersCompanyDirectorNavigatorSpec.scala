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
import controllers.register.establishers.company.director.routes
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director._
import identifiers.{AnyMoreChangesId, Identifier}
import models.Mode.checkMode
import models._
import models.address.Address
import models.person.PersonName
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersCompanyDirectorNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import EstablishersCompanyDirectorNavigatorSpec._

  private def commonRoutes(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (DirectorNameId(0, 0), emptyAnswers, directorDOB(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorNameId(0, 0), newDirector, directorDOB(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorDOBId(0, 0), emptyAnswers, directorHasNINO(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorDOBId(0, 0), newDirector, directorHasNINO(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorEnterNINOId(0, 0), emptyAnswers, directorHasUTR(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorNoUTRReasonId(0, 0), newDirector, directorAddressPostcode(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorHasNINOId(0, 0), hasNino(newDirector, value = true), directorNinoNew(mode), true, Some(directorNinoNew(checkMode(mode))), true),
    (DirectorHasNINOId(0, 0), hasNino(newDirector, value = false), directorNinoReason(mode), true, Some(directorNinoReason(checkMode(mode))), true),
    (DirectorHasUTRId(0, 0), hasUtr(newDirector, value = true), directorWhatIsDirectorUTR(mode), true, Some(directorWhatIsDirectorUTR(checkMode(mode))), true),
    (DirectorHasUTRId(0, 0), hasUtr(newDirector, value = false), directorWhyNoUTR(mode), true, Some(directorWhyNoUTR(checkMode(mode))), true),
    (DirectorAddressPostcodeLookupId(0, 0), emptyAnswers, directorAddressList(mode), true, Some(directorAddressList(checkMode(mode))), true),
    (DirectorAddressYearsId(0, 0), addressYearsOverAYear, directorEmail(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorAddressYearsId(0, 0), addressYearsOverAYearNew, directorEmail(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
    (DirectorAddressYearsId(0, 0), addressYearsUnderAYear, directorPreviousAddPostcode(mode), true,
      addressYearsLessThanTwelveEdit(mode, addressYearsUnderAYear), true),
    (DirectorAddressYearsId(0, 0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (DirectorConfirmPreviousAddressId(0, 0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
    (DirectorConfirmPreviousAddressId(0, 0), confirmPreviousAddressNo, none, false, Some(directorPreviousAddPostcode(checkMode(mode))), false),
    (DirectorConfirmPreviousAddressId(0, 0), emptyAnswers, none, false, Some(sessionExpired), false),
    (DirectorPreviousAddressPostcodeLookupId(0, 0), emptyAnswers, directorPreviousAddList(mode), true, Some(directorPreviousAddList(checkMode(mode))), true),
    (DirectorPreviousAddressListId(0, 0), emptyAnswers, directorEmail(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorPreviousAddressListId(0, 0), newDirector, directorEmail(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorPreviousAddressId(0, 0), emptyAnswers, directorEmail(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorPreviousAddressId(0, 0), newDirector, directorEmail(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorPhoneNumberId(0, 0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorPhoneNumberId(0, 0), newDirector, checkYourAnswers(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorEmailId(0, 0), emptyAnswers, directorPhone(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorEmailId(0, 0), newDirector, directorPhone(mode), true, Some(exitJourney(mode, newDirector)), true),
    (AnyMoreChangesId, newDirector, anyMoreChanges, true, None, true)
  )

  private def normalRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(NormalMode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, addCompanyDirectors(NormalMode), false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addCompanyDirectors(NormalMode), true, None, true),
    (DirectorAddressListId(0, 0), emptyAnswers, directorAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true),
    (DirectorAddressListId(0, 0), newDirector, directorAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true),
    (DirectorAddressId(0, 0), emptyAnswers, directorAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true),
    (DirectorAddressId(0, 0), newDirector, directorAddressYears(NormalMode), true, Some(checkYourAnswers(NormalMode)), true)
  )

  private def variancesRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(UpdateMode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, anyMoreChanges, false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addCompanyDirectors(UpdateMode), true, None, true),
    (CheckYourAnswersId(0, 0), newEstablisher, addCompanyDirectors(UpdateMode), true, None, true),
    (DirectorAddressYearsId(0, 0), directorNoExistingCurrentAddress, directorPreviousAddPostcode(UpdateMode), true, addressYearsLessThanTwelveEdit(UpdateMode, directorNoExistingCurrentAddress), true),
    (DirectorAddressYearsId(0, 0), directorExistingCurrentAddress, directorPreviousAddPostcode(UpdateMode), true, addressYearsLessThanTwelveEdit(UpdateMode, directorExistingCurrentAddress), true),
    (DirectorAddressListId(0, 0), emptyAnswers, directorAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true),
    (DirectorAddressListId(0, 0), newDirector, directorAddressYears(UpdateMode), true, Some(checkYourAnswers(UpdateMode)), true),
    (DirectorAddressId(0, 0), emptyAnswers, directorAddressYears(UpdateMode), true, Some(confirmPreviousAddress), true),
    (DirectorAddressId(0, 0), newDirector, directorAddressYears(UpdateMode), true, Some(checkYourAnswers(UpdateMode)), true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, variancesRoutes, dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }
}

object EstablishersCompanyDirectorNavigatorSpec extends SpecBase with OptionValues {

  private val config = injector.instanceOf[Configuration]

  private val navigator = new EstablishersCompanyDirectorNavigator(FakeUserAnswersCacheConnector)
  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers().set(IsEstablisherNewId(0))(true).asOpt.value
  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val newDirector = UserAnswers(Json.obj()).set(IsNewDirectorId(establisherIndex, directorIndex))(true).asOpt.value

  private def hasNino(ua: UserAnswers, value: Boolean): UserAnswers =
    ua.set(DirectorHasNINOId(0, 0))(value = value).asOpt.value

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(DirectorConfirmPreviousAddressId(0, 0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(DirectorConfirmPreviousAddressId(0, 0))(false).asOpt.value

  private def addressYearsLessThanTwelveEdit(mode: => Mode, userAnswers: => UserAnswers) =
    (
      userAnswers.get(ExistingCurrentAddressId(establisherIndex, directorIndex)),
      checkMode(mode)
    ) match {
      case (None, CheckUpdateMode) =>
        Some(directorPreviousAddPostcode(checkMode(mode)))
      case (_, CheckUpdateMode) =>
        Some(confirmPreviousAddress)
      case _ =>
        Some(directorPreviousAddPostcode(checkMode(mode)))
    }

  private def confirmPreviousAddress = routes.DirectorConfirmPreviousAddressController.onPageLoad(0, 0, None)

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers: UserAnswers, index: Int = 0) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if (answers.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private def hasUtr(ua: UserAnswers, value: Boolean): UserAnswers =
    ua.set(DirectorHasUTRId(0, 0))(value = value).asOpt.value

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def directorPhone(mode: Mode) = routes.DirectorPhoneNumberController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorEmail(mode: Mode) = routes.DirectorEmailController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressPostcode(mode: Mode) = routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorNinoNew(mode: Mode) = routes.DirectorEnterNINOController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorNinoReason(mode: Mode) = routes.DirectorNoNINOReasonController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressList(mode: Mode) = routes.DirectorAddressListController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddress(mode: Mode) = routes.DirectorAddressController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressYears(mode: Mode) = routes.DirectorAddressYearsController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddPostcode(mode: Mode) = routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddList(mode: Mode) = routes.DirectorPreviousAddressListController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddress(mode: Mode) = routes.DirectorPreviousAddressController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(directorIndex, establisherIndex, mode, None)

  private def addCompanyDirectors(mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(
    mode, None, establisherIndex)

  private def directorWhatIsDirectorUTR(mode: Mode) = routes.DirectorEnterUTRController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorWhyNoUTR(mode: Mode) = routes.DirectorNoUTRReasonController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorHasUTR(mode: Mode) = routes.DirectorHasUTRController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorDOB(mode: Mode) = routes.DirectorDOBController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorHasNINO(mode: Mode) = routes.DirectorHasNINOController.onPageLoad(mode, directorIndex, establisherIndex, None)

  val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).flatMap(
    _.set(IsNewDirectorId(establisherIndex, directorIndex))(true)).asOpt.value
  val addressYearsOverAYear: UserAnswers = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear: UserAnswers = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear).asOpt.value
  val addressYearsUnderAYearWithPreviousAddress: UserAnswers = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear).flatMap(
    _.set(ExistingCurrentAddressId(establisherIndex, directorIndex))(Address("Line 1", "Line 2", None, None, None, "UK"))).asOpt.value

  val directorNoExistingCurrentAddress: UserAnswers = UserAnswers(Json.obj())
    .set(IsNewDirectorId(establisherIndex, directorIndex))(true).flatMap(
    _.set(DirectorNameId(establisherIndex, directorIndex))(PersonName("Alan", "Allman", false))).flatMap(
    _.set(DirectorHasNINOId(establisherIndex, directorIndex))(true)).flatMap(
    _.set(DirectorEnterNINOId(establisherIndex, directorIndex))(ReferenceValue("a"))).flatMap(
    _.set(DirectorHasUTRId(establisherIndex, directorIndex))(true)).flatMap(
    _.set(DirectorEnterUTRId(establisherIndex, directorIndex))(ReferenceValue("a"))).flatMap(
    _.set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear)).flatMap(
    _.set(DirectorAddressId(establisherIndex, directorIndex))(Address("Line 1", "Line 2", None, None, None, "UK"))).asOpt.value

  val directorExistingCurrentAddress: UserAnswers =
    directorNoExistingCurrentAddress.set(ExistingCurrentAddressId(establisherIndex, directorIndex))(Address("Line 1", "Line 2", None, None, None, "UK")).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString


}
