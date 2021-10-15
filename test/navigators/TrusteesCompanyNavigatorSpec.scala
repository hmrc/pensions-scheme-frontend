/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import controllers.register.trustees.company.routes._
import controllers.register.trustees.routes._
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import identifiers.{Identifier, TypedIdentifier}
import models.Mode._
import models._
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableFor3
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Call
import utils.UserAnswers

class TrusteesCompanyNavigatorSpec extends SpecBase with Matchers with NavigatorBehaviour {

  import TrusteesCompanyNavigatorSpec._

  val navigator: Navigator =
    applicationBuilder(dataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))).build().injector.instanceOf[Navigator]

  "TrusteesCompanyNavigator" when {

    "in NormalMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails(someStringValue), addTrusteePage(NormalMode)),
          row(HasCompanyCRNId(0))(true, companyNoPage(NormalMode)),
          row(HasCompanyCRNId(0))(false, noCompanyNoPage(NormalMode)),
          row(CompanyNoCRNReasonId(0))(someStringValue, hasCompanyUtrPage(NormalMode)),
          row(CompanyEnterCRNId(0))(someRefValue, hasCompanyUtrPage(NormalMode)),
          row(HasCompanyUTRId(0))(true, utrPage(NormalMode)),
          row(HasCompanyUTRId(0))(false, noUtrPage(NormalMode)),
          row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVatPage(NormalMode)),
          row(CompanyEnterUTRId(0))(someRefValue, hasCompanyVatPage(NormalMode)),
          row(HasCompanyVATId(0))(true, vatPage(NormalMode)),
          row(HasCompanyVATId(0))(false, hasCompanyPayePage(NormalMode)),
          row(CompanyEnterVATId(0))(someRefValue, hasCompanyPayePage(NormalMode)),
          row(HasCompanyPAYEId(0))(true, payePage(NormalMode)),
          row(HasCompanyPAYEId(0))(false, cyaPage(NormalMode)),
          row(CompanyEnterPAYEId(0))(someRefValue, cyaPage(NormalMode)),
          row(CompanyPostcodeLookupId(0))(Seq(someTolerantAddress), selectAddressPage(NormalMode)),
          row(CompanyAddressListId(0))(someTolerantAddress, addressYearsPage(NormalMode)),
          row(CompanyAddressId(0))(someAddress, addressYearsPage(NormalMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(NormalMode)),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(NormalMode)),
          row(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(NormalMode)),
          row(HasBeenTradingCompanyId(0))(false, cyaAddressPage(NormalMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(NormalMode)),
          row(CompanyPreviousAddressListId(0))(someTolerantAddress, cyaAddressPage(NormalMode)),
          row(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(NormalMode)),
          row(CompanyEmailId(0))(someStringValue, phonePage(NormalMode)),
          row(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(NormalMode))
        )
      behave like navigatorWithRoutesForMode(NormalMode)(navigator, navigation, None)
    }

    "in CheckMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(HasCompanyCRNId(0))(true, companyNoPage(CheckMode)),
          row(HasCompanyCRNId(0))(false, noCompanyNoPage(CheckMode)),
          row(CompanyNoCRNReasonId(0))(someStringValue, cyaPage(CheckMode)),
          row(CompanyEnterCRNId(0))(someRefValue, cyaPage(CheckMode)),
          row(HasCompanyUTRId(0))(true, utrPage(CheckMode)),
          row(HasCompanyUTRId(0))(false, noUtrPage(CheckMode)),
          row(CompanyNoUTRReasonId(0))(someStringValue, cyaPage(CheckMode)),
          row(CompanyEnterUTRId(0))(someRefValue, cyaPage(CheckMode)),
          row(HasCompanyVATId(0))(true, vatPage(CheckMode)),
          row(HasCompanyVATId(0))(false, cyaPage(CheckMode)),
          row(CompanyEnterVATId(0))(someRefValue, cyaPage(CheckMode)),
          row(HasCompanyPAYEId(0))(true, payePage(CheckMode)),
          row(HasCompanyPAYEId(0))(false, cyaPage(CheckMode)),
          row(CompanyEnterPAYEId(0))(someRefValue, cyaPage(CheckMode)),
          row(CompanyAddressId(0))(someAddress, cyaAddressPage(CheckMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(CheckMode)),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(CheckMode)),
          row(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(CheckMode)),
          row(HasBeenTradingCompanyId(0))(false, cyaAddressPage(CheckMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(CheckMode)),
          row(CompanyPreviousAddressListId(0))(someTolerantAddress, cyaAddressPage(CheckMode)),
          row(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(CheckMode)),
          row(CompanyEmailId(0))(someStringValue, cyaContactDetailsPage(CheckMode)),
          row(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(CheckMode))
        )
      behave like navigatorWithRoutesForMode(CheckMode)(navigator, navigation, None)
    }

    "in UpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          row(CompanyDetailsId(0))(CompanyDetails(someStringValue), addTrusteePage(UpdateMode)),
          row(HasCompanyCRNId(0))(true, companyNoPage(UpdateMode)),
          row(HasCompanyCRNId(0))(false, noCompanyNoPage(UpdateMode)),
          row(CompanyNoCRNReasonId(0))(someStringValue, hasCompanyUtrPage(UpdateMode)),
          row(CompanyEnterCRNId(0))(someRefValue, hasCompanyUtrPage(UpdateMode)),
          row(HasCompanyUTRId(0))(true, utrPage(UpdateMode)),
          row(HasCompanyUTRId(0))(false, noUtrPage(UpdateMode)),
          row(CompanyNoUTRReasonId(0))(someStringValue, hasCompanyVatPage(UpdateMode)),
          row(CompanyEnterUTRId(0))(someRefValue, hasCompanyVatPage(UpdateMode)),
          row(HasCompanyVATId(0))(true, vatPage(UpdateMode)),
          row(HasCompanyVATId(0))(false, hasCompanyPayePage(UpdateMode)),
          row(CompanyEnterVATId(0))(someRefValue, hasCompanyPayePage(UpdateMode)),
          row(HasCompanyPAYEId(0))(true, payePage(UpdateMode)),
          row(HasCompanyPAYEId(0))(false, cyaPage(UpdateMode)),
          row(CompanyEnterPAYEId(0))(someRefValue, cyaPage(UpdateMode)),
          row(CompanyPostcodeLookupId(0))(Seq(someTolerantAddress), selectAddressPage(UpdateMode)),
          row(CompanyAddressListId(0))(someTolerantAddress, addressYearsPage(UpdateMode)),
          row(CompanyAddressId(0))(someAddress, addressYearsPage(UpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(UpdateMode)),
          row(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(UpdateMode)),
          row(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(UpdateMode)),
          row(HasBeenTradingCompanyId(0))(false, cyaAddressPage(UpdateMode)),
          row(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(UpdateMode)),
          row(CompanyPreviousAddressListId(0))(someTolerantAddress, cyaAddressPage(UpdateMode)),
          row(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(UpdateMode)),
          row(CompanyEmailId(0))(someStringValue, phonePage(UpdateMode)),
          row(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(UpdateMode))
        )
      behave like navigatorWithRoutesForMode(UpdateMode)(navigator, navigation, None)
    }

    "in CheckUpdateMode" must {
      def navigation: TableFor3[Identifier, UserAnswers, Call] =
        Table(
          ("Id", "UserAnswers", "Next Page"),
          rowNewTrustee(HasCompanyCRNId(0))(true, companyNoPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyCRNId(0))(false, noCompanyNoPage(CheckUpdateMode)),
          rowNewTrustee(CompanyNoCRNReasonId(0))(someStringValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(CompanyEnterCRNId(0))(someRefValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyUTRId(0))(true, utrPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyUTRId(0))(false, noUtrPage(CheckUpdateMode)),
          rowNewTrustee(CompanyNoUTRReasonId(0))(someStringValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(CompanyEnterUTRId(0))(someRefValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyVATId(0))(true, vatPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyVATId(0))(false, cyaPage(CheckUpdateMode)),
          rowNewTrustee(CompanyEnterVATId(0))(someRefValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyPAYEId(0))(true, payePage(CheckUpdateMode)),
          rowNewTrustee(HasCompanyPAYEId(0))(false, cyaPage(CheckUpdateMode)),
          rowNewTrustee(CompanyEnterPAYEId(0))(someRefValue, cyaPage(CheckUpdateMode)),
          rowNewTrustee(CompanyAddressId(0))(someAddress, cyaAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyAddressYearsId(0))(AddressYears.OverAYear, cyaAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyAddressYearsId(0))(AddressYears.UnderAYear, hasBeenTradingPage(CheckUpdateMode)),
          rowNewTrustee(HasBeenTradingCompanyId(0))(true, previousAddressLookupPage(CheckUpdateMode)),
          rowNewTrustee(HasBeenTradingCompanyId(0))(false, cyaAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyPreviousAddressPostcodeLookupId(0))(Seq(someTolerantAddress), selectPreviousAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyPreviousAddressListId(0))(someTolerantAddress, cyaAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyPreviousAddressId(0))(someAddress, cyaAddressPage(CheckUpdateMode)),
          rowNewTrustee(CompanyEmailId(0))(someStringValue, cyaContactDetailsPage(CheckUpdateMode)),
          rowNewTrustee(CompanyPhoneId(0))(someStringValue, cyaContactDetailsPage(CheckUpdateMode)),
          row(CompanyEnterCRNId(0))(someRefValue, anyMoreChangesPage()),
          row(CompanyEnterUTRId(0))(someRefValue, anyMoreChangesPage()),
          row(CompanyEnterVATId(0))(someRefValue, anyMoreChangesPage()),
          row(CompanyEnterPAYEId(0))(someRefValue, anyMoreChangesPage()),
          row(CompanyAddressId(0))(someAddress, isThisPreviousAddressPage),
          row(CompanyConfirmPreviousAddressId(0))(true, anyMoreChangesPage()),
          row(CompanyConfirmPreviousAddressId(0))(false, previousAddressLookupPage(CheckUpdateMode)),
          row(CompanyPreviousAddressId(0))(someAddress, anyMoreChangesPage()),
          row(CompanyEmailId(0))(someStringValue, anyMoreChangesPage()),
          row(CompanyPhoneId(0))(someStringValue, anyMoreChangesPage())
        )
      behave like navigatorWithRoutesForMode(CheckUpdateMode)(navigator, navigation, None)
    }
  }
}

object TrusteesCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {
  private def rowNewTrustee(id: TypedIdentifier.PathDependent)(value: id.Data, call: Call)(
      implicit writes: Writes[id.Data]): (id.type, UserAnswers, Call) = {
    val userAnswers = newTrustee.set(id)(value).asOpt.value
    Tuple3(id, userAnswers, call)
  }

  private val newTrustee = UserAnswers().set(IsTrusteeNewId(0))(true).asOpt.value

  private def addTrusteePage(mode: Mode): Call = AddTrusteeController.onPageLoad(mode, None)

  private def companyNoPage(mode: Mode): Call = CompanyEnterCRNController.onPageLoad(mode, None, 0)

  private def noCompanyNoPage(mode: Mode): Call = CompanyNoCRNReasonController.onPageLoad(mode, 0, None)

  private def hasCompanyUtrPage(mode: Mode): Call = HasCompanyUTRController.onPageLoad(mode, 0, None)

  private def hasCompanyVatPage(mode: Mode): Call = HasCompanyVATController.onPageLoad(mode, 0, None)

  private def hasCompanyPayePage(mode: Mode): Call = HasCompanyPAYEController.onPageLoad(mode, 0, None)

  private def utrPage(mode: Mode): Call = CompanyEnterUTRController.onPageLoad(mode, None, 0)

