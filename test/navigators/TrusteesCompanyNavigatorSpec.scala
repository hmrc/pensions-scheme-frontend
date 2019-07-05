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
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.establishers.ExistingCurrentAddressId
import identifiers.register.trustees.company._
import models.Mode.checkMode
import models._
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{FakeFeatureSwitchManagementService, UserAnswers}


class TrusteesCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import TrusteesCompanyNavigatorSpec._

  private def routes(mode: Mode, isPrevAddEnabled:Boolean = false): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(

    ("Id", "UserAnswers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (CheckMode)", "Save (CM)"),
    (CompanyDetailsId(0), emptyAnswers, companyVat(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyDetailsId(0), newTrustee, companyVat(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyVatId(0), emptyAnswers, companyPaye(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyVatId(0), newTrustee, companyPaye(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyPayeId(0), emptyAnswers, companyRegistrationNumber(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyPayeId(0), newTrustee, companyRegistrationNumber(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyRegistrationNumberId(0), emptyAnswers, companyUTR(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyRegistrationNumberId(0), newTrustee, companyUTR(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyRegistrationNumberVariationsId(0), emptyAnswers, none, true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyUniqueTaxReferenceId(0), emptyAnswers, companyPostCodeLookup(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyUniqueTaxReferenceId(0), newTrustee, companyPostCodeLookup(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyPostcodeLookupId(0), emptyAnswers, companyAddressList(mode), true, Some(companyAddressList(checkMode(mode))), true),
    (CompanyAddressListId(0), emptyAnswers, companyManualAddress(mode), true, Some(companyManualAddress(checkMode(mode))), true),
    (CompanyAddressId(0), emptyAnswers, companyAddressYears(mode), true,
      if(mode == UpdateMode) Some(companyAddressYears(checkMode(mode))) else Some(checkYourAnswers(mode)), true),
    (CompanyAddressId(0), newTrustee, companyAddressYears(mode), true, Some(checkYourAnswers(mode)), true),
    (CompanyAddressYearsId(0), addressYearsOverAYear, companyContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyAddressYearsId(0), addressYearsOverAYearNew, companyContactDetails(mode), true, Some(exitJourney(mode, addressYearsOverAYearNew)), true),
    (CompanyAddressYearsId(0), addressYearsUnderAYear, prevAddPostCodeLookup(mode), true, Some(addressYearsLessThanTwelveEdit(checkMode(mode), addressYearsUnderAYear)), true),
    (CompanyPreviousAddressPostcodeLookupId(0), emptyAnswers, companyPaList(mode), true, Some(companyPaList(checkMode(mode))), true),
    (CompanyPreviousAddressListId(0), emptyAnswers, companyPreviousAddress(mode), true, Some(companyPreviousAddress(checkMode(mode))), true),
    (CompanyPreviousAddressId(0), emptyAnswers, companyContactDetails(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyContactDetailsId(0), emptyAnswers, checkYourAnswers(mode), true, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyContactDetailsId(0), newTrustee, checkYourAnswers(mode), true, Some(exitJourney(mode, newTrustee)), true),
    (CompanyAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (CheckYourAnswersId, emptyAnswers, addTrustee(mode), false, None, true),
    (CompanyVatVariationsId(0), emptyAnswers, index, false, Some(exitJourney(mode, emptyAnswers)), true),
    (CompanyVatVariationsId(0), newTrustee, index, false, Some(exitJourney(mode, newTrustee)), true)
  )

  private def editRoutesToggleOn(mode: Mode, isPrevAddEnabled:Boolean = false): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "UserAnswers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (CheckMode)", "Save (CM)"),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressYes, sessionExpired, false, Some(anyMoreChanges), false),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressNo, sessionExpired, false, Some(prevAddPostCodeLookup(checkMode(mode))), false),
    (CompanyConfirmPreviousAddressId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (CompanyPayeVariationsId(0), emptyAnswers, none, true, Some(exitJourney(checkMode(mode), emptyAnswers)), true)
  )

  private def editRoutesToggleOff(mode: Mode): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "UserAnswers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (CheckMode)", "Save (CM)"),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressYes, sessionExpired, false, Some(sessionExpired), false),
    (CompanyConfirmPreviousAddressId(0), confirmPreviousAddressNo, sessionExpired, false, Some(sessionExpired), false)
  )

  private val navigator: TrusteesCompanyNavigator =
    new TrusteesCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(false))

  s"${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(NormalMode), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode) ++ editRoutesToggleOff(UpdateMode), dataDescriber, UpdateMode)
    behave like nonMatchingNavigator(navigator)
    behave like nonMatchingNavigator(navigator, UpdateMode)
  }

  "is-address-pre-population-enabled toggled on" must {
    val navigator: TrusteesCompanyNavigator =
    new TrusteesCompanyNavigator(FakeUserAnswersCacheConnector, frontendAppConfig, new FakeFeatureSwitchManagementService(true))
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(UpdateMode, true) ++ editRoutesToggleOn(UpdateMode), dataDescriber, UpdateMode)
  }

}

//noinspection MutatorLikeMethodIsParameterless
object TrusteesCompanyNavigatorSpec extends SpecBase with OptionValues {

  private def none: Call = controllers.routes.IndexController.onPageLoad()

  private def taskList: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private def companyRegistrationNumber(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, None, 0)

  private def companyVat(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyVatController.onPageLoad(mode, 0, None)

  private def companyPaye(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyPayeController.onPageLoad(mode, 0, None)

  private def companyUTR(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, 0, None)

  private def companyPostCodeLookup(mode: Mode) = controllers.register.trustees.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, 0, None)

  private def companyAddressList(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(mode, 0, None)

  private def companyManualAddress(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(mode, 0, None)

  private def companyAddressYears(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(mode, 0, None)

  private def prevAddPostCodeLookup(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, 0, None)

  private def companyPaList(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, 0, None)

  private def companyPreviousAddress(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(mode, 0, None)

  private def confirmPreviousAddress = controllers.register.trustees.company.routes.CompanyConfirmPreviousAddressController.onPageLoad(0, None)


  private def companyContactDetails(mode: Mode) = controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(mode, 0, None)

  private def checkYourAnswers(mode: Mode) = controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(mode, 0, None)

  private def addTrustee(mode: Mode) = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(mode, None)


  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()
  private def index = controllers.routes.IndexController.onPageLoad()

  private val emptyAnswers = UserAnswers(Json.obj())

  private val addressYearsOverAYearNew = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).flatMap(_.set(IsTrusteeNewId(0))(true)).asOpt.value

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val newTrustee = UserAnswers(Json.obj()).set(IsTrusteeNewId(0))(true).asOpt.value

  private val confirmPreviousAddressYes = UserAnswers(Json.obj()).set(CompanyConfirmPreviousAddressId(0))(true).asOpt.value

  private val confirmPreviousAddressNo = UserAnswers(Json.obj()).set(CompanyConfirmPreviousAddressId(0))(false).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString

  private def anyMoreChanges = controllers.routes.AnyMoreChangesController.onPageLoad(None)

  private def exitJourney(mode: Mode, answers:UserAnswers, index:Int = 0) = if(mode == CheckMode || mode == NormalMode) checkYourAnswers(mode)
  else {
    if(answers.get(IsTrusteeNewId(index)).getOrElse(false)) checkYourAnswers(mode)
    else anyMoreChanges
  }

  private def addressYearsLessThanTwelveEdit(mode: Mode, userAnswers: UserAnswers): Call =
    (
      userAnswers.get(ExistingCurrentAddressId(0)),
      mode
    ) match {
      case (None, CheckUpdateMode) =>
        prevAddPostCodeLookup(mode)
      case (_, CheckUpdateMode) =>
        confirmPreviousAddress
      case _ =>
        prevAddPostCodeLookup(mode)
    }
}
