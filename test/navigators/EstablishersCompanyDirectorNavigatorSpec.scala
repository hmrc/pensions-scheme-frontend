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
import config.FeatureSwitchManagementServiceTestImpl
import connectors.FakeUserAnswersCacheConnector
import controllers.register.establishers.company.director.routes
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director._
import models.Mode.checkMode
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Toggles, UserAnswers}

class EstablishersCompanyDirectorNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import EstablishersCompanyDirectorNavigatorSpec._

  private def commonRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (DirectorDetailsId(0, 0), emptyAnswers, directorNino(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorDetailsId(0, 0), newDirector, directorNino(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorNinoId(0, 0), emptyAnswers, directorUtr(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorNinoId(0, 0), newDirector, directorUtr(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorUniqueTaxReferenceId(0, 0), emptyAnswers, directorAddressPostcode(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorUniqueTaxReferenceId(0, 0), newDirector, directorAddressPostcode(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorAddressPostcodeLookupId(0, 0), emptyAnswers, directorAddressList(mode), true, Some(directorAddressList(checkMode(mode))), true),
    (DirectorAddressListId(0, 0), emptyAnswers, directorAddress(mode), true, Some(directorAddress(checkMode(mode))), true),
    (DirectorAddressId(0, 0), emptyAnswers, directorAddressYears(mode), true,
      if(mode == UpdateMode) Some(directorAddressYears(checkMode(mode))) else Some(checkYourAnswers(mode)), true),
    (DirectorAddressId(0, 0), newDirector, directorAddressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (DirectorAddressYearsId(0, 0), addressYearsOverAYear, directorContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorAddressYearsId(0, 0), addressYearsOverAYearNew, directorContactDetails(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
    (DirectorAddressYearsId(0, 0), addressYearsUnderAYear, directorPreviousAddPostcode(mode), true, addressYearsLessThanTwelveEdit(mode), true),
    (DirectorAddressYearsId(0, 0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (DirectorConfirmPreviousAddressId(0, 0), confirmPreviousAddressYes, none, false, Some(anyMoreChanges), false),
    (DirectorConfirmPreviousAddressId(0, 0), confirmPreviousAddressNo, none, false, Some(directorPreviousAddPostcode(checkMode(mode))), false),
    (DirectorPreviousAddressPostcodeLookupId(0, 0), emptyAnswers, directorPreviousAddList(mode), true, Some(directorPreviousAddList(checkMode(mode))), true),
    (DirectorPreviousAddressListId(0, 0), emptyAnswers, directorPreviousAddress(mode), true, Some(directorPreviousAddress(checkMode(mode))), true),
    (DirectorPreviousAddressId(0, 0), emptyAnswers, directorContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorPreviousAddressId(0, 0), newDirector, directorContactDetails(mode), true, Some(exitJourney(mode, newDirector)), true),
    (DirectorContactDetailsId(0, 0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (DirectorContactDetailsId(0, 0), newDirector, checkYourAnswers(mode), true, Some(exitJourney(mode, newDirector)), true)
  )

  private def normalRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(mode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, addCompanyDirectors(mode), false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addCompanyDirectors(mode), true, None, true)
  )

  private def editRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(mode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, anyMoreChanges, false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addCompanyDirectors(mode), true, None, true),
    (CheckYourAnswersId(0, 0), newEstablisher, addCompanyDirectors(mode), true, None, true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, editRoutes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }

}

object EstablishersCompanyDirectorNavigatorSpec extends SpecBase with OptionValues {

  private val config = injector.instanceOf[Configuration]
  private val featureSwitch = new FeatureSwitchManagementServiceTestImpl(config, environment)
  private val navigator = new EstablishersCompanyDirectorNavigator(FakeUserAnswersCacheConnector, featureSwitch)
  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers().set(IsEstablisherNewId(0))(true).asOpt.value
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  private val newDirector = UserAnswers(Json.obj()).set(IsNewDirectorId(establisherIndex, directorIndex))(true).asOpt.value

  private val confirmPreviousAddressYes = UserAnswers(Json.obj())
    .set(DirectorConfirmPreviousAddressId(0, 0))(true).asOpt.value
  private val confirmPreviousAddressNo = UserAnswers(Json.obj())
    .set(DirectorConfirmPreviousAddressId(0, 0))(false).asOpt.value




  private def addressYearsLessThanTwelveEdit(mode: Mode) =
    if (checkMode(mode) == CheckUpdateMode && featureSwitch.get(Toggles.isPrevAddEnabled))
      Some(confirmPreviousAddress)
    else
      Some(directorPreviousAddPostcode(checkMode(mode)))

  private def confirmPreviousAddress = routes.DirectorConfirmPreviousAddressController.onPageLoad(0, 0, None)

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if (mode == NormalMode) checkYourAnswers(mode) else {
    if(answers.get(IsNewDirectorId(establisherIndex, directorIndex)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }
  
  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private def directorDetails(mode: Mode) = routes.DirectorDetailsController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorNino(mode: Mode) = routes.DirectorNinoController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorUtr(mode: Mode) = routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorContactDetails(mode: Mode) = routes.DirectorContactDetailsController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressPostcode(mode: Mode) = routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressList(mode: Mode) = routes.DirectorAddressListController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddress(mode: Mode) = routes.DirectorAddressController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorAddressYears(mode: Mode) = routes.DirectorAddressYearsController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddPostcode(mode: Mode) = routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddList(mode: Mode) = routes.DirectorPreviousAddressListController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def directorPreviousAddress(mode: Mode) = routes.DirectorPreviousAddressController.onPageLoad(mode, directorIndex, establisherIndex, None)

  private def checkYourAnswers(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(directorIndex, establisherIndex, mode, None)

  private def addCompanyDirectors(mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(
    mode, None, establisherIndex)

  val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).flatMap(
    _.set(IsNewDirectorId(establisherIndex, directorIndex))(true)).asOpt.value
  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