  private def noUtrPage(mode: Mode): Call = CompanyNoUTRReasonController.onPageLoad(mode, 0, None)

  private def vatPage(mode: Mode): Call = CompanyEnterVATController.onPageLoad(mode, 0, None)

  private def payePage(mode: Mode): Call = CompanyEnterPAYEController.onPageLoad(mode, 0, None)

  private def cyaPage(mode: Mode): Call = CheckYourAnswersCompanyDetailsController.onPageLoad(journeyMode(mode), 0, None)

  private def cyaAddressPage(mode: Mode): Call = CheckYourAnswersCompanyAddressController.onPageLoad(journeyMode(mode), 0, None)

  private def cyaContactDetailsPage(mode: Mode): Call =
    CheckYourAnswersCompanyContactDetailsController.onPageLoad(journeyMode(mode), 0, None)

  private def selectAddressPage(mode: Mode): Call = CompanyAddressListController.onPageLoad(mode, 0, None)

  private def addressYearsPage(mode: Mode): Call = CompanyAddressYearsController.onPageLoad(mode, 0, None)

  private def hasBeenTradingPage(mode: Mode): Call = HasBeenTradingCompanyController.onPageLoad(mode, 0, None)

  private def previousAddressLookupPage(mode: Mode): Call = CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, 0, None)

  private def selectPreviousAddressPage(mode: Mode): Call = CompanyPreviousAddressListController.onPageLoad(mode, 0, None)

  private def phonePage(mode: Mode): Call = CompanyPhoneController.onPageLoad(mode, 0, None)

  private def isThisPreviousAddressPage: Call = CompanyConfirmPreviousAddressController.onPageLoad(0, None)

  val navigator: Navigator = injector.instanceOf[TrusteesCompanyNavigator]
}
