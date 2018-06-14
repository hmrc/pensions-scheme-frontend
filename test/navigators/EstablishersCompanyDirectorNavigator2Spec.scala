/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.FakeDataCacheConnector
import controllers.register.establishers.company.director.routes
import identifiers.Identifier
import identifiers.register.establishers.company.director._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class EstablishersCompanyDirectorNavigator2Spec extends SpecBase with NavigatorBehaviour2 {

  import EstablishersCompanyDirectorNavigator2Spec._

  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                              "User Answers", "Next Page (Normal Mode)",          "Save (NM)",  "Next Page (Check Mode)",             "Save (CM)"),
    (DirectorDetailsId(0, 0),                 emptyAnswers,   directorNino(NormalMode),               true,         Some(checkYourAnswers),                   true),
    (DirectorNinoId(0, 0),                    emptyAnswers,   directorUtr(NormalMode),                true,         Some(checkYourAnswers),                   true),
    (DirectorUniqueTaxReferenceId(0, 0),      emptyAnswers,   directorAddressPostcode(NormalMode),    true,         Some(checkYourAnswers),                   true),
    (DirectorAddressPostcodeLookupId(0, 0),   emptyAnswers,   directorAddressList(NormalMode),        true,         Some(directorAddressList(CheckMode)),     true),
    (DirectorAddressListId(0, 0),             emptyAnswers,   directorAddress(NormalMode),            true,         Some(directorAddress(CheckMode)),         true),
    (DirectorAddressId(0, 0),                 emptyAnswers,   directorAddressYears(NormalMode),       true,         Some(checkYourAnswers),                   true),
    (DirectorAddressYearsId(0, 0),            addressYearsOverAYear,   directorContactDetails(NormalMode),true,     Some(checkYourAnswers),                   true),
    (DirectorAddressYearsId(0, 0),            addressYearsUnderAYear,   directorPreviousAddPostcode(NormalMode),true,Some(directorPreviousAddPostcode(CheckMode)),  true),
    (DirectorPreviousAddressPostcodeLookupId(0, 0), emptyAnswers,   directorPreviousAddList(NormalMode),true,       Some(directorPreviousAddList(CheckMode)),       true),
    (DirectorPreviousAddressListId(0, 0),     emptyAnswers,   directorPreviousAddress(NormalMode),    true,         Some(directorPreviousAddress(CheckMode)),       true),
    (DirectorPreviousAddressId(0, 0),         emptyAnswers,   directorContactDetails(NormalMode),     true,         Some(checkYourAnswers),                   true),
    (DirectorContactDetailsId(0, 0),          emptyAnswers,   checkYourAnswers,                       true,         Some(checkYourAnswers),                   true),
    (ConfirmDeleteDirectorId(0),           emptyAnswers,   addCompanyDirectors(NormalMode),        false,        None,                                     false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }

}

object EstablishersCompanyDirectorNavigator2Spec extends OptionValues{

  private val navigator = new EstablishersCompanyDirectorNavigator2(FakeDataCacheConnector)
  private val emptyAnswers = UserAnswers(Json.obj())
  val establisherIndex = Index(0)
  val directorIndex = Index(0)

  private def directorDetails(mode: Mode) = routes.DirectorDetailsController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorNino(mode: Mode) = routes.DirectorNinoController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorUtr(mode: Mode) = routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorContactDetails(mode: Mode) = routes.DirectorContactDetailsController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorAddressPostcode(mode: Mode) = routes.DirectorAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorAddressList(mode: Mode) = routes.DirectorAddressListController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorAddress(mode: Mode) = routes.DirectorAddressController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorAddressYears(mode: Mode) = routes.DirectorAddressYearsController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorPreviousAddPostcode(mode: Mode) = routes.DirectorPreviousAddressPostcodeLookupController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorPreviousAddList(mode: Mode) = routes.DirectorPreviousAddressListController.onPageLoad(mode, directorIndex, establisherIndex)
  private def directorPreviousAddress(mode: Mode) = routes.DirectorPreviousAddressController.onPageLoad(mode, directorIndex, establisherIndex)
  private def checkYourAnswers = routes.CheckYourAnswersController.onPageLoad(directorIndex, establisherIndex)
  private def addCompanyDirectors(mode: Mode) = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(
    mode, establisherIndex)

  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(establisherIndex, directorIndex))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString
}
