/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.routes.*
import identifiers.*
import models.*
import models.TypeOfBenefits.Defined
import utils.{Enumerable, UserAnswers}

class AboutBenefitsAndInsuranceNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector)
  extends AbstractNavigator with Enumerable.Implicits {


  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case InvestmentRegulatedSchemeId =>
        NavigateTo.dontSave(OccupationalPensionSchemeController.onPageLoad(NormalMode))
      case OccupationalPensionSchemeId => NavigateTo.dontSave(TypeOfBenefitsController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers)
      case MoneyPurchaseBenefitsId => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case BenefitsSecuredByInsuranceId => benefitsSecuredRoutes(from.userAnswers, NormalMode)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case InsurerEnterPostCodeId => NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case InsurerSelectAddressId => checkYourAnswers(NormalMode)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode)
      case _ => None
    }
  }

  private def typesOfBenefitsRoutes(userAnswers: UserAnswers, mode: Mode = NormalMode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(TypeOfBenefitsId) match {
      case Some(Defined) => mode match {
        case NormalMode => NavigateTo.dontSave(BenefitsSecuredByInsuranceController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
        case CheckMode => checkYourAnswers(NormalMode, srn)
        case _ => anyMoreChanges(srn)
      }
      case _ => NavigateTo.dontSave(MoneyPurchaseBenefitsController.onPageLoad(mode, srn))
    }
  }

  private def benefitsSecuredRoutes(userAnswers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, EmptyOptionalSchemeReferenceNumber))
      case Some(false) => checkYourAnswers(mode)
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] =
    from.id match {
      case InvestmentRegulatedSchemeId => checkYourAnswers(NormalMode)
      case OccupationalPensionSchemeId => checkYourAnswers(NormalMode)
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers, CheckMode)
      case MoneyPurchaseBenefitsId => checkYourAnswers(NormalMode)
      case BenefitsSecuredByInsuranceId => benefitsSecuredEditRoutes(from.userAnswers, CheckMode)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber))
      case InsurancePolicyNumberId => checkYourAnswers(NormalMode)
      case InsurerConfirmAddressId => checkYourAnswers(NormalMode)
      case _ => None
    }

  private def benefitsSecuredEditRoutes(userAnswers: UserAnswers,
                                        mode: Mode,
                                        srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Option[NavigateTo] = {
    userAnswers.get(BenefitsSecuredByInsuranceId) match {
      case Some(true) => NavigateTo.dontSave(InsuranceCompanyNameController.onPageLoad(mode, srn))
      case Some(false) => if (mode == CheckMode) {
        checkYourAnswers(NormalMode, srn)
      } else {
        anyMoreChanges(srn)
      }
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad)
    }
  }

  private def checkYourAnswers(mode: Mode, srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.CheckYourAnswersBenefitsAndInsuranceController.onPageLoad(mode, srn))

  private def anyMoreChanges(srn: OptionalSchemeReferenceNumber): Option[NavigateTo] =
    NavigateTo.dontSave(controllers.routes.AnyMoreChangesController.onPageLoad(srn))

  protected def updateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = from.id match {
    case InsurancePolicyNumberId => NavigateTo.dontSave(InsurerEnterPostcodeController.onPageLoad(CheckUpdateMode, srn))
    case _ => None
  }

  protected def checkUpdateRouteMap(from: NavigateFrom, srn: OptionalSchemeReferenceNumber): Option[NavigateTo] = {
    from.id match {
      case TypeOfBenefitsId => typesOfBenefitsRoutes(from.userAnswers, CheckUpdateMode, srn)
      case MoneyPurchaseBenefitsId => anyMoreChanges(srn)
      case BenefitsSecuredByInsuranceId => benefitsSecuredEditRoutes(from.userAnswers, CheckUpdateMode, srn)
      case InsuranceCompanyNameId => NavigateTo.dontSave(InsurancePolicyNumberController.onPageLoad(UpdateMode, srn))
      case InsurancePolicyNumberId => anyMoreChanges(srn)
      case InsurerEnterPostCodeId =>
        NavigateTo.dontSave(InsurerSelectAddressController.onPageLoad(CheckUpdateMode, srn))
      case InsurerSelectAddressId => anyMoreChanges(srn)
      case InsurerConfirmAddressId => anyMoreChanges(srn)
      case _ => None
    }
  }
}


