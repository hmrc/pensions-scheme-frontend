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
import identifiers.Identifier
import identifiers.register.establishers.IsEstablisherNewId
import identifiers.register.establishers.company.director._
import models.Mode.checkMode
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersCompanyDirectorNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import EstablishersCompanyDirectorNavigatorSpec._

  private def commonRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (DirectorDetailsId(0, 0), emptyAnswers, directorNino(mode), true, Some(exitJourney(mode)), true),
    (DirectorNinoId(0, 0), emptyAnswers, directorUtr(mode), true, Some(exitJourney(mode)), true),
    (DirectorUniqueTaxReferenceId(0, 0), emptyAnswers, directorAddressPostcode(mode), true, Some(exitJourney(mode)), true),
    (DirectorAddressPostcodeLookupId(0, 0), emptyAnswers, directorAddressList(mode), true, Some(directorAddressList(checkMode(mode))), true),
    (DirectorAddressListId(0, 0), emptyAnswers, directorAddress(mode), true, Some(directorAddress(checkMode(mode))), true),
    (DirectorAddressId(0, 0), emptyAnswers, directorAddressYears(mode), true,
      if(mode == UpdateMode) Some(directorAddressYears(checkMode(UpdateMode))) else Some(checkYourAnswers(NormalMode)), true),
    (DirectorAddressId(0, 0), newDirector, directorAddressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (DirectorAddressYearsId(0, 0), addressYearsOverAYear, directorContactDetails(mode), true, Some(exitJourney(mode)), true),
    (DirectorAddressYearsId(0, 0), addressYearsUnderAYear, directorPreviousAddPostcode(mode), true, Some(directorPreviousAddPostcode(checkMode(mode))), true),
    (DirectorAddressYearsId(0, 0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (DirectorPreviousAddressPostcodeLookupId(0, 0), emptyAnswers, directorPreviousAddList(mode), true, Some(directorPreviousAddList(checkMode(mode))), true),
    (DirectorPreviousAddressListId(0, 0), emptyAnswers, directorPreviousAddress(mode), true, Some(directorPreviousAddress(checkMode(mode))), true),
    (DirectorPreviousAddressId(0, 0), emptyAnswers, directorContactDetails(mode), true, Some(exitJourney(mode)), true),
    (DirectorContactDetailsId(0, 0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode)), true)
  )

  private def normalRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(mode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, addCompanyDirectors(mode), false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, addCompanyDirectors(mode), true, None, true)
  )

  private def editRoutes(mode:Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = commonRoutes(mode) ++ Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (ConfirmDeleteDirectorId(0), emptyAnswers, anyMoreChanges, false, None, false),
    (CheckYourAnswersId(0, 0), emptyAnswers, anyMoreChanges, true, None, true),
    (CheckYourAnswersId(0, 0), newEstablisher, addCompanyDirectors(mode), true, None, true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, editRoutes(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
  }

}

object EstablishersCompanyDirectorNavigatorSpec extends OptionValues {

  private val navigator = new EstablishersCompanyDirectorNavigator(FakeUserAnswersCacheConnector)
  private val emptyAnswers = UserAnswers(Json.obj())
  private val newEstablisher = UserAnswers().set(IsEstablisherNewId(0))(true).asOpt.value
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  private val newDirector = UserAnswers(Json.obj()).set(IsNewDirectorId(establisherIndex, directorIndex))(true).asOpt.value
  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode) = checkYourAnswers(mode)
  
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

  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
