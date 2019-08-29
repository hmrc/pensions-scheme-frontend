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
import controllers.register.trustees.company.routes._
import controllers.register.trustees.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import models.Mode._
import models._
import org.scalatest.MustMatchers
import org.scalatest.prop.TableFor3
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesCompanyNavigatorSpec extends SpecBase with MustMatchers with NavigatorBehaviour {

  import TrusteesCompanyNavigatorSpec._

  "TrusteesCompanyNavigator" must {

    behave like navigatorWithRoutesForMode(NormalMode)(navigator, normalAndUpdateModeRoutes(NormalMode), None)
    behave like navigatorWithRoutesForMode(CheckMode)(navigator, routesCheckMode(CheckMode), None)

    behave like navigatorWithRoutesForMode(UpdateMode)(navigator, normalAndUpdateModeRoutes(UpdateMode), None)

    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, routesCheckUpdateMode(CheckUpdateMode), None)
    behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, setNewTrusteeIdentifier(routesCheckMode(CheckUpdateMode)), None)
  }
}

object TrusteesCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {
  private def setNewTrusteeIdentifier(table: TableFor3[Identifier, UserAnswers, Call]): TableFor3[Identifier, UserAnswers, Call] = table.map(tuple =>
    (tuple._1, tuple._2.set(IsTrusteeNewId(0))(true).asOpt.value, tuple._3)
  )

  private val newTrustee = UserAnswers().set(IsTrusteeNewId(0))(true).asOpt

  private def addTrusteePage(mode: Mode): Call = AddTrusteeController.onPageLoad(mode, None)

  private def companyNoPage(mode: Mode): Call = CompanyRegistrationNumberVariationsController.onPageLoad(mode, None, 0)

  private def noCompanyNoPage(mode: Mode): Call = NoCompanyNumberController.onPageLoad(mode, 0, None)

  private def hasCompanyUtrPage(mode: Mode): Call = HasCompanyUTRController.onPageLoad(mode, 0, None)

  private def hasCompanyVatPage(mode: Mode): Call = HasCompanyVATController.onPageLoad(mode, 0, None)

  private def hasCompanyPayePage(mode: Mode): Call = HasCompanyPAYEController.onPageLoad(mode, 0, None)

  private def utrPage(mode: Mode): Call = CompanyUTRController.onPageLoad(mode, None, 0)

  private def noUtrPage(mode: Mode): Call = CompanyNoUTRReasonController.onPageLoad(mode, 0, None)

  private def vatPage(mode: Mode): Call = CompanyEnterVATController.onPageLoad(mode, 0, None)

  private def payePage(mode: Mode): Call = CompanyPayeVariationsController.onPageLoad(mode, 0, None)

  private def cyaPage(mode: Mode): Call = CheckYourAnswersCompanyDetailsController.onPageLoad(journeyMode(mode), 0, None)

  private def cyaAddressPage(mode: Mode): Call = CheckYourAnswersCompanyAddressController.onPageLoad(journeyMode(mode), 0, None)

  private def cyaContactDetailsPage(mode: Mode): Call = CheckYourAnswersCompanyContactDetailsController.onPageLoad(journeyMode(mode), 0, None)

  private def selectAddressPage(mode: Mode): Call = CompanyAddressListController.onPageLoad(mode, 0, None)

  private def confirmAddressPage(mode: Mode): Call = CompanyAddressController.onPageLoad(mode, 0, None)

  private def addressYearsPage(mode: Mode): Call = CompanyAddressYearsController.onPageLoad(mode, 0, None)

  private def hasBeenTradingPage(mode: Mode): Call = HasBeenTradingCompanyController.onPageLoad(mode, 0, None)

