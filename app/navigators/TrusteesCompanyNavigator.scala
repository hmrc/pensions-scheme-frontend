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

import com.google.inject.Inject
import connectors.UserAnswersCacheConnector
import controllers.register.trustees.company.routes._
import identifiers.Identifier
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.company._
import models.Mode._
import models._
import play.api.mvc.Call
import utils.UserAnswers
import controllers.register.trustees.routes.AddTrusteeController
import controllers.routes.AnyMoreChangesController

class TrusteesCompanyNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends AbstractNavigator {
  import TrusteesCompanyNavigator._

  private def normalAndUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case CompanyDetailsId(index) => addTrusteePage(mode, srn)
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, companyNoPage(mode, index, srn), noCompanyNoPage(mode, index, srn))
    case NoCompanyNumberId(index) => hasCompanyUtrPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => hasCompanyUtrPage(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrPage(mode, index, srn))
    case CompanyNoUTRReasonId(index) => hasCompanyVatPage(mode, index, srn)
    case CompanyUTRId(index) => hasCompanyVatPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, vatPage(mode, index, srn), noVatPage(mode, index, srn))
    case CompanyEnterVATId(index) => hasCompanyPayePage(mode, index, srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case CompanyPayeVariationsId(index) => cyaPage(mode, index, srn)
    case CompanyPostcodeLookupId(index) => selectAddressPage(mode, index, srn)
    case CompanyAddressListId(index) => confirmAddressPage(mode, index, srn)
    case CompanyAddressId(index) => addressYearsPage(mode, index, srn)
    case CompanyAddressYearsId(index) if overAYear(ua, index) => cyaAddressPage(mode, index, srn)
    case CompanyAddressYearsId(index) if underAYear(ua, index) => hasBeenTradingPage(mode, index, srn)
    case id@HasBeenTradingCompanyId(index) => booleanNav(id, ua, previousAddressLookupPage(mode, index, srn), cyaAddressPage(mode, index, srn))
    case CompanyPreviousAddressPostcodeLookupId(index) => selectPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressListId(index) => confirmPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressId(index) => cyaAddressPage(mode, index, srn)
    case CompanyEmailId(index) => phonePage(mode, index, srn)
    case CompanyPhoneId(index) => cyaContactDetailsPage(mode, index, srn)
  }

  private def checkModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, companyNoPage(mode, index, srn), noCompanyNoPage(mode, index, srn))
    case NoCompanyNumberId(index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrPage(mode, index, srn))
    case CompanyNoUTRReasonId(index) => cyaPage(mode, index, srn)
    case CompanyUTRId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua,vatPage(mode, index, srn), cyaPage(mode, index, srn))
    case CompanyEnterVATId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case CompanyPayeVariationsId(index) => cyaPage(mode, index, srn)
    case CompanyAddressId(index) => cyaAddressPage(mode, index, srn)
    case CompanyAddressYearsId(index) if overAYear(ua, index) => cyaAddressPage(mode, index, srn)
    case CompanyAddressYearsId(index) if underAYear(ua, index) => hasBeenTradingPage(mode, index, srn)
    case id@HasBeenTradingCompanyId(index) => booleanNav(id, ua, previousAddressLookupPage(mode, index, srn), cyaAddressPage(mode, index, srn))
    case CompanyPreviousAddressPostcodeLookupId(index) => selectPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressListId(index) => confirmPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressId(index) => cyaAddressPage(mode, index, srn)
    case CompanyEmailId(index) => cyaContactDetailsPage(mode, index, srn)
    case CompanyPhoneId(index) => cyaContactDetailsPage(mode, index, srn)
  }

  private def checkUpdateModeRoutes(mode: Mode, ua: UserAnswers, srn: Option[String]): PartialFunction[Identifier, Call] = {
    case id@HasCompanyNumberId(index) => booleanNav(id, ua, companyNoPage(mode, index, srn), noCompanyNoPage(mode, index, srn))
    case NoCompanyNumberId(index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case CompanyRegistrationNumberVariationsId(_) => anyMoreChangesPage(srn)
    case id@HasCompanyUTRId(index) => booleanNav(id, ua, utrPage(mode, index, srn), noUtrPage(mode, index, srn))
    case CompanyUTRId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case CompanyUTRId(_) => anyMoreChangesPage(srn)
    case CompanyNoUTRReasonId(index) => cyaPage(mode, index, srn)
    case id@HasCompanyVATId(index) => booleanNav(id, ua, vatPage(mode, index, srn), cyaPage(mode, index, srn))
    case CompanyEnterVATId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case CompanyEnterVATId(_) => anyMoreChangesPage(srn)
    case id@HasCompanyPAYEId(index) => booleanNav(id, ua, payePage(mode, index, srn), cyaPage(mode, index, srn))
    case CompanyPayeVariationsId(index) if isNewTrustee(ua, index) => cyaPage(mode, index, srn)
    case CompanyPayeVariationsId(_) => anyMoreChangesPage(srn)
    case CompanyAddressId(index) if isNewTrustee(ua, index) => cyaAddressPage(mode, index, srn)
    case CompanyAddressId(index) => isThisPreviousAddressPage(index, srn)
    case id@CompanyConfirmPreviousAddressId(index) => booleanNav(id, ua, moreChanges(mode, index, srn), previousAddressLookupPage(mode, index, srn))
    case CompanyAddressYearsId(index) if overAYear(ua, index) => cyaAddressPage(mode, index, srn)
    case CompanyAddressYearsId(index) if underAYear(ua, index) => hasBeenTradingPage(mode, index, srn)
    case CompanyPreviousAddressPostcodeLookupId(index) => selectPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressListId(index) => confirmPreviousAddressPage(mode, index, srn)
    case CompanyPreviousAddressId(index) if isNewTrustee(ua, index) => cyaAddressPage(mode, index, srn)
    case CompanyPreviousAddressId(_) => anyMoreChangesPage(srn)
    case id@HasBeenTradingCompanyId(index) => booleanNav(id, ua, previousAddressLookupPage(mode, index, srn), cyaAddressPage(mode, index, srn))
    case CompanyEmailId(index) if isNewTrustee(ua, index) => cyaContactDetailsPage(mode, index, srn)
    case CompanyEmailId(_) => anyMoreChangesPage(srn)
    case CompanyPhoneId(index) if isNewTrustee(ua, index) => cyaContactDetailsPage(mode, index, srn)
    case CompanyPhoneId(_) => anyMoreChangesPage(srn)
  }

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = navigateTo(normalAndUpdateModeRoutes(NormalMode, from.userAnswers, None), from.id)

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = navigateTo(checkModeRoutes(CheckMode, from.userAnswers, None), from.id)

  override protected def updateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = navigateTo(normalAndUpdateModeRoutes(UpdateMode, from.userAnswers, srn), from.id)

  override protected def checkUpdateRouteMap(from: NavigateFrom, srn: Option[String]): Option[NavigateTo] = navigateTo(checkUpdateModeRoutes(CheckUpdateMode, from.userAnswers, srn), from.id)
}

