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
import identifiers.Identifier
import identifiers.register.trustees.company._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import org.scalatest.prop.TableFor6
import org.scalatest.{MustMatchers, OptionValues}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers


class TrusteesCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import TrusteesCompanyNavigatorSpec._

  private def routesTrusteeCompany: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(

    ("Id", "UserAnswers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (CheckMode)", "Save (CM)"),
    (CompanyDetailsId(0), emptyAnswers, companyRegistrationNumber(NormalMode), true, Some(checkYourAnswers), true),
    (CompanyRegistrationNumberId(0), emptyAnswers, companyUTR(NormalMode), true, Some(checkYourAnswers), true),
    (CompanyUniqueTaxReferenceId(0), emptyAnswers, companyPostCodeLookup(NormalMode), true, Some(checkYourAnswers), true),
    (CompanyPostcodeLookupId(0), emptyAnswers, companyAddressList(NormalMode), true, Some(companyAddressList(CheckMode)), true),
    (CompanyAddressListId(0), emptyAnswers, companyManualAddress(NormalMode), true, Some(companyManualAddress(CheckMode)), true),
    (CompanyAddressId(0), emptyAnswers, companyAddressYears(NormalMode), true, Some(checkYourAnswers), true),
    (CompanyAddressYearsId(0), addressYearsOverAYear, companyContactDetails, true, Some(checkYourAnswers), true),
    (CompanyAddressYearsId(0), addressYearsUnderAYear, prevAddPostCodeLookup(NormalMode), true, Some(prevAddPostCodeLookup(CheckMode)), true),
    (CompanyPreviousAddressPostcodeLookupId(0), emptyAnswers, companyPaList(NormalMode), true, Some(companyPaList(CheckMode)), true),
    (CompanyPreviousAddressListId(0), emptyAnswers, companyPreviousAddress(NormalMode), true, Some(companyPreviousAddress(CheckMode)), true),
    (CompanyPreviousAddressId(0), emptyAnswers, companyContactDetails, true, Some(checkYourAnswers), true),
    (CompanyContactDetailsId(0), emptyAnswers, checkYourAnswers, true, Some(checkYourAnswers), true),
    (CompanyAddressYearsId(0), emptyAnswers, sessionExpired, false, Some(sessionExpired), false),
    (CheckYourAnswersId, emptyAnswers, addTrustee, true, None, false)
  )


  "EstablishersCompanyNavigator when restrict-establisher toggle is off" must {
    appRunning()
    val navigator = new TrusteesCompanyNavigator(FakeDataCacheConnector)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routesTrusteeCompany, dataDescriber)
    behave like nonMatchingNavigator(navigator)
  }
}

//noinspection MutatorLikeMethodIsParameterless
object TrusteesCompanyNavigatorSpec extends OptionValues {

  private val navigator = new TrusteesCompanyNavigator(FakeDataCacheConnector)

  private def companyRegistrationNumber(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyRegistrationNumberController.onPageLoad(mode, 0)

  private def companyUTR(mode: Mode): Call =
    controllers.register.trustees.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(mode, 0)

  private def companyPostCodeLookup(mode: Mode) = controllers.register.trustees.company.routes.CompanyPostCodeLookupController.onPageLoad(mode, 0)

  private def companyAddressList(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressListController.onPageLoad(mode, 0)

  private def companyManualAddress(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressController.onPageLoad(mode, 0)

  private def companyAddressYears(mode: Mode) = controllers.register.trustees.company.routes.CompanyAddressYearsController.onPageLoad(mode, 0)

  private def prevAddPostCodeLookup(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, 0)

  private def companyPaList(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressListController.onPageLoad(mode, 0)

  private def companyPreviousAddress(mode: Mode) =
    controllers.register.trustees.company.routes.CompanyPreviousAddressController.onPageLoad(mode, 0)

  private def companyContactDetails = controllers.register.trustees.company.routes.CompanyContactDetailsController.onPageLoad(NormalMode, 0)

  private def checkYourAnswers = controllers.register.trustees.company.routes.CheckYourAnswersController.onPageLoad(0)

  private def addTrustee = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode)

  private def sessionExpired = controllers.routes.SessionExpiredController.onPageLoad()

  private val emptyAnswers = UserAnswers(Json.obj())

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private def dataDescriber(answers: UserAnswers): String = answers.toString


}