  private def previousAddressLookupPage(mode: Mode): Call = CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, 0, None)

  private def selectPreviousAddressPage(mode: Mode): Call = CompanyPreviousAddressListController.onPageLoad(mode, 0, None)

  private def confirmPreviousAddressPage(mode: Mode): Call = CompanyPreviousAddressController.onPageLoad(mode, 0, None)

  private def phonePage(mode: Mode): Call = CompanyPhoneController.onPageLoad(mode, 0, None)

  private def isThisPreviousAddressPage: Call = CompanyConfirmPreviousAddressController.onPageLoad(0, None)

  def normalAndUpdateModeRoutes(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(CompanyDetailsId(0))(CompanyDetails(someStringValue), addTrusteePage(mode)),
      row(HasCompanyNumberId(0))(true, companyNoPage(mode)),
      row(HasCompanyNumberId(0))(false, noCompanyNoPage(mode)),
      row(NoCompanyNumberId(0))(someStringValue, hasCompanyUtrPage(mode)),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, hasCompanyUtrPage(mode)),
      row(HasCompanyUTRId(0))(true, utrPage(mode)),
      row(HasCompanyUTRId(0))(false, noUtrPage(mode)),
      row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVatPage(mode)),
      row(CompanyUTRId(0))(someRefValue, hasCompanyVatPage(mode)),
      row(HasCompanyVATId(0))(true, vatPage(mode)),
      row(HasCompanyVATId(0))(false, hasCompanyPayePage(mode)),
      row(CompanyEnterVATId(0))(someRefValue, hasCompanyPayePage(mode)),
      row(HasCompanyPAYEId(0))(true, payePage(mode)),
      row(HasCompanyPAYEId(0))(false, cyaPage(mode)),
      row(CompanyPayeVariationsId(0))(someRefValue, cyaPage(mode)),
      row(CompanyPostcodeLookupId(0))(Seq(someTolerantAddress), selectAddressPage(mode)),
      row(CompanyAddressListId(0))(someTolerantAddress, confirmAddressPage(mode)),
      row(CompanyAddressId(0))(someAddress, addressYearsPage(mode)),
      row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(mode)),
      row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(mode)),
      row(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(mode)),
      row(HasBeenTradingCompanyId(0))(false, cyaAddressPage(mode)),
      row(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(mode)),
      row(CompanyPreviousAddressListId(0))(someTolerantAddress, confirmPreviousAddressPage(mode)),
      row(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(mode)),
      row(CompanyEmailId(0))(someStringValue, phonePage(mode)),
      row(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(mode))
    )

  def routesCheckMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] =
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(HasCompanyNumberId(0))(true, companyNoPage(mode)),
      row(HasCompanyNumberId(0))(false, noCompanyNoPage(mode)),
      row(NoCompanyNumberId(0))(someStringValue, cyaPage(mode)),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, cyaPage(mode)),
      row(HasCompanyUTRId(0))(true, utrPage(mode)),
      row(HasCompanyUTRId(0))(false, noUtrPage(mode)),
      row(CompanyNoUTRReasonId(0))(someStringValue, cyaPage(mode)),
      row(CompanyUTRId(0))(someRefValue, cyaPage(mode)),
      row(HasCompanyVATId(0))(true, vatPage(mode)),
      row(HasCompanyVATId(0))(false, cyaPage(mode)),
      row(CompanyEnterVATId(0))(someRefValue, cyaPage(mode)),
      row(HasCompanyPAYEId(0))(true, payePage(mode)),
      row(HasCompanyPAYEId(0))(false, cyaPage(mode)),
      row(CompanyPayeVariationsId(0))(someRefValue, cyaPage(mode)),
      row(CompanyAddressId(0))(someAddress, cyaAddressPage(mode)),
      row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(mode)),
      row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(mode)),
      row(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(mode)),
      row(HasBeenTradingCompanyId(0))(false, cyaAddressPage(mode)),
      row(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(mode)),
      row(CompanyPreviousAddressListId(0))(someTolerantAddress, confirmPreviousAddressPage(mode)),
      row(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(mode)),
      row(CompanyEmailId(0))(someStringValue, cyaContactDetailsPage(mode)),
      row(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(mode))
    )

  def routesCheckUpdateMode(mode: Mode): TableFor3[Identifier, UserAnswers, Call] = {
    Table(
      ("Id", "UserAnswers", "Next Page"),
      row(CompanyRegistrationNumberVariationsId(0))(someRefValue, anyMoreChangesPage()),
      row(CompanyUTRId(0))(someRefValue, anyMoreChangesPage()),
      row(CompanyEnterVATId(0))(someRefValue, anyMoreChangesPage()),
      row(CompanyPayeVariationsId(0))(someRefValue, anyMoreChangesPage()),
      row(CompanyAddressId(0))(someAddress, isThisPreviousAddressPage),
      row(CompanyConfirmPreviousAddressId(0))(true, anyMoreChangesPage()),
      row(CompanyConfirmPreviousAddressId(0))(false, previousAddressLookupPage(mode)),
      row(CompanyPreviousAddressId(0))(someAddress, anyMoreChangesPage()),
      row(CompanyEmailId(0))(someStringValue, anyMoreChangesPage()),
      row(CompanyPhoneId(0))(someStringValue, anyMoreChangesPage())
    )
  }

  val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigator]
}