object TrusteesCompanyNavigator {
  private def isNewTrustee(ua: UserAnswers, index: Int): Boolean = ua.get(IsTrusteeNewId(index)).getOrElse(false)

  private def overAYear(ua: UserAnswers, index: Int): Boolean = ua.get(CompanyAddressYearsId(index)).contains(AddressYears.OverAYear)

  private def underAYear(ua: UserAnswers, index: Int): Boolean = ua.get(CompanyAddressYearsId(index)).contains(AddressYears.UnderAYear)

  private def companyNoPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyRegistrationNumberVariationsController.onPageLoad(mode, srn, index)

  private def noCompanyNoPage(mode: Mode, index: Int, srn: Option[String]): Call = NoCompanyNumberController.onPageLoad(mode, index, srn)

  private def utrPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyUTRController.onPageLoad(mode, srn, index)

  private def noUtrPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyNoUTRReasonController.onPageLoad(mode, index, srn)

  private def vatPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyEnterVATController.onPageLoad(mode, index, srn)

  private def noVatPage(mode: Mode, index: Int, srn: Option[String]): Call = HasCompanyPAYEController.onPageLoad(mode, index, srn)

  private def payePage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyPayeVariationsController.onPageLoad(mode, index, srn)

  private def cyaPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersCompanyDetailsController.onPageLoad(journeyMode(mode), index, srn)

  private def cyaContactDetailsPage(mode: Mode, index: Int, srn: Option[String]): Call =
    CheckYourAnswersCompanyContactDetailsController.onPageLoad(journeyMode(mode), index, srn)

  private def cyaAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CheckYourAnswersCompanyAddressController.onPageLoad(journeyMode(mode), index, srn)

  private def hasCompanyUtrPage(mode: Mode, index: Int, srn: Option[String]): Call = HasCompanyUTRController.onPageLoad(mode, index, srn)

  private def hasCompanyVatPage(mode: Mode, index: Int, srn: Option[String]): Call = HasCompanyVATController.onPageLoad(mode, index, srn)

  private def hasCompanyPayePage(mode: Mode, index: Int, srn: Option[String]): Call = HasCompanyPAYEController.onPageLoad(mode, index, srn)

  private def phonePage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyPhoneController.onPageLoad(mode, index, srn)

  private def addTrusteePage(mode: Mode, srn: Option[String]): Call = AddTrusteeController.onPageLoad(mode, srn)

  private def selectAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyAddressListController.onPageLoad(mode, index, srn)

  private def confirmAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyAddressController.onPageLoad(mode, index, srn)

  private def addressYearsPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyAddressYearsController.onPageLoad(mode, index, srn)

  private def hasBeenTradingPage(mode: Mode, index: Int, srn: Option[String]): Call = HasBeenTradingCompanyController.onPageLoad(mode, index, srn)

  private def previousAddressLookupPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyPreviousAddressPostcodeLookupController.onPageLoad(mode, index, srn)

  private def selectPreviousAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyPreviousAddressListController.onPageLoad(mode, index, srn)

  private def confirmPreviousAddressPage(mode: Mode, index: Int, srn: Option[String]): Call = CompanyPreviousAddressController.onPageLoad(mode, index, srn)

  private def isThisPreviousAddressPage(index: Int, srn: Option[String]): Call = CompanyConfirmPreviousAddressController.onPageLoad(index, srn)

  private def moreChanges(mode: Mode, index: Int, srn: Option[String]): Call = AnyMoreChangesController.onPageLoad(srn)

}
